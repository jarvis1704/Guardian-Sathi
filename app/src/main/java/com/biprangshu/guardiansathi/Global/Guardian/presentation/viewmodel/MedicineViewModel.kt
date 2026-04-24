package com.biprangshu.guardiansathi.Global.Guardian.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.domain.MedicineReminder
import com.biprangshu.guardiansathi.Global.core.domain.MedicineRepository
import com.biprangshu.guardiansathi.Global.core.domain.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class MedicineState(
    val reminders: List<MedicineReminder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val linkedUid: String? = null
)

sealed interface MedicineAction {
    data class AddReminder(val reminder: MedicineReminder) : MedicineAction
    data class UpdateReminder(val reminder: MedicineReminder) : MedicineAction
    data class DeleteReminder(val reminderId: String) : MedicineAction
    data object Refresh : MedicineAction
}

sealed interface MedicineEvent {
    data class ShowError(val message: String) : MedicineEvent
    data object Success : MedicineEvent
}

@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(MedicineState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<MedicineEvent>()
    val events = _events.asSharedFlow()

    init {
        fetchLinkedUidAndObserve()
    }

    private fun fetchLinkedUidAndObserve() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val uid = auth.uid
            if (uid != null) {
                try {
                    val doc = firestore.collection("users").document(uid).get().await()
                    val linkedUid = doc.getString("linkedUid")
                    if (linkedUid != null) {
                        _state.update { it.copy(linkedUid = linkedUid) }
                        observeReminders(linkedUid)
                    } else {
                        _state.update { it.copy(isLoading = false, error = "No linked elder found") }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "User not authenticated") }
            }
        }
    }

    private fun observeReminders(linkedUid: String) {
        viewModelScope.launch {
            medicineRepository.getReminders(linkedUid).collectLatest { reminders ->
                _state.update { it.copy(reminders = reminders, isLoading = false) }
            }
        }
    }

    fun onAction(action: MedicineAction) {
        val linkedUid = _state.value.linkedUid ?: return
        when (action) {
            is MedicineAction.AddReminder -> {
                viewModelScope.launch {
                    when (val result = medicineRepository.addReminder(linkedUid, action.reminder)) {
                        is Result.Success -> _events.emit(MedicineEvent.Success)
                        is Result.Error -> _events.emit(MedicineEvent.ShowError("Failed to add reminder"))
                    }
                }
            }
            is MedicineAction.UpdateReminder -> {
                viewModelScope.launch {
                    when (val result = medicineRepository.updateReminder(linkedUid, action.reminder)) {
                        is Result.Success -> _events.emit(MedicineEvent.Success)
                        is Result.Error -> _events.emit(MedicineEvent.ShowError("Failed to update reminder"))
                    }
                }
            }
            is MedicineAction.DeleteReminder -> {
                viewModelScope.launch {
                    when (val result = medicineRepository.deleteReminder(linkedUid, action.reminderId)) {
                        is Result.Success -> _events.emit(MedicineEvent.Success)
                        is Result.Error -> _events.emit(MedicineEvent.ShowError("Failed to delete reminder"))
                    }
                }
            }
            MedicineAction.Refresh -> fetchLinkedUidAndObserve()
        }
    }
}

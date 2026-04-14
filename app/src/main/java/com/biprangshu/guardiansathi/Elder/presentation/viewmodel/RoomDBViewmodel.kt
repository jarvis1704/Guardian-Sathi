package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Elder.data.local.ContactRepository
import com.biprangshu.guardiansathi.Elder.data.local.GuardianContact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RoomDBViewmodel @Inject constructor(
    private val repository: ContactRepository
): ViewModel() {

    val contacts: StateFlow<List<GuardianContact>> = repository
        .getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addContact(name: String, phone: String) {
        viewModelScope.launch {
            repository.addContact(name, phone)
        }
    }

    fun deletecontact(contact: GuardianContact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }
}
package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class ElderHomeScreenViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
): ViewModel() {

//    private val _guardianName = MutableStateFlow<>
//    private val _guardianPhotoUrl = MutableStateFlow<String?>(null)




    val guardianName = sessionRepository.guardianName
    val guardianPhotoUrl = sessionRepository.guardianPhotoUrl
}
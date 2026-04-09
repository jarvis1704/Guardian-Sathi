package com.biprangshu.guardiansathi.Global.presentation.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.guardiansathi.Global.core.setAppLanguage
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    fun onLanguageSelected(context: Context, languageCode: String) {
        viewModelScope.launch {
            sessionRepository.setLanguageSelected(true)
            // Restart MainActivity to apply the new locale
            setAppLanguage(context, languageCode)
        }
    }
}
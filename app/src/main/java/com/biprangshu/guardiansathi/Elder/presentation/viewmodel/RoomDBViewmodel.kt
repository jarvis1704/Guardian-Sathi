package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.biprangshu.guardiansathi.Elder.data.local.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class RoomDBViewmodel @Inject constructor(
    private val repository: ContactRepository
): ViewModel() {

}
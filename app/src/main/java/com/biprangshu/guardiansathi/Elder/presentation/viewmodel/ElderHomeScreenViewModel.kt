package com.biprangshu.guardiansathi.Elder.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.guardiansathi.Global.domain.SessionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging.getInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ElderHomeScreenViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
): ViewModel() {

//    private val _guardianName = MutableStateFlow<>
//    private val _guardianPhotoUrl = MutableStateFlow<String?>(null)

    suspend fun getFCMTokenAndSave() {
        try {
            getInstance().token.await().let { token ->
                Log.d("FCM", "Got token: $token")
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
                val db = FirebaseDatabase.getInstance().reference
                db.child(uid)
                    .child("device_token")
                    .setValue(token)
                    .addOnSuccessListener {
                        Log.d("FCM", "Token saved successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Failed to save token: ${e.message}")
                    }
            }
        } catch (e: Exception) {
            Log.e("FCM", "Failed to get FCM token", e)
        }
    }


    val guardianName = sessionRepository.guardianName
    val guardianPhotoUrl = sessionRepository.guardianPhotoUrl
}
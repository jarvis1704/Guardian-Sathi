package com.biprangshu.guardiansathi.Elder.data

import android.util.Log
import com.biprangshu.guardiansathi.Elder.core.NotificationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class ElderFirebaseRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseDatabase: FirebaseDatabase
) : ElderFirebaseRepository {
    override fun sendDataToFirebaseDatabase(label: String, data: String){
        val uid = firebaseAuth.uid?:""
        val ref = firebaseDatabase.reference

        if (uid.isNotEmpty()){
            ref.child(uid)
                .child(label)
                .setValue(data)
        }
    }

    override fun updateFirebaseTimestamp(label: String) {
        val uid = firebaseAuth.uid?:""
        val ref = firebaseDatabase.reference

        if (uid.isNotEmpty()){
            ref.child(uid)
                .child(label)
                .setValue(ServerValue.TIMESTAMP)
        }
    }

    override fun sendNotificaitonToGuardian(notificationData: NotificationData, isOtp: Boolean, isTransaction: Boolean) {
        val uid = firebaseAuth.uid ?: return

        // Step 1: Get linkedUid from Firestore
        firebaseFirestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val linkedUid = document.getString("linkedUid") ?: return@addOnSuccessListener

                // Step 2 & 3 & 4: Navigate to linkedUid in Realtime DB
                // "notifications" node is auto-created when you push to it
                val notificationsRef = firebaseDatabase.reference
                    .child(linkedUid)
                    .child("notifications")

                // push() auto-generates a unique key like "sdfjalsk234kdskf"
                val newNotifRef = notificationsRef.push()

                if (isOtp) {
                    val payload = mapOf(
                        "title" to "OTP Detected",
                        "body" to notificationData.title,
                        "desc" to notificationData.subText + " | " + notificationData.text,
                        "imp" to "HIGH",
                        "time" to ServerValue.TIMESTAMP,
                        "appName" to notificationData.appName,
                    )

                    newNotifRef.setValue(payload)
                        .addOnSuccessListener {
                            Log.d("ElderFirebaseRepo", "Notification sent to guardian: $linkedUid")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ElderFirebaseRepo", "Failed to send notification", e)
                        }
                } else if (isTransaction) {  //check if transaction
                    val payload = mapOf(
                        "title" to notificationData.title,
                        "body" to notificationData.text,
                        "desc" to notificationData.subText,
                        "imp" to "HIGH",
                        "time" to ServerValue.TIMESTAMP,
                        "appName" to notificationData.appName,
                    )

                    newNotifRef.setValue(payload)
                        .addOnSuccessListener {
                            Log.d("ElderFirebaseRepo", "Notification sent to guardian: $linkedUid")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ElderFirebaseRepo", "Failed to send notification", e)
                        }
                } else {
                    //all other messages
                    val payload = mapOf(
                        "title" to notificationData.title,
                        "body" to notificationData.text,
                        "desc" to notificationData.subText.ifEmpty { notificationData.text },
                        "imp" to "LOW",
                        "time" to ServerValue.TIMESTAMP,
                        "appName" to notificationData.appName,
                    )

                    newNotifRef.setValue(payload)
                        .addOnSuccessListener {
                            Log.d("ElderFirebaseRepo", "Notification sent to guardian: $linkedUid")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ElderFirebaseRepo", "Failed to send notification", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ElderFirebaseRepo", "Failed to fetch linkedUid", e)
            }
    }

    override fun pushActivityLog(type: String) {
        val uid = firebaseAuth.uid ?: return
        val ref = firebaseDatabase.reference.child(uid).child("activity_logs").push()
        val logData = mapOf(
            "type" to type,
            "timestamp" to ServerValue.TIMESTAMP
        )
        ref.setValue(logData)

        // Also push a notification to the linked guardian
        firebaseFirestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val linkedUid = doc.getString("linkedUid")
                if (!linkedUid.isNullOrEmpty()) {
                    val notifRef =
                        firebaseDatabase.reference.child(linkedUid).child("notifications").push()
                    val title =
                        if (type == "GEOFENCE_ENTER") "Safe Zone Entered" else "Safe Zone Exited"
                    val body =
                        if (type == "GEOFENCE_ENTER") "Elder has entered the safe zone." else "Elder has left the safe zone."
                    notifRef.setValue(
                        mapOf(
                            "title" to title,
                            "body" to body,
                            "timestamp" to ServerValue.TIMESTAMP
                        )
                    )
                }
            }
    }
}
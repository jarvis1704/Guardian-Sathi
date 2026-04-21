package com.biprangshu.guardiansathi.Guardian.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class GuardianAlertsRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) : GuardianAlertsRepository {

    override fun getAlerts(guardianUid: String): Flow<List<GuardianAlert>> = callbackFlow {
        val ref = firebaseDatabase.reference.child(guardianUid).child("notifications")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alerts = mutableListOf<GuardianAlert>()
                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val title = child.child("title").getValue(String::class.java) ?: ""
                    val body = child.child("body").getValue(String::class.java) ?: ""
                    val desc = child.child("desc").getValue(String::class.java) ?: ""
                    val imp = child.child("imp").getValue(String::class.java) ?: "LOW"
                    val appName = child.child("appName").getValue(String::class.java) ?: ""
                    
                    // Handle both "time" and "timestamp"
                    val time = child.child("time").getValue(Long::class.java)
                    val timestamp = child.child("timestamp").getValue(Long::class.java)
                    val finalTimestamp = time ?: timestamp ?: 0L
                    
                    alerts.add(
                        GuardianAlert(
                            id = id,
                            title = title,
                            body = body,
                            desc = desc,
                            imp = imp,
                            timestamp = finalTimestamp,
                            appName = appName
                        )
                    )
                }
                // Sort descending so newest is first
                alerts.sortByDescending { it.timestamp }
                trySend(alerts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GuardianAlertsRepo", "Database error: ${error.message}")
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}

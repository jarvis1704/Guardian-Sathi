package com.biprangshu.guardiansathi.Elder.data

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
}
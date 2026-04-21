package com.biprangshu.guardiansathi.Elder.data

interface ElderFirebaseRepository {
    fun sendDataToFirebaseDatabase(label: String, data: String)
    fun updateFirebaseTimestamp(label: String)
    fun pushActivityLog(type: String)
}
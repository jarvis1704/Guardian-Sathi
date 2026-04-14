package com.biprangshu.guardiansathi.Elder.data.local

import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getAllContacts(): Flow<List<GuardianContact>>
    suspend fun addContact(name: String, phone: String)
    suspend fun deleteContact(contact: GuardianContact)
}
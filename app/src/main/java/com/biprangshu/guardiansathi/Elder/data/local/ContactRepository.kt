package com.biprangshu.guardiansathi.Elder.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ContactRepository {
    fun getAllContacts(): Flow<List<GuardianContact>>
    suspend fun addContact(name: String, phone: String)
    suspend fun deleteContact(contact: GuardianContact)
}

class ContactRepositoryImpl @Inject constructor(
    private val dao: GuardianContactDao
) : ContactRepository {

    override fun getAllContacts(): Flow<List<GuardianContact>> = dao.getAllContacts()

    override suspend fun addContact(name: String, phone: String) {
        dao.insert(GuardianContact(name = name, phone = phone))
    }

    override suspend fun deleteContact(contact: GuardianContact) {
        dao.delete(contact)
    }
}
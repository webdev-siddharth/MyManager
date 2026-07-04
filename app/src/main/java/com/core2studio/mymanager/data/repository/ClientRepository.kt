package com.core2studio.mymanager.data.repository

import com.core2studio.mymanager.data.local.dao.ClientDao
import com.core2studio.mymanager.data.local.entity.Client
import kotlinx.coroutines.flow.Flow

class ClientRepository(private val clientDao: ClientDao) {

    fun getAllClients(): Flow<List<Client>> = clientDao.getAllClients()

    suspend fun getClientById(id: String): Client? = clientDao.getClientById(id)

    fun searchClients(query: String): Flow<List<Client>> = clientDao.searchClients(query)

    suspend fun insertClient(client: Client) = clientDao.insert(client)

    suspend fun updateClient(client: Client) = clientDao.update(client)

    suspend fun deleteClient(client: Client) = clientDao.delete(client)
}

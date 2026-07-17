package com.core2studio.mymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.core2studio.mymanager.data.local.entity.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: Client)

    @Update
    suspend fun update(client: Client)

    @Delete
    suspend fun delete(client: Client)

    @Query("DELETE FROM clients")
    suspend fun deleteAll()

    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients ORDER BY name ASC")
    suspend fun getAllOnce(): List<Client>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: String): Client?

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchClients(query: String): Flow<List<Client>>
}

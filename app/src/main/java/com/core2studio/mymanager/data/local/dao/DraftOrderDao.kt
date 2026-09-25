package com.core2studio.mymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.core2studio.mymanager.data.local.entity.DraftOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draft: DraftOrder)

    @Query("SELECT * FROM draft_orders WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getDraftsByUser(userId: String): Flow<List<DraftOrder>>

    @Query("SELECT * FROM draft_orders WHERE id = :id")
    suspend fun getDraftById(id: String): DraftOrder?

    @Query("DELETE FROM draft_orders WHERE id = :id")
    suspend fun deleteDraft(id: String)

    @Query("DELETE FROM draft_orders WHERE userId = :userId")
    suspend fun deleteAllDrafts(userId: String)
}
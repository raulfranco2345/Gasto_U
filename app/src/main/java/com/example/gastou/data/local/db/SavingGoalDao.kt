package com.example.gastou.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingGoalDao {

    @Query("SELECT * FROM saving_goals WHERE syncState != ${SyncState.PENDING_DELETE} ORDER BY isCompleted ASC, updatedAt DESC")
    fun observeActive(): Flow<List<SavingGoalEntity>>

    @Query("SELECT * FROM saving_goals WHERE syncState = :state")
    suspend fun getByState(state: Int): List<SavingGoalEntity>

    @Upsert
    suspend fun upsert(goal: SavingGoalEntity)

    @Upsert
    suspend fun upsertAll(goals: List<SavingGoalEntity>)

    @Query("UPDATE saving_goals SET syncState = :state WHERE id = :id")
    suspend fun updateState(id: String, state: Int)

    @Query("DELETE FROM saving_goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM saving_goals WHERE syncState = ${SyncState.SYNCED}")
    suspend fun deleteSynced()

    @Query("DELETE FROM saving_goals")
    suspend fun clear()

    @Transaction
    suspend fun replaceSynced(remote: List<SavingGoalEntity>) {
        val pendingIds = (getByState(SyncState.PENDING_UPSERT) + getByState(SyncState.PENDING_DELETE))
            .map { it.id }
            .toSet()
        deleteSynced()
        upsertAll(remote.filter { it.id !in pendingIds })
    }
}

package com.example.gastou.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE syncState != ${SyncState.PENDING_DELETE} ORDER BY dateEpochDay DESC, updatedAt DESC")
    fun observeActive(): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE syncState != ${SyncState.PENDING_DELETE} " +
            "AND dateEpochDay >= :fromEpochDay AND dateEpochDay < :toEpochDay " +
            "ORDER BY dateEpochDay DESC, updatedAt DESC"
    )
    fun observeBetween(fromEpochDay: Long, toEpochDay: Long): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE syncState != ${SyncState.PENDING_DELETE} " +
            "AND dateEpochDay >= :fromEpochDay AND dateEpochDay < :toEpochDay"
    )
    suspend fun totalBetween(fromEpochDay: Long, toEpochDay: Long): Double

    @Query("SELECT COUNT(*) FROM expenses WHERE syncState != ${SyncState.SYNCED}")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM expenses WHERE syncState = :state")
    suspend fun getByState(state: Int): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: String): ExpenseEntity?

    @Upsert
    suspend fun upsert(expense: ExpenseEntity)

    @Upsert
    suspend fun upsertAll(expenses: List<ExpenseEntity>)

    @Query("UPDATE expenses SET syncState = :state WHERE id = :id")
    suspend fun updateState(id: String, state: Int)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM expenses WHERE syncState = ${SyncState.SYNCED}")
    suspend fun deleteSynced()

    @Query("DELETE FROM expenses")
    suspend fun clear()

    /**
     * Reemplaza los registros ya sincronizados por los que mandó el servidor,
     * sin tocar los cambios locales que aún no se han enviado.
     */
    @Transaction
    suspend fun replaceSynced(remote: List<ExpenseEntity>) {
        val pendingIds = (getByState(SyncState.PENDING_UPSERT) + getByState(SyncState.PENDING_DELETE))
            .map { it.id }
            .toSet()
        deleteSynced()
        upsertAll(remote.filter { it.id !in pendingIds })
    }
}

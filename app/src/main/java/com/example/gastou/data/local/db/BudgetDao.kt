package com.example.gastou.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT amount FROM budgets WHERE month = :month")
    fun observeAmount(month: String): Flow<Double?>

    @Query("SELECT amount FROM budgets WHERE month = :month")
    suspend fun getAmount(month: String): Double?

    @Query("SELECT * FROM budgets WHERE syncState = ${SyncState.PENDING_UPSERT}")
    suspend fun getPending(): List<BudgetEntity>

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Upsert
    suspend fun upsertAll(budgets: List<BudgetEntity>)

    @Query("UPDATE budgets SET syncState = :state WHERE month = :month")
    suspend fun updateState(month: String, state: Int)

    @Query("DELETE FROM budgets WHERE syncState = ${SyncState.SYNCED}")
    suspend fun deleteSynced()

    @Query("DELETE FROM budgets")
    suspend fun clear()

    @Transaction
    suspend fun replaceSynced(remote: List<BudgetEntity>) {
        val pendingMonths = getPending().map { it.month }.toSet()
        deleteSynced()
        upsertAll(remote.filter { it.month !in pendingMonths })
    }
}

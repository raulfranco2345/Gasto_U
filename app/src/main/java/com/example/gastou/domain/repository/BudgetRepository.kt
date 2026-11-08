package com.example.gastou.domain.repository

import com.example.gastou.domain.model.SyncOutcome
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/** Contrato de presupuestos mensuales (Room + API). */
interface BudgetRepository {
    fun observeBudget(month: YearMonth): Flow<Double?>

    suspend fun setBudget(month: YearMonth, amount: Double): SyncOutcome
    suspend fun budgetOf(month: YearMonth): Double?
    suspend fun sync(): SyncOutcome
}

package com.example.gastou.domain.repository

import com.example.gastou.domain.model.Expense
import com.example.gastou.domain.model.MonthlySummary
import com.example.gastou.domain.model.SyncOutcome
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/** Contrato de gastos. La implementación coordina Room (fuente de verdad) y la API. */
interface ExpenseRepository {
    fun observeExpenses(): Flow<List<Expense>>
    fun observeExpensesOfMonth(month: YearMonth): Flow<List<Expense>>
    fun observePendingCount(): Flow<Int>

    suspend fun addExpense(expense: Expense): SyncOutcome
    suspend fun deleteExpense(expenseId: String): SyncOutcome

    /** Envía los cambios pendientes y descarga los gastos del servidor. */
    suspend fun sync(): SyncOutcome

    /** Consulta adicional a la API: total y gasto por categoría calculados en el servidor. */
    suspend fun fetchMonthlySummary(month: YearMonth): Result<MonthlySummary>

    /** Total gastado en el mes (lo usa el widget). */
    suspend fun totalOfMonth(month: YearMonth): Double
}

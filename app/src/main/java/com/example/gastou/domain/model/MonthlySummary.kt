package com.example.gastou.domain.model

import java.time.YearMonth

/** Resumen calculado por el servidor (GET /api/expenses/summary). */
data class MonthlySummary(
    val month: YearMonth,
    val total: Double,
    val budget: Double?,
    val byCategory: List<CategoryTotal>
)

data class CategoryTotal(
    val category: String,
    val total: Double,
    val count: Int
)

/** Datos que necesita el generador de PDF para el estado de cuenta mensual. */
data class MonthlyReport(
    val month: YearMonth,
    val monthlyBudget: Double?,
    val totalSpent: Double,
    val expenses: List<Expense>,
    val savingGoals: List<SavingGoal>,
    val ownerName: String?
)

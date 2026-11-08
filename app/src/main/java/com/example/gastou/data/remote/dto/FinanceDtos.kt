package com.example.gastou.data.remote.dto

/* Objetos de petición y respuesta de /api/expenses, /api/goals y /api/budgets */

data class ExpenseDto(
    val clientId: String,
    val amount: Double,
    val category: String,
    val date: String,          // AAAA-MM-DD
    val displayDate: String?,
    val paymentMethod: String,
    val note: String?
)

data class ExpenseRequestDto(
    val amount: Double,
    val category: String,
    val date: String,
    val displayDate: String,
    val paymentMethod: String,
    val note: String
)

data class GoalDto(
    val clientId: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val category: String,
    val targetDate: String?,
    val isCompleted: Boolean
)

data class GoalRequestDto(
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val category: String,
    val targetDate: String?,
    val isCompleted: Boolean
)

data class BudgetDto(
    val month: String,         // AAAA-MM
    val amount: Double
)

data class BudgetRequestDto(
    val amount: Double
)

data class CategoryTotalDto(
    val category: String,
    val total: Double,
    val count: Int
)

data class MonthlySummaryDto(
    val month: String,
    val total: Double,
    val budget: Double?,
    val byCategory: List<CategoryTotalDto>
)

package com.example.gastou.domain.model

import java.util.UUID

/** Meta de ahorro con monto objetivo y lo que se ha abonado. */
data class SavingGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val category: String,
    val targetDate: String?,
    val isCompleted: Boolean = false
) {
    val progress: Float
        get() = if (targetAmount <= 0) 0f else (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
}

package com.example.gastou.domain.usecase

import com.example.gastou.domain.model.SyncOutcome
import com.example.gastou.domain.repository.BudgetRepository
import com.example.gastou.domain.repository.ExpenseRepository
import com.example.gastou.domain.repository.GoalRepository

/**
 * Regla de negocio: sincronizar todo lo del usuario con la API en un solo paso
 * (presupuestos, metas y gastos). Lo usan el inicio y el botón "Reintentar".
 */
class SyncAllDataUseCase(
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(): SyncOutcome {
        val results = listOf(
            budgetRepository.sync(),
            goalRepository.sync(),
            expenseRepository.sync()
        )
        return results.filterIsInstance<SyncOutcome.Failed>().firstOrNull() ?: SyncOutcome.Synced
    }
}

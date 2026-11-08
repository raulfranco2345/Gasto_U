package com.example.gastou.domain.usecase

import com.example.gastou.domain.model.Expense
import com.example.gastou.domain.model.SavingGoal
import com.example.gastou.domain.model.SyncOutcome
import com.example.gastou.domain.repository.ExpenseRepository
import com.example.gastou.domain.repository.GoalRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Regla de negocio: abonar a una meta suma el monto a la meta y además registra
 * el movimiento como gasto de la categoría "Ahorro e Inversión" en el historial.
 */
class AddFundsToGoalUseCase(
    private val goalRepository: GoalRepository,
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(goal: SavingGoal, amount: Double, paymentMethod: String): SyncOutcome {
        val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amount)
        val goalOutcome = goalRepository.saveGoal(updatedGoal)

        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-MX"))
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        val expense = Expense(
            amount = amount,
            categoryId = "Ahorro e Inversión",
            date = "Hoy (${today.format(dateFormatter)}) • ${LocalTime.now().format(timeFormatter)}",
            paymentMethod = paymentMethod,
            note = "Abono a meta: ${goal.name}",
            dateObj = today
        )
        val expenseOutcome = expenseRepository.addExpense(expense)

        return listOf(goalOutcome, expenseOutcome).firstOrNull { it !is SyncOutcome.Synced } ?: SyncOutcome.Synced
    }
}

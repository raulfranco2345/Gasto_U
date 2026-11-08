package com.example.gastou.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastou.domain.model.Expense
import com.example.gastou.domain.model.SyncOutcome
import com.example.gastou.domain.repository.BudgetRepository
import com.example.gastou.domain.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

/**
 * Estado que representa TODO lo que necesitan las pantallas de Historial y Detalle de Gasto
 */
data class HistoryUiState(
    val expenses: List<Expense> = emptyList(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val monthlyBudget: Double? = null,
    val selectedExpenseId: String? = null,
    val message: String? = null
) {
    // Calculamos el gasto seleccionado dinámicamente
    val selectedExpense: Expense?
        get() = expenses.find { it.id == selectedExpenseId }

    // Calculamos el total gastado en la categoría del gasto seleccionado
    val selectedExpenseCategoryTotal: Double
        get() = selectedExpense?.let { exp ->
            expenses.filter { it.categoryId == exp.categoryId }.sumOf { it.amount }
        } ?: 0.0
}

/**
 * ViewModel para el historial de gastos y sus detalles
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val selectedMonth: MutableStateFlow<YearMonth>
) : ViewModel() {

    // Estado local para saber qué gasto estamos viendo en detalle
    private val _selectedExpenseId = MutableStateFlow<String?>(null)
    private val _message = MutableStateFlow<String?>(null)

    private val monthData = selectedMonth.flatMapLatest { month ->
        combine(
            expenseRepository.observeExpensesOfMonth(month),
            budgetRepository.observeBudget(month)
        ) { expenses, budget -> Triple(month, expenses, budget) }
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        monthData,
        _selectedExpenseId,
        _message
    ) { (month, expenses, budget), expenseId, message ->
        HistoryUiState(
            expenses = expenses,
            selectedMonth = month,
            monthlyBudget = budget,
            selectedExpenseId = expenseId,
            message = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    // Eventos que la UI puede lanzar
    fun selectMonth(month: YearMonth) {
        selectedMonth.value = month
    }

    fun selectExpenseForDetail(expenseId: String?) {
        _selectedExpenseId.value = expenseId
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            val outcome = expenseRepository.deleteExpense(expenseId)
            _message.value = when (outcome) {
                SyncOutcome.Synced -> "Gasto eliminado"
                is SyncOutcome.SavedOffline -> outcome.message
                is SyncOutcome.Failed -> outcome.message
            }
        }
        // Limpiamos la selección si lo acabamos de eliminar
        if (_selectedExpenseId.value == expenseId) {
            _selectedExpenseId.value = null
        }
    }

    fun onMessageShown() {
        _message.value = null
    }
}

package com.example.gastou.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastou.core.common.Validators
import com.example.gastou.domain.model.SyncOutcome
import com.example.gastou.domain.repository.BudgetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class BudgetUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val currentBudget: Double? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null
)

private data class BudgetOperation(
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null
)

/** ViewModel de "Modificar presupuesto": lee y guarda el presupuesto del mes elegido. */
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val selectedMonth: MutableStateFlow<YearMonth>
) : ViewModel() {

    private val operation = MutableStateFlow(BudgetOperation())

    val uiState: StateFlow<BudgetUiState> = combine(
        selectedMonth.flatMapLatest { month ->
            budgetRepository.observeBudget(month).map { month to it }
        },
        operation
    ) { (month, budget), op ->
        BudgetUiState(
            selectedMonth = month,
            currentBudget = budget,
            isSaving = op.isSaving,
            isSaved = op.isSaved,
            message = op.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetUiState())

    fun selectMonth(month: YearMonth) {
        selectedMonth.value = month
    }

    fun saveBudget(amount: Double?) {
        if (operation.value.isSaving) return
        val error = when {
            amount == null -> "Escribe un monto"
            amount <= 0.0 -> "El presupuesto debe ser mayor a 0"
            amount > Validators.MAX_AMOUNT -> "El presupuesto es demasiado grande"
            else -> null
        }
        if (error != null || amount == null) {
            operation.value = BudgetOperation(message = error)
            return
        }
        operation.value = BudgetOperation(isSaving = true)
        viewModelScope.launch {
            val message = when (val outcome = budgetRepository.setBudget(selectedMonth.value, amount)) {
                SyncOutcome.Synced -> "Presupuesto actualizado"
                is SyncOutcome.SavedOffline -> outcome.message
                is SyncOutcome.Failed -> outcome.message
            }
            operation.value = BudgetOperation(isSaved = true, message = message)
        }
    }

    fun onEventsHandled() {
        operation.value = BudgetOperation()
    }
}

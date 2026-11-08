package com.example.gastou.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastou.domain.model.Expense
import com.example.gastou.domain.model.MonthlyReport
import com.example.gastou.domain.model.SavingGoal
import com.example.gastou.domain.model.SyncOutcome
import com.example.gastou.domain.repository.AuthRepository
import com.example.gastou.domain.repository.BudgetRepository
import com.example.gastou.domain.repository.ExpenseRepository
import com.example.gastou.domain.repository.GoalRepository
import com.example.gastou.domain.usecase.SyncAllDataUseCase
import com.example.gastou.integration.pdf.PdfReportGenerator
import com.example.gastou.integration.pdf.PdfResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

/**
 * Estado que representa TODO lo que necesita pintar el HomeScreen
 */
data class HomeUiState(
    val monthlyBudget: Double? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val expenses: List<Expense> = emptyList(),
    val savingGoals: List<SavingGoal> = emptyList(),
    val totalSpent: Double = 0.0,
    val userName: String = "",
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val pendingChanges: Int = 0,
    val isGeneratingPdf: Boolean = false,
    val pdfResult: PdfResult? = null,
    val message: String? = null
)

/** Estado interno de las operaciones que lanza la pantalla (sincronizar, PDF, mensajes). */
private data class HomeOperations(
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val isGeneratingPdf: Boolean = false,
    val pdfResult: PdfResult? = null,
    val message: String? = null
)

/**
 * ViewModel del Inicio. Observa Room (gastos, metas, presupuesto) a través de los repositorios
 * y, al abrirse, sincroniza con la API mostrando carga, error y opción de reintento.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val budgetRepository: BudgetRepository,
    private val authRepository: AuthRepository,
    private val syncAllData: SyncAllDataUseCase,
    private val pdfReportGenerator: PdfReportGenerator,
    private val selectedMonth: MutableStateFlow<YearMonth>
) : ViewModel() {

    private val operations = MutableStateFlow(HomeOperations())

    // Gastos y presupuesto del mes elegido; cambia automáticamente cuando el usuario elige otro mes
    private val monthData = selectedMonth.flatMapLatest { month ->
        combine(
            expenseRepository.observeExpensesOfMonth(month),
            budgetRepository.observeBudget(month)
        ) { expenses, budget -> Triple(month, expenses, budget) }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        monthData,
        goalRepository.observeGoals(),
        authRepository.session,
        expenseRepository.observePendingCount(),
        operations
    ) { (month, expenses, budget), goals, session, pending, ops ->
        HomeUiState(
            monthlyBudget = budget,
            selectedMonth = month,
            expenses = expenses,
            savingGoals = goals,
            totalSpent = expenses.sumOf { it.amount },
            userName = session?.user?.nombre.orEmpty(),
            isSyncing = ops.isSyncing,
            syncError = ops.syncError,
            pendingChanges = pending,
            isGeneratingPdf = ops.isGeneratingPdf,
            pdfResult = ops.pdfResult,
            message = ops.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        sync()
    }

    // Eventos que la UI puede lanzar
    fun selectMonth(month: YearMonth) {
        selectedMonth.value = month
    }

    /** Sincroniza con la API. También es la acción del botón "Reintentar". */
    fun sync() {
        if (operations.value.isSyncing) return
        operations.update { it.copy(isSyncing = true, syncError = null) }
        viewModelScope.launch {
            val outcome = syncAllData()
            operations.update {
                it.copy(
                    isSyncing = false,
                    syncError = (outcome as? SyncOutcome.Failed)?.message
                )
            }
        }
    }

    /** Genera el estado de cuenta del mes en PDF (fuera del hilo principal). */
    fun exportPdf() {
        val state = uiState.value
        if (state.isGeneratingPdf) return
        operations.update { it.copy(isGeneratingPdf = true) }
        viewModelScope.launch {
            val report = MonthlyReport(
                month = state.selectedMonth,
                monthlyBudget = state.monthlyBudget,
                totalSpent = state.totalSpent,
                expenses = state.expenses,
                savingGoals = state.savingGoals,
                ownerName = state.userName
            )
            val result = withContext(Dispatchers.IO) { pdfReportGenerator.generate(report) }
            operations.update {
                it.copy(
                    isGeneratingPdf = false,
                    pdfResult = result,
                    message = when {
                        result == null -> "No se pudo generar el PDF. Intenta de nuevo."
                        result.savedInDownloads -> "PDF guardado en Descargas/GastoU"
                        else -> null
                    }
                )
            }
        }
    }

    fun onPdfHandled() = operations.update { it.copy(pdfResult = null) }

    fun onMessageShown() = operations.update { it.copy(message = null) }
}

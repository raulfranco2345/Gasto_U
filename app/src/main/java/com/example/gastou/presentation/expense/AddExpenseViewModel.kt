package com.example.gastou.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastou.core.common.Validators
import com.example.gastou.domain.model.Expense
import com.example.gastou.domain.model.SyncOutcome
import com.example.gastou.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Estado que representa el formulario de "Añadir Gasto"
 */
data class AddExpenseUiState(
    val amount: String = "",
    val note: String = "",
    val category: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.now(),
    val paymentMethod: String = "Tarjeta prepago U",
    val receiptUri: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    /** Mensaje para el usuario después de guardar (por ejemplo, guardado sin conexión). */
    val savedMessage: String? = null,
    val isSaved: Boolean = false
) {
    // Calculamos el texto de fecha formateado directamente en el estado
    val displayDate: String
        get() {
            val today = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-MX"))
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
            val formattedDate = selectedDate.format(dateFormatter)
            val formattedTime = selectedTime.format(timeFormatter)

            val dateStr = when (selectedDate) {
                today -> "Hoy ($formattedDate)"
                today.minusDays(1) -> "Ayer ($formattedDate)"
                else -> formattedDate
            }
            return "$dateStr • $formattedTime"
        }
}

/**
 * ViewModel que maneja toda la lógica de crear un nuevo gasto:
 * valida el formulario y delega el guardado al repositorio (Room + API).
 */
class AddExpenseViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun updateAmount(newAmount: String) {
        _uiState.update { it.copy(amount = Validators.sanitizeAmountInput(newAmount), errorMessage = null) }
    }

    fun updateNote(newNote: String) {
        _uiState.update { it.copy(note = newNote.take(300)) }
    }

    fun updateCategory(newCategory: String) {
        _uiState.update { it.copy(category = newCategory, errorMessage = null) }
    }

    fun updateDateTime(date: LocalDate, time: LocalTime) {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(errorMessage = "No puedes registrar gastos con fecha futura") }
            return
        }
        _uiState.update { it.copy(selectedDate = date, selectedTime = time, errorMessage = null) }
    }

    fun updatePaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun updateReceiptUri(uri: String) {
        _uiState.update { it.copy(receiptUri = uri) }
    }

    /** Valida y guarda el gasto. El resultado se refleja en isSaved / errorMessage. */
    fun saveExpense() {
        val state = _uiState.value
        if (state.isSaving) return

        val amountError = Validators.amount(state.amount)
        val error = amountError ?: if (state.category == null) "Selecciona una categoría" else null
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        val expense = Expense(
            amount = Validators.parseAmount(state.amount)!!,
            categoryId = state.category!!,
            date = state.displayDate,
            paymentMethod = state.paymentMethod,
            note = state.note.trim(),
            receiptUri = state.receiptUri,
            dateObj = state.selectedDate
        )

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val message = when (val outcome = expenseRepository.addExpense(expense)) {
                SyncOutcome.Synced -> "Gasto registrado"
                is SyncOutcome.SavedOffline -> outcome.message
                is SyncOutcome.Failed -> "Gasto guardado en el teléfono. ${outcome.message}"
            }
            // El formulario se reinicia y se marca como guardado para que la navegación regrese
            _uiState.value = AddExpenseUiState(isSaved = true, savedMessage = message)
        }
    }

    /** La pantalla ya navegó después de guardar: se limpia el evento. */
    fun onSavedHandled() {
        _uiState.update { it.copy(isSaved = false, savedMessage = null) }
    }

    // Método extra para reiniciar manualmente si el usuario cancela
    fun resetForm() {
        _uiState.value = AddExpenseUiState()
    }
}

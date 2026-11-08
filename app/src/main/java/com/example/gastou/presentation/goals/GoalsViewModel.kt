package com.example.gastou.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastou.domain.model.SavingGoal
import com.example.gastou.domain.model.SyncOutcome
import com.example.gastou.domain.repository.GoalRepository
import com.example.gastou.domain.usecase.AddFundsToGoalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado que representa las pantallas de Metas de Ahorro
 */
data class GoalsUiState(
    val savingGoals: List<SavingGoal> = emptyList(),
    val selectedGoalId: String? = null,
    val message: String? = null
) {
    // Calculamos la meta seleccionada dinámicamente
    val selectedGoal: SavingGoal?
        get() = savingGoals.find { it.id == selectedGoalId }
}

class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val addFundsToGoal: AddFundsToGoalUseCase
) : ViewModel() {

    private val _selectedGoalId = MutableStateFlow<String?>(null)
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.observeGoals(),
        _selectedGoalId,
        _message
    ) { goalsList, goalId, message ->
        GoalsUiState(
            savingGoals = goalsList,
            selectedGoalId = goalId,
            message = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsUiState()
    )

    fun selectGoal(goalId: String?) {
        _selectedGoalId.value = goalId
    }

    /** Devuelve false si la meta no es válida (la pantalla se queda abierta). */
    fun saveNewGoal(goal: SavingGoal): Boolean {
        val error = when {
            goal.name.isBlank() -> "Escribe un nombre para la meta"
            goal.name.length > 80 -> "El nombre es demasiado largo"
            goal.targetAmount <= 0.0 -> "El monto objetivo debe ser mayor a 0"
            goal.targetAmount > 10_000_000 -> "El monto objetivo es demasiado grande"
            else -> null
        }
        if (error != null) {
            _message.value = error
            return false
        }
        viewModelScope.launch {
            _message.value = goalRepository.saveGoal(goal.copy(name = goal.name.trim())).toMessage("Meta creada")
        }
        return true
    }

    fun addFundsToGoal(amountToAdd: Double, paymentMethod: String) {
        val currentGoal = uiState.value.selectedGoal ?: return
        if (amountToAdd <= 0.0) {
            _message.value = "El abono debe ser mayor a 0"
            return
        }
        viewModelScope.launch {
            _message.value = addFundsToGoal(currentGoal, amountToAdd, paymentMethod).toMessage("Abono registrado")
        }
    }

    fun markGoalCompleted() {
        val currentGoal = uiState.value.selectedGoal ?: return
        viewModelScope.launch {
            _message.value = goalRepository.saveGoal(currentGoal.copy(isCompleted = true))
                .toMessage("¡Meta completada!")
        }
    }

    fun deleteGoal() {
        val goalId = _selectedGoalId.value ?: return
        _selectedGoalId.value = null
        viewModelScope.launch {
            _message.value = goalRepository.deleteGoal(goalId).toMessage("Meta eliminada")
        }
    }

    fun onMessageShown() {
        _message.value = null
    }

    private fun SyncOutcome.toMessage(success: String): String = when (this) {
        SyncOutcome.Synced -> success
        is SyncOutcome.SavedOffline -> message
        is SyncOutcome.Failed -> message
    }
}

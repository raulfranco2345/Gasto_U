package com.example.gastou.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastou.core.common.Validators
import com.example.gastou.domain.model.MonthlySummary
import com.example.gastou.domain.model.User
import com.example.gastou.domain.repository.AuthRepository
import com.example.gastou.domain.repository.ExpenseRepository
import com.example.gastou.domain.repository.GoalRepository
import com.example.gastou.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

/** Estado del resumen que se consulta a la API (carga, contenido, vacío o error). */
sealed interface SummaryState {
    data object Loading : SummaryState
    data class Content(val summary: MonthlySummary) : SummaryState
    data class Error(val message: String) : SummaryState
}

data class ProfileUiState(
    val user: User? = null,
    val biometricEnabled: Boolean = false,
    val totalExpenses: Int = 0,
    val activeGoals: Int = 0,
    val completedGoals: Int = 0,
    val summary: SummaryState = SummaryState.Loading,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editUniversity: String = "",
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val message: String? = null,
    val isLoggedOut: Boolean = false
)

private data class ProfileForm(
    val summary: SummaryState = SummaryState.Loading,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editUniversity: String = "",
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val message: String? = null,
    val isLoggedOut: Boolean = false
)

/**
 * Perfil: datos del usuario (API + DataStore), estadísticas locales (Room),
 * resumen del mes calculado por el servidor y preferencia de biometría.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val expenseRepository: ExpenseRepository,
    goalRepository: GoalRepository
) : ViewModel() {

    private val form = MutableStateFlow(ProfileForm())

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.session,
        settingsRepository.biometricEnabled,
        expenseRepository.observeExpenses(),
        goalRepository.observeGoals(),
        form
    ) { session, biometric, expenses, goals, f ->
        ProfileUiState(
            user = session?.user,
            biometricEnabled = biometric,
            totalExpenses = expenses.size,
            activeGoals = goals.count { !it.isCompleted },
            completedGoals = goals.count { it.isCompleted },
            summary = f.summary,
            isEditing = f.isEditing,
            editName = f.editName,
            editUniversity = f.editUniversity,
            nameError = f.nameError,
            isSaving = f.isSaving,
            message = f.message,
            isLoggedOut = f.isLoggedOut
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    init {
        refresh()
    }

    /** Actualiza el perfil y el resumen del mes desde la API. */
    fun refresh() {
        form.update { it.copy(summary = SummaryState.Loading) }
        viewModelScope.launch {
            authRepository.refreshProfile() // si falla se siguen mostrando los datos guardados en DataStore
            val result = expenseRepository.fetchMonthlySummary(YearMonth.now())
            form.update { f ->
                f.copy(
                    summary = result.fold(
                        onSuccess = { SummaryState.Content(it) },
                        onFailure = { SummaryState.Error(it.message ?: "No se pudo cargar el resumen") }
                    )
                )
            }
        }
    }

    fun startEditing() {
        val user = uiState.value.user ?: return
        form.update {
            it.copy(isEditing = true, editName = user.nombre, editUniversity = user.universidad, nameError = null)
        }
    }

    fun cancelEditing() = form.update { it.copy(isEditing = false, nameError = null) }

    fun onEditNameChange(value: String) = form.update { it.copy(editName = value, nameError = null) }

    fun onEditUniversityChange(value: String) = form.update { it.copy(editUniversity = value.take(120)) }

    fun saveProfile() {
        val f = form.value
        val nameError = Validators.name(f.editName)
        if (nameError != null) {
            form.update { it.copy(nameError = nameError) }
            return
        }
        form.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            authRepository.updateProfile(f.editName, f.editUniversity)
                .onSuccess { form.update { it.copy(isSaving = false, isEditing = false, message = "Perfil actualizado") } }
                .onFailure { e -> form.update { it.copy(isSaving = false, message = e.message) } }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricEnabled(enabled)
            form.update {
                it.copy(message = if (enabled) "Bloqueo con huella activado" else "Bloqueo con huella desactivado")
            }
        }
    }

    fun showMessage(message: String) = form.update { it.copy(message = message) }

    fun onMessageShown() = form.update { it.copy(message = null) }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            form.update { it.copy(isLoggedOut = true) }
        }
    }
}

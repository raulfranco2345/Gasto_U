package com.example.gastou.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastou.core.common.Validators
import com.example.gastou.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val termsError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, nameError = null, errorMessage = null) }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value.trim(), emailError = null, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }

    fun onConfirmChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmError = null, errorMessage = null) }

    fun onTermsChange(value: Boolean) =
        _uiState.update { it.copy(acceptedTerms = value, termsError = null) }

    fun register() {
        val s = _uiState.value
        if (s.isLoading) return
        val validated = s.copy(
            nameError = Validators.name(s.name),
            emailError = Validators.email(s.email),
            passwordError = Validators.password(s.password),
            confirmError = Validators.passwordConfirmation(s.password, s.confirmPassword),
            termsError = if (!s.acceptedTerms) "Debes aceptar los términos para continuar" else null
        )
        val hasErrors = listOf(
            validated.nameError, validated.emailError, validated.passwordError,
            validated.confirmError, validated.termsError
        ).any { it != null }
        if (hasErrors) {
            _uiState.value = validated
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.register(s.name, s.email, s.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isRegistered = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }
}

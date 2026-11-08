package com.example.gastou.presentation.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastou.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel de la pantalla de bloqueo biométrico. */
class LockViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val userName: StateFlow<String> = authRepository.session
        .map { it?.user?.nombre.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun onAuthError(message: String) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
    }

    /** "Usar otra cuenta": cierra la sesión actual. */
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}

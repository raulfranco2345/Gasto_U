package com.example.gastou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastou.domain.repository.AuthRepository
import com.example.gastou.domain.repository.SettingsRepository
import com.example.gastou.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Decide la pantalla inicial según la sesión guardada en DataStore:
 * sin sesión -> Login; con sesión y biometría activa -> Bloqueo; con sesión -> Inicio.
 */
class MainViewModel(
    authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    /** null mientras carga; false cuando la sesión se cierra (logout o token vencido). */
    val isLoggedIn: StateFlow<Boolean?> = authRepository.session
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            val session = authRepository.session.first()
            val biometric = settingsRepository.biometricEnabled.first()
            _startDestination.value = when {
                session == null -> Routes.LOGIN
                biometric -> Routes.LOCK
                else -> Routes.HOME
            }
        }
    }
}

package com.example.gastou.domain.repository

import kotlinx.coroutines.flow.Flow

/** Preferencias del usuario guardadas en DataStore. */
interface SettingsRepository {
    val biometricEnabled: Flow<Boolean>
    suspend fun setBiometricEnabled(enabled: Boolean)
}

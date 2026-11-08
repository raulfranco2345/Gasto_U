package com.example.gastou.data.repository

import com.example.gastou.data.local.preferences.PreferencesDataSource
import com.example.gastou.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val preferences: PreferencesDataSource
) : SettingsRepository {

    override val biometricEnabled: Flow<Boolean> = preferences.biometricEnabled

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        preferences.setBiometricEnabled(enabled)
    }
}

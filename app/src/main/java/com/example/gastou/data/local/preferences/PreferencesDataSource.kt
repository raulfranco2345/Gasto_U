package com.example.gastou.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gastou.domain.model.User
import com.example.gastou.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

// Una sola instancia de DataStore para toda la app
private val Context.gastoUDataStore: DataStore<Preferences> by preferencesDataStore(name = "gastou_preferences")

/**
 * DataStore guarda datos pequeños de tipo clave-valor: la sesión (token y datos del usuario)
 * y preferencias como la biometría. Room se reserva para los registros (gastos, metas, presupuestos).
 */
class PreferencesDataSource(context: Context) {

    private val dataStore = context.applicationContext.gastoUDataStore

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = stringPreferencesKey("user_id")
        val NOMBRE = stringPreferencesKey("nombre")
        val EMAIL = stringPreferencesKey("email")
        val UNIVERSIDAD = stringPreferencesKey("universidad")
        val CREATED_AT = stringPreferencesKey("created_at")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LAST_SYNC = longPreferencesKey("last_sync")
    }

    private val safeData: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val session: Flow<UserSession?> = safeData.map { prefs ->
        val token = prefs[Keys.TOKEN] ?: return@map null
        UserSession(
            token = token,
            user = User(
                id = prefs[Keys.USER_ID].orEmpty(),
                nombre = prefs[Keys.NOMBRE].orEmpty(),
                email = prefs[Keys.EMAIL].orEmpty(),
                universidad = prefs[Keys.UNIVERSIDAD].orEmpty(),
                createdAt = prefs[Keys.CREATED_AT].orEmpty()
            )
        )
    }

    val biometricEnabled: Flow<Boolean> = safeData.map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    val lastSync: Flow<Long?> = safeData.map { it[Keys.LAST_SYNC] }

    suspend fun currentToken(): String? = session.first()?.token

    suspend fun saveSession(session: UserSession) {
        dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = session.token
            writeUser(prefs, session.user)
        }
    }

    suspend fun updateUser(user: User) {
        dataStore.edit { prefs -> writeUser(prefs, user) }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setLastSync(timestamp: Long) {
        dataStore.edit { it[Keys.LAST_SYNC] = timestamp }
    }

    /** Borra la sesión y las preferencias del usuario al cerrar sesión. */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun writeUser(prefs: androidx.datastore.preferences.core.MutablePreferences, user: User) {
        prefs[Keys.USER_ID] = user.id
        prefs[Keys.NOMBRE] = user.nombre
        prefs[Keys.EMAIL] = user.email
        prefs[Keys.UNIVERSIDAD] = user.universidad
        prefs[Keys.CREATED_AT] = user.createdAt
    }
}

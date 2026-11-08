package com.example.gastou.data.repository

import com.example.gastou.core.common.ErrorMapper
import com.example.gastou.data.local.db.GastoUDatabase
import com.example.gastou.data.local.preferences.PreferencesDataSource
import com.example.gastou.data.mapper.toDomain
import com.example.gastou.data.remote.api.GastoUApi
import com.example.gastou.data.remote.dto.LoginRequestDto
import com.example.gastou.data.remote.dto.RegisterRequestDto
import com.example.gastou.data.remote.dto.UpdateProfileRequestDto
import com.example.gastou.domain.model.User
import com.example.gastou.domain.model.UserSession
import com.example.gastou.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * Autenticación contra la API propia. El token JWT y los datos del usuario se guardan
 * en DataStore para que la sesión siga abierta al reiniciar la app.
 */
class AuthRepositoryImpl(
    private val api: GastoUApi,
    private val preferences: PreferencesDataSource,
    private val database: GastoUDatabase,
    private val io: CoroutineDispatcher
) : AuthRepository {

    override val session: Flow<UserSession?> = preferences.session

    override suspend fun login(email: String, password: String): Result<User> = withContext(io) {
        runCatching {
            val response = api.login(LoginRequestDto(email.trim().lowercase(), password))
            startSession(response.token, response.user.toDomain())
        }.mapError()
    }

    override suspend fun register(nombre: String, email: String, password: String): Result<User> =
        withContext(io) {
            runCatching {
                val response = api.register(
                    RegisterRequestDto(nombre.trim(), email.trim().lowercase(), password)
                )
                startSession(response.token, response.user.toDomain())
            }.mapError()
        }

    override suspend fun refreshProfile(): Result<User> = withContext(io) {
        runCatching {
            val user = api.getProfile().toDomain()
            preferences.updateUser(user)
            user
        }.mapError()
    }

    override suspend fun updateProfile(nombre: String, universidad: String): Result<User> =
        withContext(io) {
            runCatching {
                val user = api.updateProfile(UpdateProfileRequestDto(nombre.trim(), universidad.trim())).toDomain()
                preferences.updateUser(user)
                user
            }.mapError()
        }

    override suspend fun logout() = withContext(io) {
        // Se borran los datos locales para que otro usuario no vea la información anterior
        database.clearAllTables()
        preferences.clear()
    }

    private suspend fun startSession(token: String, user: User): User {
        // Si antes había otra cuenta en el teléfono, se limpian sus datos locales
        val previous = preferences.session.firstOrNull()
        if (previous != null && previous.user.id != user.id) {
            database.clearAllTables()
        }
        preferences.saveSession(UserSession(token, user))
        return user
    }

    private fun <T> Result<T>.mapError(): Result<T> =
        recoverCatching { throw Exception(ErrorMapper.toUserMessage(it), it) }
}

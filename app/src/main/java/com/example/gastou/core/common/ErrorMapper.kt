package com.example.gastou.core.common

import com.example.gastou.data.remote.dto.ApiErrorDto
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Convierte excepciones técnicas (red, HTTP, JSON) en mensajes comprensibles para el usuario.
 */
object ErrorMapper {

    private val gson = Gson()

    fun toUserMessage(error: Throwable): String = when (error) {
        is SocketTimeoutException ->
            "El servidor tardó demasiado en responder. Intenta de nuevo en unos segundos."
        is UnknownHostException, is IOException ->
            "Sin conexión con el servidor. Revisa tu internet e inténtalo de nuevo."
        is HttpException -> httpMessage(error)
        else -> error.message ?: "Ocurrió un error inesperado."
    }

    /** true si el error es de conectividad (la operación puede quedar pendiente de sincronizar). */
    fun isNetworkError(error: Throwable): Boolean =
        error is IOException || (error is HttpException && error.code() >= 500)

    private fun httpMessage(error: HttpException): String {
        val serverMessage = try {
            error.response()?.errorBody()?.string()?.let {
                gson.fromJson(it, ApiErrorDto::class.java)?.message
            }
        } catch (_: Exception) {
            null
        }
        if (!serverMessage.isNullOrBlank()) return serverMessage
        return when (error.code()) {
            400 -> "Los datos enviados no son válidos."
            401 -> "Tu sesión expiró. Inicia sesión de nuevo."
            404 -> "No se encontró la información solicitada."
            409 -> "El registro ya existe."
            in 500..599 -> "El servidor tuvo un problema. Intenta más tarde."
            else -> "Error de comunicación con el servidor (${error.code()})."
        }
    }
}

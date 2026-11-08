package com.example.gastou.data.remote.api

import com.example.gastou.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Construye el cliente Retrofit. La URL base viene de BuildConfig.API_BASE_URL,
 * que se define en gradle.properties (GASTOU_API_URL).
 */
object ApiClient {

    fun create(
        tokenProvider: suspend () -> String?,
        onUnauthorized: () -> Unit
    ): GastoUApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider, onUnauthorized))
            .addInterceptor(logging)
            // Render (plan gratuito) puede tardar ~50 s en despertar el servicio
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GastoUApi::class.java)
    }
}

/**
 * Agrega "Authorization: Bearer <token>" a cada petición y avisa cuando la API
 * responde 401 (token vencido) para cerrar la sesión.
 */
class AuthInterceptor(
    private val tokenProvider: suspend () -> String?,
    private val onUnauthorized: () -> Unit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val isAuthEndpoint = original.url.encodedPath.contains("/api/auth/login") ||
            original.url.encodedPath.contains("/api/auth/register")

        // OkHttp ejecuta los interceptores en un hilo de fondo, por eso aquí es seguro bloquear
        val token = if (isAuthEndpoint) null else kotlinx.coroutines.runBlocking { tokenProvider() }
        val request = if (token != null) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            original
        }

        val response = chain.proceed(request)
        if (response.code == 401 && !isAuthEndpoint && token != null) {
            onUnauthorized()
        }
        return response
    }
}

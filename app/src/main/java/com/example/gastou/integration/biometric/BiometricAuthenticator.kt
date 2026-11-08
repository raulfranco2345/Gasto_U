package com.example.gastou.integration.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Disponibilidad de la autenticación en el dispositivo. */
sealed interface BiometricAvailability {
    data object Available : BiometricAvailability
    data class Unavailable(val reason: String) : BiometricAvailability
}

/**
 * Integración de autenticación biométrica (huella, rostro o, como respaldo, el PIN/patrón del teléfono).
 * Se usa para proteger la app al abrirla cuando el usuario lo activa en su perfil.
 */
object BiometricAuthenticator {

    // BIOMETRIC_WEAK | DEVICE_CREDENTIAL funciona desde Android 8 (minSdk 26) y permite usar el PIN si no hay huella
    private const val AUTHENTICATORS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    fun availability(context: Context): BiometricAvailability =
        when (BiometricManager.from(context).canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricAvailability.Unavailable("Registra una huella, rostro o PIN en los ajustes del teléfono.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricAvailability.Unavailable("Este dispositivo no tiene sensor biométrico.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricAvailability.Unavailable("El sensor biométrico no está disponible en este momento.")
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                BiometricAvailability.Unavailable("Se requiere una actualización de seguridad del sistema.")
            else -> BiometricAvailability.Unavailable("La autenticación biométrica no es compatible con este dispositivo.")
        }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Se llama cuando el usuario cancela o hay demasiados intentos
                onError(errString.toString())
            }
            // onAuthenticationFailed (huella no reconocida) no se maneja: el diálogo permite reintentar
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        prompt.authenticate(promptInfo)
    }
}

package com.example.gastou.core.common

/**
 * Reglas de validación de formularios. Devuelven el mensaje de error o null si el valor es válido.
 * Las mismas reglas existen en la API, pero validar aquí da respuesta inmediata al usuario.
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")
    const val MAX_AMOUNT = 10_000_000.0

    fun name(value: String): String? = when {
        value.isBlank() -> "Escribe tu nombre"
        value.trim().length < 2 -> "El nombre es muy corto"
        value.trim().length > 80 -> "El nombre es muy largo"
        else -> null
    }

    fun email(value: String): String? = when {
        value.isBlank() -> "Escribe tu correo"
        !EMAIL_REGEX.matches(value.trim()) -> "El correo no es válido"
        else -> null
    }

    fun password(value: String): String? = when {
        value.isEmpty() -> "Escribe tu contraseña"
        value.length < 6 -> "Debe tener al menos 6 caracteres"
        value.length > 72 -> "Debe tener máximo 72 caracteres"
        else -> null
    }

    fun passwordConfirmation(password: String, confirmation: String): String? =
        if (password != confirmation) "Las contraseñas no coinciden" else null

    /** Valida un monto escrito por el usuario. Acepta punto o coma decimal. */
    fun amount(value: String): String? {
        val number = parseAmount(value)
        return when {
            value.isBlank() -> "Escribe un monto"
            number == null -> "El monto no es un número válido"
            number <= 0.0 -> "El monto debe ser mayor a 0"
            number > MAX_AMOUNT -> "El monto es demasiado grande"
            else -> null
        }
    }

    fun parseAmount(value: String): Double? =
        value.trim().replace(",", ".").toDoubleOrNull()

    /** Filtro para campos de monto: solo dígitos y un separador decimal con máximo 2 decimales. */
    fun sanitizeAmountInput(input: String): String {
        val normalized = input.replace(",", ".").filter { it.isDigit() || it == '.' }
        val firstDot = normalized.indexOf('.')
        if (firstDot == -1) return normalized.take(9)
        val integerPart = normalized.substring(0, firstDot).take(9)
        val decimals = normalized.substring(firstDot + 1).replace(".", "").take(2)
        return "$integerPart.$decimals"
    }
}

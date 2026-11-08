package com.example.gastou

import com.example.gastou.core.common.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas locales de las reglas de validación de formularios. */
class ValidatorsTest {

    @Test
    fun email_valido_e_invalido() {
        assertNull(Validators.email("walter@correo.com"))
        assertNotNull(Validators.email("walter@"))
        assertNotNull(Validators.email(""))
    }

    @Test
    fun password_minimo_seis_caracteres() {
        assertNotNull(Validators.password("12345"))
        assertNull(Validators.password("123456"))
    }

    @Test
    fun confirmacion_de_password() {
        assertNull(Validators.passwordConfirmation("abc123", "abc123"))
        assertNotNull(Validators.passwordConfirmation("abc123", "abc124"))
    }

    @Test
    fun monto_debe_ser_positivo_y_numerico() {
        assertNull(Validators.amount("150.50"))
        assertNull(Validators.amount("150,50"))
        assertNotNull(Validators.amount("0"))
        assertNotNull(Validators.amount("abc"))
        assertNotNull(Validators.amount(""))
    }

    @Test
    fun limpia_el_texto_del_monto() {
        assertEquals("12.34", Validators.sanitizeAmountInput("12.345"))
        assertEquals("12.3", Validators.sanitizeAmountInput("1a2,3"))
        assertEquals("100", Validators.sanitizeAmountInput("100"))
    }
}

package com.example.gastou.domain.model

import java.time.LocalDate
import java.util.UUID

/**
 * Gasto registrado por el estudiante.
 * El id es un UUID generado en el teléfono; el servidor lo usa como "clientId" para que
 * un gasto creado sin conexión no se duplique al sincronizar.
 */
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val categoryId: String,
    val date: String,
    val paymentMethod: String,
    val note: String,
    val receiptUri: String? = null,
    val dateObj: LocalDate = LocalDate.now(),
    val isSynced: Boolean = false
)

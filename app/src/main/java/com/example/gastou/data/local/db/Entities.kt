package com.example.gastou.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Estado de sincronización de cada registro local respecto a la API.
 * - SYNCED: igual que en el servidor.
 * - PENDING_UPSERT: creado o modificado sin confirmación del servidor.
 * - PENDING_DELETE: borrado por el usuario; falta borrarlo en el servidor.
 */
object SyncState {
    const val SYNCED = 0
    const val PENDING_UPSERT = 1
    const val PENDING_DELETE = 2
}

@Entity(tableName = "expenses", indices = [Index("dateEpochDay"), Index("syncState")])
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val categoryId: String,
    val displayDate: String,
    val paymentMethod: String,
    val note: String,
    val receiptUri: String?,
    val dateEpochDay: Long,
    val syncState: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saving_goals", indices = [Index("syncState")])
data class SavingGoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val category: String,
    val targetDate: String?,
    val isCompleted: Boolean,
    val syncState: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val month: String, // AAAA-MM
    val amount: Double,
    val syncState: Int
)

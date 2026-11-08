package com.example.gastou.data.mapper

import com.example.gastou.data.local.db.BudgetEntity
import com.example.gastou.data.local.db.ExpenseEntity
import com.example.gastou.data.local.db.SavingGoalEntity
import com.example.gastou.data.local.db.SyncState
import com.example.gastou.data.remote.dto.BudgetDto
import com.example.gastou.data.remote.dto.CategoryTotalDto
import com.example.gastou.data.remote.dto.ExpenseDto
import com.example.gastou.data.remote.dto.ExpenseRequestDto
import com.example.gastou.data.remote.dto.GoalDto
import com.example.gastou.data.remote.dto.GoalRequestDto
import com.example.gastou.data.remote.dto.MonthlySummaryDto
import com.example.gastou.data.remote.dto.UserDto
import com.example.gastou.domain.model.CategoryTotal
import com.example.gastou.domain.model.Expense
import com.example.gastou.domain.model.MonthlySummary
import com.example.gastou.domain.model.SavingGoal
import com.example.gastou.domain.model.User
import java.time.LocalDate
import java.time.YearMonth

/*
 * Conversiones entre los modelos de cada capa:
 * DTO (API)  <->  Entity (Room)  <->  modelo de dominio (lo que usa la interfaz).
 */

// ---------- Usuario ----------
fun UserDto.toDomain() = User(
    id = id,
    nombre = nombre,
    email = email,
    universidad = universidad.orEmpty(),
    createdAt = createdAt.orEmpty()
)

// ---------- Gastos ----------
fun ExpenseEntity.toDomain() = Expense(
    id = id,
    amount = amount,
    categoryId = categoryId,
    date = displayDate,
    paymentMethod = paymentMethod,
    note = note,
    receiptUri = receiptUri,
    dateObj = LocalDate.ofEpochDay(dateEpochDay),
    isSynced = syncState == SyncState.SYNCED
)

fun Expense.toEntity(syncState: Int) = ExpenseEntity(
    id = id,
    amount = amount,
    categoryId = categoryId,
    displayDate = date,
    paymentMethod = paymentMethod,
    note = note,
    receiptUri = receiptUri,
    dateEpochDay = dateObj.toEpochDay(),
    syncState = syncState
)

fun ExpenseEntity.toRequest() = ExpenseRequestDto(
    amount = amount,
    category = categoryId,
    date = LocalDate.ofEpochDay(dateEpochDay).toString(),
    displayDate = displayDate,
    paymentMethod = paymentMethod,
    note = note
)

/** La foto del ticket solo vive en el teléfono, por eso se conserva la del registro local. */
fun ExpenseDto.toEntity(localReceiptUri: String?) = ExpenseEntity(
    id = clientId,
    amount = amount,
    categoryId = category,
    displayDate = displayDate.orEmpty().ifBlank { date },
    paymentMethod = paymentMethod,
    note = note.orEmpty(),
    receiptUri = localReceiptUri,
    dateEpochDay = LocalDate.parse(date).toEpochDay(),
    syncState = SyncState.SYNCED
)

// ---------- Metas ----------
fun SavingGoalEntity.toDomain() = SavingGoal(
    id = id,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    category = category,
    targetDate = targetDate,
    isCompleted = isCompleted
)

fun SavingGoal.toEntity(syncState: Int) = SavingGoalEntity(
    id = id,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    category = category,
    targetDate = targetDate,
    isCompleted = isCompleted,
    syncState = syncState
)

fun SavingGoalEntity.toRequest() = GoalRequestDto(
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    category = category,
    targetDate = targetDate?.takeIf { isIsoDate(it) },
    isCompleted = isCompleted
)

fun GoalDto.toEntity() = SavingGoalEntity(
    id = clientId,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    category = category,
    targetDate = targetDate,
    isCompleted = isCompleted,
    syncState = SyncState.SYNCED
)

// ---------- Presupuestos ----------
fun BudgetDto.toEntity() = BudgetEntity(month = month, amount = amount, syncState = SyncState.SYNCED)

// ---------- Resumen ----------
fun MonthlySummaryDto.toDomain() = MonthlySummary(
    month = YearMonth.parse(month),
    total = total,
    budget = budget,
    byCategory = byCategory.map(CategoryTotalDto::toDomain)
)

fun CategoryTotalDto.toDomain() = CategoryTotal(category = category, total = total, count = count)

private fun isIsoDate(value: String): Boolean =
    runCatching { LocalDate.parse(value) }.isSuccess

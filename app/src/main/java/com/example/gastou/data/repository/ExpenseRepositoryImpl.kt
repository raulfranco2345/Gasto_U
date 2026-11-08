package com.example.gastou.data.repository

import com.example.gastou.core.common.ErrorMapper
import com.example.gastou.data.local.db.ExpenseDao
import com.example.gastou.data.local.db.ExpenseEntity
import com.example.gastou.data.local.db.SyncState
import com.example.gastou.data.mapper.toDomain
import com.example.gastou.data.mapper.toEntity
import com.example.gastou.data.mapper.toRequest
import com.example.gastou.data.remote.api.GastoUApi
import com.example.gastou.domain.model.Expense
import com.example.gastou.domain.model.MonthlySummary
import com.example.gastou.domain.model.SyncOutcome
import com.example.gastou.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.time.YearMonth

/**
 * Estrategia "offline first":
 * 1. Toda escritura se guarda primero en Room (la interfaz se actualiza al instante).
 * 2. Después se intenta enviar a la API. Si no hay red, el registro queda pendiente.
 * 3. sync() reintenta los pendientes y descarga lo que hay en el servidor.
 */
class ExpenseRepositoryImpl(
    private val dao: ExpenseDao,
    private val api: GastoUApi,
    private val onDataChanged: suspend () -> Unit,
    private val io: CoroutineDispatcher
) : ExpenseRepository {

    override fun observeExpenses(): Flow<List<Expense>> =
        dao.observeActive().map { list -> list.map(ExpenseEntity::toDomain) }

    override fun observeExpensesOfMonth(month: YearMonth): Flow<List<Expense>> {
        val (from, to) = month.toEpochRange()
        return dao.observeBetween(from, to).map { list -> list.map(ExpenseEntity::toDomain) }
    }

    override fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    override suspend fun addExpense(expense: Expense): SyncOutcome = withContext(io) {
        val entity = expense.toEntity(SyncState.PENDING_UPSERT)
        dao.upsert(entity)
        onDataChanged()
        pushUpsert(entity)
    }

    override suspend fun deleteExpense(expenseId: String): SyncOutcome = withContext(io) {
        dao.updateState(expenseId, SyncState.PENDING_DELETE)
        onDataChanged()
        pushDelete(expenseId)
    }

    override suspend fun sync(): SyncOutcome = withContext(io) {
        try {
            // 1) Enviar cambios locales pendientes
            dao.getByState(SyncState.PENDING_UPSERT).forEach { entity ->
                api.upsertExpense(entity.id, entity.toRequest())
                dao.updateState(entity.id, SyncState.SYNCED)
            }
            dao.getByState(SyncState.PENDING_DELETE).forEach { entity ->
                deleteRemote(entity.id)
                dao.deleteById(entity.id)
            }
            // 2) Descargar el estado del servidor
            val localReceipts = mutableMapOf<String, String?>()
            val remote = api.getExpenses().map { dto ->
                val receipt = localReceipts.getOrPut(dto.clientId) { dao.getById(dto.clientId)?.receiptUri }
                dto.toEntity(receipt)
            }
            dao.replaceSynced(remote)
            onDataChanged()
            SyncOutcome.Synced
        } catch (error: Exception) {
            SyncOutcome.Failed(ErrorMapper.toUserMessage(error))
        }
    }

    override suspend fun fetchMonthlySummary(month: YearMonth): Result<MonthlySummary> = withContext(io) {
        runCatching { api.getMonthlySummary(month.toString()).toDomain() }
            .recoverCatching { throw Exception(ErrorMapper.toUserMessage(it), it) }
    }

    override suspend fun totalOfMonth(month: YearMonth): Double = withContext(io) {
        val (from, to) = month.toEpochRange()
        dao.totalBetween(from, to)
    }

    private suspend fun pushUpsert(entity: ExpenseEntity): SyncOutcome = try {
        api.upsertExpense(entity.id, entity.toRequest())
        dao.updateState(entity.id, SyncState.SYNCED)
        SyncOutcome.Synced
    } catch (error: Exception) {
        if (ErrorMapper.isNetworkError(error)) {
            SyncOutcome.SavedOffline("Gasto guardado en el teléfono. Se enviará cuando haya conexión.")
        } else {
            // El servidor rechazó el dato: se conserva localmente para no perderlo
            SyncOutcome.Failed(ErrorMapper.toUserMessage(error))
        }
    }

    private suspend fun pushDelete(id: String): SyncOutcome = try {
        deleteRemote(id)
        dao.deleteById(id)
        SyncOutcome.Synced
    } catch (error: Exception) {
        if (ErrorMapper.isNetworkError(error)) {
            SyncOutcome.SavedOffline("Gasto eliminado. El cambio se enviará cuando haya conexión.")
        } else {
            SyncOutcome.Failed(ErrorMapper.toUserMessage(error))
        }
    }

    /** Borra en el servidor; un 404 significa que ya no existe allá, lo cual es correcto. */
    private suspend fun deleteRemote(id: String) {
        val response = api.deleteExpense(id)
        if (!response.isSuccessful && response.code() != 404) {
            throw HttpException(response)
        }
    }
}

/** Rango [primer día del mes, primer día del mes siguiente) en días desde 1970. */
internal fun YearMonth.toEpochRange(): Pair<Long, Long> =
    atDay(1).toEpochDay() to plusMonths(1).atDay(1).toEpochDay()

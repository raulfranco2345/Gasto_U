package com.example.gastou.data.repository

import com.example.gastou.core.common.ErrorMapper
import com.example.gastou.data.local.db.BudgetDao
import com.example.gastou.data.local.db.BudgetEntity
import com.example.gastou.data.local.db.SyncState
import com.example.gastou.data.mapper.toEntity
import com.example.gastou.data.remote.api.GastoUApi
import com.example.gastou.data.remote.dto.BudgetRequestDto
import com.example.gastou.domain.model.SyncOutcome
import com.example.gastou.domain.repository.BudgetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.YearMonth

/** Presupuesto mensual: un registro por mes en Room y en la API. */
class BudgetRepositoryImpl(
    private val dao: BudgetDao,
    private val api: GastoUApi,
    private val onDataChanged: suspend () -> Unit,
    private val io: CoroutineDispatcher
) : BudgetRepository {

    override fun observeBudget(month: YearMonth): Flow<Double?> = dao.observeAmount(month.toString())

    override suspend fun budgetOf(month: YearMonth): Double? = withContext(io) {
        dao.getAmount(month.toString())
    }

    override suspend fun setBudget(month: YearMonth, amount: Double): SyncOutcome = withContext(io) {
        val key = month.toString()
        dao.upsert(BudgetEntity(month = key, amount = amount, syncState = SyncState.PENDING_UPSERT))
        onDataChanged()
        try {
            api.upsertBudget(key, BudgetRequestDto(amount))
            dao.updateState(key, SyncState.SYNCED)
            SyncOutcome.Synced
        } catch (error: Exception) {
            if (ErrorMapper.isNetworkError(error)) {
                SyncOutcome.SavedOffline("Presupuesto guardado en el teléfono. Se enviará cuando haya conexión.")
            } else {
                SyncOutcome.Failed(ErrorMapper.toUserMessage(error))
            }
        }
    }

    override suspend fun sync(): SyncOutcome = withContext(io) {
        try {
            dao.getPending().forEach { budget ->
                api.upsertBudget(budget.month, BudgetRequestDto(budget.amount))
                dao.updateState(budget.month, SyncState.SYNCED)
            }
            dao.replaceSynced(api.getBudgets().map { it.toEntity() })
            onDataChanged()
            SyncOutcome.Synced
        } catch (error: Exception) {
            SyncOutcome.Failed(ErrorMapper.toUserMessage(error))
        }
    }
}

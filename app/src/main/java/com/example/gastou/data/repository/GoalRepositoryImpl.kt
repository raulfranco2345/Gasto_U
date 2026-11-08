package com.example.gastou.data.repository

import com.example.gastou.core.common.ErrorMapper
import com.example.gastou.data.local.db.SavingGoalDao
import com.example.gastou.data.local.db.SavingGoalEntity
import com.example.gastou.data.local.db.SyncState
import com.example.gastou.data.mapper.toDomain
import com.example.gastou.data.mapper.toEntity
import com.example.gastou.data.mapper.toRequest
import com.example.gastou.data.remote.api.GastoUApi
import com.example.gastou.domain.model.SavingGoal
import com.example.gastou.domain.model.SyncOutcome
import com.example.gastou.domain.repository.GoalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/** Metas de ahorro con la misma estrategia offline first que los gastos. */
class GoalRepositoryImpl(
    private val dao: SavingGoalDao,
    private val api: GastoUApi,
    private val io: CoroutineDispatcher
) : GoalRepository {

    override fun observeGoals(): Flow<List<SavingGoal>> =
        dao.observeActive().map { list -> list.map(SavingGoalEntity::toDomain) }

    override suspend fun saveGoal(goal: SavingGoal): SyncOutcome = withContext(io) {
        val entity = goal.toEntity(SyncState.PENDING_UPSERT)
        dao.upsert(entity)
        try {
            api.upsertGoal(entity.id, entity.toRequest())
            dao.updateState(entity.id, SyncState.SYNCED)
            SyncOutcome.Synced
        } catch (error: Exception) {
            if (ErrorMapper.isNetworkError(error)) {
                SyncOutcome.SavedOffline("Meta guardada en el teléfono. Se enviará cuando haya conexión.")
            } else {
                SyncOutcome.Failed(ErrorMapper.toUserMessage(error))
            }
        }
    }

    override suspend fun deleteGoal(goalId: String): SyncOutcome = withContext(io) {
        dao.updateState(goalId, SyncState.PENDING_DELETE)
        try {
            deleteRemote(goalId)
            dao.deleteById(goalId)
            SyncOutcome.Synced
        } catch (error: Exception) {
            if (ErrorMapper.isNetworkError(error)) {
                SyncOutcome.SavedOffline("Meta eliminada. El cambio se enviará cuando haya conexión.")
            } else {
                SyncOutcome.Failed(ErrorMapper.toUserMessage(error))
            }
        }
    }

    override suspend fun sync(): SyncOutcome = withContext(io) {
        try {
            dao.getByState(SyncState.PENDING_UPSERT).forEach { entity ->
                api.upsertGoal(entity.id, entity.toRequest())
                dao.updateState(entity.id, SyncState.SYNCED)
            }
            dao.getByState(SyncState.PENDING_DELETE).forEach { entity ->
                deleteRemote(entity.id)
                dao.deleteById(entity.id)
            }
            dao.replaceSynced(api.getGoals().map { it.toEntity() })
            SyncOutcome.Synced
        } catch (error: Exception) {
            SyncOutcome.Failed(ErrorMapper.toUserMessage(error))
        }
    }

    private suspend fun deleteRemote(id: String) {
        val response = api.deleteGoal(id)
        if (!response.isSuccessful && response.code() != 404) {
            throw HttpException(response)
        }
    }
}

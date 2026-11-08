package com.example.gastou.domain.repository

import com.example.gastou.domain.model.SavingGoal
import com.example.gastou.domain.model.SyncOutcome
import kotlinx.coroutines.flow.Flow

/** Contrato de metas de ahorro (Room + API). */
interface GoalRepository {
    fun observeGoals(): Flow<List<SavingGoal>>

    suspend fun saveGoal(goal: SavingGoal): SyncOutcome
    suspend fun deleteGoal(goalId: String): SyncOutcome
    suspend fun sync(): SyncOutcome
}

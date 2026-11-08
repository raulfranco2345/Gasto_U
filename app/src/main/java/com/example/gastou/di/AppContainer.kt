package com.example.gastou.di

import android.content.Context
import com.example.gastou.data.local.db.GastoUDatabase
import com.example.gastou.data.local.preferences.PreferencesDataSource
import com.example.gastou.data.remote.api.ApiClient
import com.example.gastou.data.remote.api.GastoUApi
import com.example.gastou.data.repository.AuthRepositoryImpl
import com.example.gastou.data.repository.BudgetRepositoryImpl
import com.example.gastou.data.repository.ExpenseRepositoryImpl
import com.example.gastou.data.repository.GoalRepositoryImpl
import com.example.gastou.data.repository.SettingsRepositoryImpl
import com.example.gastou.domain.repository.AuthRepository
import com.example.gastou.domain.repository.BudgetRepository
import com.example.gastou.domain.repository.ExpenseRepository
import com.example.gastou.domain.repository.GoalRepository
import com.example.gastou.domain.repository.SettingsRepository
import com.example.gastou.domain.usecase.AddFundsToGoalUseCase
import com.example.gastou.domain.usecase.SyncAllDataUseCase
import com.example.gastou.integration.pdf.PdfReportGenerator
import com.example.gastou.integration.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.YearMonth

/**
 * Inyección de dependencias manual (contenedor de la aplicación).
 * Crea una sola instancia de Retrofit, Room, DataStore y repositorios y las comparte.
 * Ninguna pantalla construye estos objetos directamente.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val io = Dispatchers.IO

    /** Alcance de la aplicación para tareas que no dependen de una pantalla. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Mes que el usuario está consultando; lo comparten Inicio, Historial y Presupuesto. */
    val selectedMonth = kotlinx.coroutines.flow.MutableStateFlow(YearMonth.now())

    private val database: GastoUDatabase by lazy { GastoUDatabase.build(appContext) }

    private val preferences: PreferencesDataSource by lazy { PreferencesDataSource(appContext) }

    private val api: GastoUApi by lazy {
        ApiClient.create(
            tokenProvider = { preferences.currentToken() },
            // Si la API responde 401 el token venció: se cierra la sesión y la navegación vuelve al login
            onUnauthorized = { applicationScope.launch { authRepository.logout() } }
        )
    }

    private val widgetUpdater: WidgetUpdater by lazy { WidgetUpdater(appContext) }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(api, preferences, database, io)
    }

    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(database.expenseDao(), api, { widgetUpdater.refresh() }, io)
    }

    val goalRepository: GoalRepository by lazy {
        GoalRepositoryImpl(database.savingGoalDao(), api, io)
    }

    val budgetRepository: BudgetRepository by lazy {
        BudgetRepositoryImpl(database.budgetDao(), api, { widgetUpdater.refresh() }, io)
    }

    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(preferences) }

    val syncAllData: SyncAllDataUseCase by lazy {
        SyncAllDataUseCase(expenseRepository, goalRepository, budgetRepository)
    }

    val addFundsToGoal: AddFundsToGoalUseCase by lazy {
        AddFundsToGoalUseCase(goalRepository, expenseRepository)
    }

    val pdfReportGenerator: PdfReportGenerator by lazy { PdfReportGenerator(appContext) }
}

package com.example.gastou.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gastou.GastoUApp
import com.example.gastou.MainViewModel
import com.example.gastou.presentation.auth.LoginViewModel
import com.example.gastou.presentation.auth.RegisterViewModel
import com.example.gastou.presentation.budget.BudgetViewModel
import com.example.gastou.presentation.expense.AddExpenseViewModel
import com.example.gastou.presentation.goals.GoalsViewModel
import com.example.gastou.presentation.history.HistoryViewModel
import com.example.gastou.presentation.home.HomeViewModel
import com.example.gastou.presentation.lock.LockViewModel
import com.example.gastou.presentation.profile.ProfileViewModel

/**
 * Fábrica única de ViewModels. Toma las dependencias del AppContainer,
 * así las pantallas solo piden "viewModel(factory = AppViewModelProvider.Factory)".
 */
object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            MainViewModel(container().authRepository, container().settingsRepository)
        }
        initializer {
            LoginViewModel(container().authRepository)
        }
        initializer {
            LockViewModel(container().authRepository)
        }
        initializer {
            RegisterViewModel(container().authRepository)
        }
        initializer {
            val c = container()
            HomeViewModel(
                expenseRepository = c.expenseRepository,
                goalRepository = c.goalRepository,
                budgetRepository = c.budgetRepository,
                authRepository = c.authRepository,
                syncAllData = c.syncAllData,
                pdfReportGenerator = c.pdfReportGenerator,
                selectedMonth = c.selectedMonth
            )
        }
        initializer {
            AddExpenseViewModel(container().expenseRepository)
        }
        initializer {
            val c = container()
            HistoryViewModel(c.expenseRepository, c.budgetRepository, c.selectedMonth)
        }
        initializer {
            val c = container()
            GoalsViewModel(c.goalRepository, c.addFundsToGoal)
        }
        initializer {
            val c = container()
            BudgetViewModel(c.budgetRepository, c.selectedMonth)
        }
        initializer {
            val c = container()
            ProfileViewModel(c.authRepository, c.settingsRepository, c.expenseRepository, c.goalRepository)
        }
    }

    private fun CreationExtras.container(): AppContainer =
        (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastoUApp).container
}

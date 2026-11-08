package com.example.gastou.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gastou.core.ui.components.MessageEffect
import com.example.gastou.core.ui.components.findActivity
import com.example.gastou.di.AppViewModelProvider
import com.example.gastou.integration.biometric.BiometricAuthenticator
import com.example.gastou.integration.biometric.BiometricAvailability
import com.example.gastou.presentation.auth.LoginScreen
import com.example.gastou.presentation.auth.LoginViewModel
import com.example.gastou.presentation.auth.RegisterScreen
import com.example.gastou.presentation.auth.RegisterViewModel
import com.example.gastou.presentation.budget.BudgetViewModel
import com.example.gastou.presentation.budget.ModifyBudgetScreen
import com.example.gastou.presentation.expense.AddExpenseScreen
import com.example.gastou.presentation.expense.AddExpenseViewModel
import com.example.gastou.presentation.expense.SelectCategoryScreen
import com.example.gastou.presentation.expense.SelectDateScreen
import com.example.gastou.presentation.expense.SelectPaymentMethodScreen
import com.example.gastou.presentation.goals.AddGoalScreen
import com.example.gastou.presentation.goals.GoalDetailScreen
import com.example.gastou.presentation.goals.GoalsScreen
import com.example.gastou.presentation.goals.GoalsViewModel
import com.example.gastou.presentation.history.ExpenseDetailScreen
import com.example.gastou.presentation.history.HistoryScreen
import com.example.gastou.presentation.history.HistoryViewModel
import com.example.gastou.presentation.home.HomeScreen
import com.example.gastou.presentation.home.HomeViewModel
import com.example.gastou.presentation.lock.LockScreen
import com.example.gastou.presentation.lock.LockViewModel
import com.example.gastou.presentation.profile.ProfileScreen
import com.example.gastou.presentation.profile.ProfileViewModel

/**
 * Grafo de navegación central. Aquí se conectan las pantallas con sus ViewModels;
 * las pantallas solo reciben estado y funciones (eventos).
 */
@Composable
fun AppNavGraph(
    startDestination: String,
    isLoggedIn: Boolean?,
    navController: NavHostController = rememberNavController()
) {
    // Si la sesión se cierra (logout o token vencido) se regresa al login y se limpia la pila
    LaunchedEffect(isLoggedIn) {
        val current = navController.currentDestination?.route
        if (isLoggedIn == false && current != null && current !in Routes.publicRoutes) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // ---------------- Autenticación ----------------
        composable(Routes.LOGIN) {
            val vm: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.isLoggedIn) {
                if (state.isLoggedIn) navController.navigateClearingStack(Routes.HOME)
            }
            LoginScreen(
                uiState = state,
                onEmailChange = vm::onEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onLogin = vm::login,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            val vm: RegisterViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.isRegistered) {
                if (state.isRegistered) navController.navigateClearingStack(Routes.HOME)
            }
            RegisterScreen(
                uiState = state,
                onNameChange = vm::onNameChange,
                onEmailChange = vm::onEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onConfirmChange = vm::onConfirmChange,
                onTermsChange = vm::onTermsChange,
                onRegister = vm::register,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack(Routes.LOGIN, inclusive = false) }
            )
        }

        composable(Routes.LOCK) {
            val vm: LockViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val userName by vm.userName.collectAsStateWithLifecycle()
            val error by vm.error.collectAsStateWithLifecycle()
            val context = LocalContext.current

            val unlock: () -> Unit = {
                vm.clearError()
                val activity = context.findActivity() as? FragmentActivity
                when (val availability = BiometricAuthenticator.availability(context)) {
                    BiometricAvailability.Available -> if (activity != null) {
                        BiometricAuthenticator.authenticate(
                            activity = activity,
                            title = "Desbloquear GastoU",
                            subtitle = "Usa tu huella, rostro o PIN",
                            onSuccess = { navController.navigateClearingStack(Routes.HOME) },
                            onError = vm::onAuthError
                        )
                    }
                    is BiometricAvailability.Unavailable ->
                        vm.onAuthError("${availability.reason} Toca \"Usar otra cuenta\" para entrar con tu contraseña.")
                }
                Unit
            }
            // El diálogo se abre automáticamente al llegar a la pantalla
            LaunchedEffect(Unit) { unlock() }

            LockScreen(
                userName = userName,
                errorMessage = error,
                onUnlock = unlock,
                onUseAnotherAccount = { vm.logout { navController.navigateClearingStack(Routes.LOGIN) } }
            )
        }

        // ---------------- Inicio y perfil ----------------
        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                uiState = state,
                onMonthSelected = vm::selectMonth,
                onNavigateToModifyBudget = { navController.navigate(Routes.MODIFY_BUDGET) },
                onNavigateToAddExpense = { navController.navigate(Routes.ADD_EXPENSE) },
                onNavigateToHistory = { navController.navigateToTab(Routes.HISTORY) },
                onNavigateToGoals = { navController.navigateToTab(Routes.GOALS) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onRetrySync = vm::sync,
                onExportPdf = vm::exportPdf,
                onPdfHandled = vm::onPdfHandled,
                onMessageShown = vm::onMessageShown
            )
        }

        composable(Routes.PROFILE) {
            val vm: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            MessageEffect(state.message, vm::onMessageShown)

            ProfileScreen(
                uiState = state,
                onNavigateBack = { navController.popBackStack() },
                onRetrySummary = vm::refresh,
                onStartEditing = vm::startEditing,
                onCancelEditing = vm::cancelEditing,
                onEditNameChange = vm::onEditNameChange,
                onEditUniversityChange = vm::onEditUniversityChange,
                onSaveProfile = vm::saveProfile,
                onBiometricToggle = { enable ->
                    if (!enable) {
                        vm.setBiometricEnabled(false)
                    } else {
                        // Antes de activar el bloqueo se confirma que el dispositivo lo soporta y que el usuario lo autoriza
                        when (val availability = BiometricAuthenticator.availability(context)) {
                            BiometricAvailability.Available -> {
                                val activity = context.findActivity() as? FragmentActivity
                                if (activity != null) {
                                    BiometricAuthenticator.authenticate(
                                        activity = activity,
                                        title = "Activar bloqueo",
                                        subtitle = "Confirma con tu huella, rostro o PIN",
                                        onSuccess = { vm.setBiometricEnabled(true) },
                                        onError = vm::showMessage
                                    )
                                }
                            }
                            is BiometricAvailability.Unavailable -> vm.showMessage(availability.reason)
                        }
                    }
                },
                onLogout = vm::logout
            )
        }

        composable(Routes.MODIFY_BUDGET) {
            val vm: BudgetViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            LaunchedEffect(state.message, state.isSaved) {
                state.message?.let {
                    android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
                }
                if (state.isSaved) {
                    vm.onEventsHandled()
                    navController.popBackStack()
                } else if (state.message != null) {
                    vm.onEventsHandled()
                }
            }
            ModifyBudgetScreen(
                currentBudget = state.currentBudget,
                selectedMonth = state.selectedMonth,
                onMonthSelected = vm::selectMonth,
                onSaveBudget = vm::saveBudget,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------- Registro de gasto (comparten AddExpenseViewModel) ----------------
        composable(Routes.ADD_EXPENSE) {
            val vm: AddExpenseViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            LaunchedEffect(state.isSaved) {
                if (state.isSaved) {
                    state.savedMessage?.let {
                        android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
                    }
                    vm.onSavedHandled()
                    navController.navigateToTab(Routes.HOME)
                }
            }
            AddExpenseScreen(
                uiState = state,
                onAmountChange = vm::updateAmount,
                onNavigateToCategory = { navController.navigate(Routes.SELECT_CATEGORY) },
                onNavigateToDate = { navController.navigate(Routes.SELECT_DATE) },
                onNavigateToPaymentMethod = { navController.navigate(Routes.SELECT_PAYMENT_METHOD) },
                onNoteChange = vm::updateNote,
                onReceiptPhotoTaken = vm::updateReceiptUri,
                onSave = vm::saveExpense,
                onNavigateHome = {
                    vm.resetForm()
                    navController.navigateToTab(Routes.HOME)
                },
                onNavigateToHistory = {
                    vm.resetForm()
                    navController.navigateToTab(Routes.HISTORY)
                },
                onNavigateToGoals = {
                    vm.resetForm()
                    navController.navigateToTab(Routes.GOALS)
                }
            )
        }

        composable(Routes.SELECT_CATEGORY) { entry ->
            val vm: AddExpenseViewModel = sharedViewModel(navController, entry, Routes.ADD_EXPENSE)
            val state by vm.uiState.collectAsStateWithLifecycle()
            SelectCategoryScreen(
                currentCategory = state.category,
                onCategorySelected = {
                    vm.updateCategory(it)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SELECT_DATE) { entry ->
            val vm: AddExpenseViewModel = sharedViewModel(navController, entry, Routes.ADD_EXPENSE)
            val state by vm.uiState.collectAsStateWithLifecycle()
            SelectDateScreen(
                currentDate = state.selectedDate,
                currentTime = state.selectedTime,
                onDateTimeSelected = vm::updateDateTime,
                onConfirm = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SELECT_PAYMENT_METHOD) { entry ->
            val vm: AddExpenseViewModel = sharedViewModel(navController, entry, Routes.ADD_EXPENSE)
            val state by vm.uiState.collectAsStateWithLifecycle()
            SelectPaymentMethodScreen(
                currentMethod = state.paymentMethod,
                onMethodSelected = {
                    vm.updatePaymentMethod(it)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------- Historial ----------------
        composable(Routes.HISTORY) {
            val vm: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            MessageEffect(state.message, vm::onMessageShown)
            HistoryScreen(
                uiState = state,
                onMonthSelected = vm::selectMonth,
                onNavigateToHome = { navController.navigateToTab(Routes.HOME) },
                onNavigateToAddExpense = { navController.navigate(Routes.ADD_EXPENSE) },
                onNavigateToGoals = { navController.navigateToTab(Routes.GOALS) },
                onNavigateToExpenseDetail = { id -> navController.navigate(Routes.expenseDetail(id)) }
            )
        }

        composable(
            route = Routes.EXPENSE_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_EXPENSE_ID) { type = NavType.StringType })
        ) { entry ->
            val vm: HistoryViewModel = sharedViewModel(navController, entry, Routes.HISTORY)
            val state by vm.uiState.collectAsStateWithLifecycle()
            val expenseId = entry.arguments?.getString(Routes.ARG_EXPENSE_ID)
            LaunchedEffect(expenseId) { vm.selectExpenseForDetail(expenseId) }

            val expense = state.expenses.find { it.id == expenseId }
            if (expense != null) {
                ExpenseDetailScreen(
                    expense = expense,
                    uiState = state.copy(selectedExpenseId = expenseId),
                    onNavigateBack = { navController.popBackStack() },
                    onDelete = {
                        vm.deleteExpense(expense.id)
                        navController.popBackStack()
                    }
                )
            }
        }

        // ---------------- Metas de ahorro ----------------
        composable(Routes.GOALS) {
            val vm: GoalsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            MessageEffect(state.message, vm::onMessageShown)
            GoalsScreen(
                uiState = state,
                onNavigateHome = { navController.navigateToTab(Routes.HOME) },
                onNavigateToAddExpense = { navController.navigate(Routes.ADD_EXPENSE) },
                onNavigateToHistory = { navController.navigateToTab(Routes.HISTORY) },
                onNavigateToAddGoal = { navController.navigate(Routes.ADD_GOAL) },
                onNavigateToGoalDetail = { id -> navController.navigate(Routes.goalDetail(id)) }
            )
        }

        composable(Routes.ADD_GOAL) { entry ->
            val vm: GoalsViewModel = sharedViewModel(navController, entry, Routes.GOALS)
            val state by vm.uiState.collectAsStateWithLifecycle()
            MessageEffect(state.message, vm::onMessageShown)
            AddGoalScreen(
                onSaveGoal = { newGoal ->
                    if (vm.saveNewGoal(newGoal)) navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.GOAL_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_GOAL_ID) { type = NavType.StringType })
        ) { entry ->
            val vm: GoalsViewModel = sharedViewModel(navController, entry, Routes.GOALS)
            val state by vm.uiState.collectAsStateWithLifecycle()
            val goalId = entry.arguments?.getString(Routes.ARG_GOAL_ID)
            LaunchedEffect(goalId) { vm.selectGoal(goalId) }

            if (state.savingGoals.any { it.id == goalId }) {
                GoalDetailScreen(
                    uiState = state.copy(selectedGoalId = goalId),
                    onNavigateBack = { navController.popBackStack() },
                    onAddFunds = vm::addFundsToGoal,
                    onMarkCompleted = vm::markGoalCompleted,
                    onDelete = {
                        vm.deleteGoal()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/** Navega a una sección principal (Inicio, Historial, Metas) sin apilar pantallas repetidas. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        // Se conserva Inicio como base de la pila para no recrear su ViewModel
        popUpTo(Routes.HOME)
        launchSingleTop = true
    }
}

/** Navega y elimina toda la pila anterior (después de iniciar o cerrar sesión). */
private fun NavHostController.navigateClearingStack(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

/**
 * Obtiene un ViewModel guardado en una pantalla anterior de la pila, para que varias
 * pantallas compartan el mismo estado (por ejemplo, el formulario de gasto y sus selectores).
 */
@Composable
private inline fun <reified VM : androidx.lifecycle.ViewModel> sharedViewModel(
    navController: NavHostController,
    entry: NavBackStackEntry,
    parentRoute: String
): VM {
    val parentEntry = remember(entry) { navController.getBackStackEntry(parentRoute) }
    return viewModel(viewModelStoreOwner = parentEntry, factory = AppViewModelProvider.Factory)
}

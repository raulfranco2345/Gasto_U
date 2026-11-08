package com.example.gastou.navigation

/**
 * Definición central de rutas. Las pantallas de detalle reciben solo el id
 * (argumento mínimo); el ViewModel busca el registro completo.
 */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val LOCK = "lock"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val MODIFY_BUDGET = "budget"

    const val ADD_EXPENSE = "expense/add"
    const val SELECT_CATEGORY = "expense/add/category"
    const val SELECT_DATE = "expense/add/date"
    const val SELECT_PAYMENT_METHOD = "expense/add/payment"

    const val HISTORY = "history"
    const val ARG_EXPENSE_ID = "expenseId"
    const val EXPENSE_DETAIL = "history/{$ARG_EXPENSE_ID}"
    fun expenseDetail(expenseId: String) = "history/$expenseId"

    const val GOALS = "goals"
    const val ADD_GOAL = "goals/add"
    const val ARG_GOAL_ID = "goalId"
    const val GOAL_DETAIL = "goals/detail/{$ARG_GOAL_ID}"
    fun goalDetail(goalId: String) = "goals/detail/$goalId"

    /** Rutas que no requieren sesión iniciada. */
    val publicRoutes = setOf(LOGIN, REGISTER)
}

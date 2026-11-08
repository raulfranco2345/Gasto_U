package com.example.gastou.data.remote.api

import com.example.gastou.data.remote.dto.AuthResponseDto
import com.example.gastou.data.remote.dto.BudgetDto
import com.example.gastou.data.remote.dto.BudgetRequestDto
import com.example.gastou.data.remote.dto.ExpenseDto
import com.example.gastou.data.remote.dto.ExpenseRequestDto
import com.example.gastou.data.remote.dto.GoalDto
import com.example.gastou.data.remote.dto.GoalRequestDto
import com.example.gastou.data.remote.dto.LoginRequestDto
import com.example.gastou.data.remote.dto.MonthlySummaryDto
import com.example.gastou.data.remote.dto.RegisterRequestDto
import com.example.gastou.data.remote.dto.UpdateProfileRequestDto
import com.example.gastou.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints de la API propia de GastoU (carpeta backend/ del repositorio).
 * El token JWT se agrega automáticamente con AuthInterceptor.
 */
interface GastoUApi {

    // ---- Autenticación y perfil ----
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    @GET("api/auth/me")
    suspend fun getProfile(): UserDto

    @PUT("api/auth/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequestDto): UserDto

    // ---- Gastos ----
    @GET("api/expenses")
    suspend fun getExpenses(@Query("month") month: String? = null): List<ExpenseDto>

    @GET("api/expenses/summary")
    suspend fun getMonthlySummary(@Query("month") month: String): MonthlySummaryDto

    @PUT("api/expenses/{clientId}")
    suspend fun upsertExpense(
        @Path("clientId") clientId: String,
        @Body body: ExpenseRequestDto
    ): ExpenseDto

    @DELETE("api/expenses/{clientId}")
    suspend fun deleteExpense(@Path("clientId") clientId: String): Response<Unit>

    // ---- Metas de ahorro ----
    @GET("api/goals")
    suspend fun getGoals(): List<GoalDto>

    @PUT("api/goals/{clientId}")
    suspend fun upsertGoal(
        @Path("clientId") clientId: String,
        @Body body: GoalRequestDto
    ): GoalDto

    @DELETE("api/goals/{clientId}")
    suspend fun deleteGoal(@Path("clientId") clientId: String): Response<Unit>

    // ---- Presupuestos ----
    @GET("api/budgets")
    suspend fun getBudgets(): List<BudgetDto>

    @PUT("api/budgets/{month}")
    suspend fun upsertBudget(
        @Path("month") month: String,
        @Body body: BudgetRequestDto
    ): BudgetDto
}

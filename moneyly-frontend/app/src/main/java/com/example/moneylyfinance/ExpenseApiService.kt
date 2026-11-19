package com.example.moneylyfinance

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
interface ExpenseApiService {
    @GET("api/expenses")
    suspend fun getExpenses(): List<ApiExpense>
    @POST("api/expenses")
    suspend fun createExpense(
        @Body expense: CreateExpenseRequest
    ): ApiExpense

    @PUT("api/expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: Long,
        @Body expense: ApiExpense
    ): ApiExpense

    @DELETE("api/expenses/{id}")
    suspend fun deleteExpense(
        @Path("id") id: Long
    ): Response<Unit>
    @POST("api/ai/predict-category")
    suspend fun predictCategory(
        @Body request: AiCategoryRequest
    ): AiCategoryResponse

    @POST("api/ai/predict-spending")
    suspend fun predictSpending(
        @Body request: SpendingPredictionRequest
    ): SpendingPredictionResponse
}

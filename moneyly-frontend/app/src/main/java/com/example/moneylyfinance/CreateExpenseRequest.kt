package com.example.moneylyfinance

data class CreateExpenseRequest(
    val description: String,
    val amount: Double,
    val category: String,
    val expenseDate: String
)
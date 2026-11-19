package com.example.moneylyfinance

data class ApiExpense(
    val id: Long,
    val description: String,
    val amount: Double,
    val category: String,
    val expenseDate: String
)

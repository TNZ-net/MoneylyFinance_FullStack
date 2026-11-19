package com.example.moneylyfinance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: Int = 1,
    val monthlyLimit: Double
)

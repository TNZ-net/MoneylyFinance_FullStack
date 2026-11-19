package com.example.moneylyfinance

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val amount: Double,
    val date: String,
    val category: String = "Other",
    val isSynced: Boolean = false,
    val serverId: Long? = null
)
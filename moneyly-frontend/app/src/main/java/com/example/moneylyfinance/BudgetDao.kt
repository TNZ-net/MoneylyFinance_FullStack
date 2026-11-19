package com.example.moneylyfinance

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    //Observe the saved budget
    @Query("SELECT * FROM budgets WHERE id = 1")
    fun getBudget(): Flow<BudgetEntity?>
    @Upsert
    suspend fun saveBudget(budget: BudgetEntity)
}

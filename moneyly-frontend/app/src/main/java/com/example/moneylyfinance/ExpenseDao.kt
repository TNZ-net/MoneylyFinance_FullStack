package com.example.moneylyfinance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: Int): ExpenseEntity?

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date = :selectedDate ORDER BY id DESC")
    fun getExpensesByDate(selectedDate: String): Flow<List<ExpenseEntity>>

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Int)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("""
    UPDATE expenses
    SET isSynced = 1, serverId = :serverId
    WHERE id = :expenseId
""")
    suspend fun markExpenseAsSynced(
        expenseId: Int,
        serverId: Long
    )

    @Query("""
    UPDATE expenses
    SET isSynced = 0
    WHERE id = :expenseId
""")
    suspend fun markExpenseAsPending(expenseId: Int)

    @Query("SELECT * FROM expenses WHERE isSynced = 0 ORDER BY id ASC")
    suspend fun getPendingExpenses(): List<ExpenseEntity>
}


package com.example.moneylyfinance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class FinanceViewModel(
    private val expenseDao: ExpenseDao
) : ViewModel() {

    val expenses: Flow<List<ExpenseEntity>> =
        expenseDao.getAllExpenses()

    val totalExpenses: Flow<Double> =
        expenses.map { expenseList ->
            expenseList.sumOf { it.amount }
        }

    val monthlyExpenses: Flow<Double> =
        expenses.map { expenseList ->
            val currentMonth = SimpleDateFormat(
                "yyyy-MM",
                Locale.getDefault()
            ).format(Date())

            expenseList
                .filter { it.date.startsWith(currentMonth) }
                .sumOf { it.amount }
        }
}

class FinanceViewModelFactory(
    private val expenseDao: ExpenseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java))
        {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(expenseDao) as T
        }
        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
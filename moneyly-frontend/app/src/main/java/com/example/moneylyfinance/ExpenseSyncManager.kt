package com.example.moneylyfinance

class ExpenseSyncManager(
    private val expenseDao: ExpenseDao
)
{
    suspend fun syncPendingExpenses(): String {
        val pendingExpenses = expenseDao.getPendingExpenses()
        var syncedCount = 0

        for (expense in pendingExpenses)
        {
            try
            {
                if (expense.serverId != null) {
                    //This expense already exists on the server-update it.
                    RetrofitClient.api.updateExpense(
                        id = expense.serverId,
                        expense = ApiExpense(
                            id = expense.serverId,
                            description = expense.description,
                            amount = expense.amount,
                            category = expense.category,
                            expenseDate = expense.date
                        )
                    )
                    expenseDao.markExpenseAsSynced(
                        expenseId = expense.id,
                        serverId = expense.serverId
                    )
                }
                else
                {
                    val serverExpense = RetrofitClient.api.createExpense(
                        CreateExpenseRequest(
                            description = expense.description,
                            amount = expense.amount,
                            category = expense.category,
                            expenseDate = expense.date
                        )
                    )

                    expenseDao.markExpenseAsSynced(
                        expenseId = expense.id,
                        serverId = serverExpense.id
                    )
                }

                syncedCount++
            }
            catch (e: Exception)
            {
                return "Synced $syncedCount. Upload failed: ${e.message}"
            }
        }
        return "Sync complete: $syncedCount expense(s) uploaded."
    }
    suspend fun syncExpenseById(expenseId: Int): String {
        val expense = expenseDao.getExpenseById(expenseId)
            ?: return "Expense not found locally."

        if (expense.isSynced){
            return "Expense is already synced."
        }

        return try {
            if (expense.serverId != null)
            {
                // Update the existing backend expense.
                RetrofitClient.api.updateExpense(
                    id = expense.serverId,
                    expense = ApiExpense(
                        id = expense.serverId,
                        description = expense.description,
                        amount = expense.amount,
                        category = expense.category,
                        expenseDate = expense.date
                    )
                )

                expenseDao.markExpenseAsSynced(
                    expenseId = expense.id,
                    serverId = expense.serverId
                )
            }
            else
            {
                val serverExpense = RetrofitClient.api.createExpense(
                    CreateExpenseRequest(
                        description = expense.description,
                        amount = expense.amount,
                        category = expense.category,
                        expenseDate = expense.date
                    )
                )

                expenseDao.markExpenseAsSynced(
                    expenseId = expense.id,
                    serverId = serverExpense.id
                )
            }

            "Synced successfully."
        }
        catch (e: Exception)
        {
            "Saved locally; sync pending."
        }
    }
}

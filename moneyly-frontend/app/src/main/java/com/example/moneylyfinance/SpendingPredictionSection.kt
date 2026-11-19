
package com.example.moneylyfinance

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SpendingPredictionSection(expenseDao: ExpenseDao) {
    val expenses by expenseDao.getAllExpenses()
        .collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()
    var prediction by remember { mutableStateOf<Double?>(null) }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    //YYYY-MM and total each month.
    val monthlyTotals = expenses
        .filter { it.date.length >= 7 }
        .groupBy { it.date.take(7) }
        .toSortedMap()
        .values
        .map { monthExpenses ->
            monthExpenses.sumOf { it.amount }
        }

    SageCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "AI spending prediction",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val predicted = prediction
                if (predicted != null) {
                    Text(
                        "RM %.2f".format(predicted),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Predicted next month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else {
                    Text(
                        "Based on your saved expenses.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.width(12.dp))
            SageTonalButton(
                text = if (loading) "Predicting..." else "Predict Spending",
                enabled = !loading,

                onClick = {
                    if (monthlyTotals.size < 2) {
                        prediction = null
                        message = "Add expenses across at least 2 different months first."
                    } else {
                        scope.launch {
                            loading = true
                            prediction = null
                            message = ""

                            try {
                                val response = RetrofitClient.api.predictSpending(
                                    SpendingPredictionRequest(monthlyTotals)
                                )
                                prediction = response.predictedSpending
                            } catch (e: Exception) {
                                message = "Prediction failed. Check backend connection."
                            } finally {
                                loading = false
                            }
                        }
                    }
                }
            )
        }

        Text(
            "Months of real expense data: ${monthlyTotals.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            "Monthly totals: ${monthlyTotals.joinToString { "%.2f".format(it) }}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (message.isNotEmpty()) {
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
package com.example.moneylyfinance
data class SpendingPredictionRequest(
    val monthlySpending: List<Double>
)

data class SpendingPredictionResponse(
    val predictedSpending: Double
)
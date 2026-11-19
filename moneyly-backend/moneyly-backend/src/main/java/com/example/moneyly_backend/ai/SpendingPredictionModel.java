
package com.example.moneyly_backend.ai;

public class SpendingPredictionModel {

    public double predictNextMonth(double[] monthlySpending) {
        if (monthlySpending == null || monthlySpending.length < 2) {
            throw new IllegalArgumentException(
                    "At least two months of spending are required."
            );
        }

        int n = monthlySpending.length;
        //X = month index: 0, 1, 2, 3...
        //Y = spending amount for each month.
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;

        for (int i = 0; i < n; i++)
        {
            sumX += i;
            sumY += monthlySpending[i];
            sumXY += i * monthlySpending[i];
            sumXX += i * i;
        }

        //Fit the line: y = mx + intercept
        double denominator = n * sumXX - sumX * sumX;
        double slope =
                (n * sumXY - sumX * sumY) / denominator;
        double intercept =
                (sumY - slope * sumX) / n;
        //Predict the month immediately after the last one.
        double prediction = slope * n + intercept;
        //Spending cannot be negative.
        return Math.max(0.0, prediction);
    }
}
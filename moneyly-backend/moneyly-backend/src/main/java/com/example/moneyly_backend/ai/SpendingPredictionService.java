package com.example.moneyly_backend.ai;

import org.springframework.stereotype.Service;

@Service
public class SpendingPredictionService
{
    private final SpendingPredictionModel model =
            new SpendingPredictionModel();

    public double predict(double[] monthlySpending) {
        return model.predictNextMonth(monthlySpending);
    }
}
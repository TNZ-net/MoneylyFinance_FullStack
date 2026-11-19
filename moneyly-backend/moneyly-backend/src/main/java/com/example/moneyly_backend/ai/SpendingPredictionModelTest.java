package com.example.moneyly_backend.ai;

public class SpendingPredictionModelTest
{
    public static void main(String[] args) {
        SpendingPredictionModel model =
                new SpendingPredictionModel();

        double result1 = model.predictNextMonth(
                new double[]{200, 250, 300}
        );

        System.out.println("Rising trend: " + result1);
        if (Math.abs(result1 - 350) > 0.01) {
            throw new RuntimeException("Rising trend test failed");
        }

        double result2 = model.predictNextMonth(
                new double[]{100, 100, 100}
        );

        System.out.println("Stable trend: " + result2);
        if (Math.abs(result2 - 100) > 0.01) {
            throw new RuntimeException("Stable trend test failed");
        }

        double result3 = model.predictNextMonth(
                new double[]{200, 250, 300, 350}
        );
        System.out.println("Next month forecast: " + result3);
        if (Math.abs(result3 - 400) > 0.01) {
            throw new RuntimeException("Forecast test failed");
        }
        System.out.println("All tests passed!");
    }
}
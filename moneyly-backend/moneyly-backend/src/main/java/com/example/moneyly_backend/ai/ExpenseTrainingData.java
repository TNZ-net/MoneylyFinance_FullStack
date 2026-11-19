package com.example.moneyly_backend.ai;

import java.util.List;

public class ExpenseTrainingData {

    public record Example(String description, String category) {}
    public static List<Example> getExamples() {
        return List.of(
                new Example("lunch", "Food"),
                new Example("pizza delivery", "Food"),
                new Example("grocery shopping", "Food"),
                new Example("coffee", "Food"),
                new Example("Grab ride", "Transport"),
                new Example("bus fare", "Transport"),
                new Example("petrol", "Transport"),
                new Example("train ticket", "Transport"),
                new Example("new shoes", "Shopping"),
                new Example("buy clothes", "Shopping"),
                new Example("online shopping", "Shopping"),
                new Example("electricity bill", "Bills"),
                new Example("water bill", "Bills"),
                new Example("internet bill", "Bills"),
                new Example("miscellaneous expense", "Other"),
                new Example("repair fee", "Other")
        );
    }
}

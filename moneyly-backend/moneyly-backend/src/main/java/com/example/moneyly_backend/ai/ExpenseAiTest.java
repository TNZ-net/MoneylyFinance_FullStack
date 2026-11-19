package com.example.moneyly_backend.ai;

public class ExpenseAiTest
{
    public static void main(String[] args)
    {
        ExpenseNaiveBayes model = new ExpenseNaiveBayes();
        model.train(ExpenseTrainingData.getExamples());
        test(model, "fried rice", "Food");
        test(model, "Grab ride", "Transport");
        test(model, "buy shoes", "Shopping");
        test(model, "electricity bill", "Bills");
        test(model, "repair fee", "Other");
    }

    private static void test(
            ExpenseNaiveBayes model,
            String description,
            String expected
    )
    {
        String predicted = model.predict(description);
        System.out.println(
                description + " -> " + predicted
                        + " | Expected: " + expected
                        + " | " + (predicted.equals(expected) ? "PASS" : "CHECK")
        );
    }
}
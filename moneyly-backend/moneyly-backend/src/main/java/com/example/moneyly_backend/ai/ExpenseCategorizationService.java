package com.example.moneyly_backend.ai;

import org.springframework.stereotype.Service;

@Service
public class ExpenseCategorizationService {
    private final ExpenseNaiveBayes model = new ExpenseNaiveBayes();
    public ExpenseCategorizationService() {
        model.train(ExpenseTrainingData.getExamples());
    }
    public String predictCategory(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Expense description must not be empty"
            );
        }
        return model.predict(description);
    }
}
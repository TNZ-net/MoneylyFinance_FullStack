package com.example.moneyly_backend.controller;

import com.example.moneyly_backend.ExpenseEntity;
import com.example.moneyly_backend.ExpenseService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    @Cacheable("expenses")
    public List<ExpenseEntity> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    //POST /api/expenses creates a new expense.
    @PostMapping
    @CacheEvict(value = "expenses", allEntries = true)
    public ExpenseEntity createExpense(
            @Valid @RequestBody ExpenseEntity expense) {

        return expenseService.saveExpense(expense);
    }

    //PUT /api/expenses/2 updates expense ID 2.
    @PutMapping("/{id}")
    @CacheEvict(value = "expenses", allEntries = true)
    public ExpenseEntity updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseEntity expense) {

        return expenseService.updateExpense(id, expense);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "expenses", allEntries = true)
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id) {

        expenseService.deleteExpense(id);

        return ResponseEntity.noContent().build();
    }
}

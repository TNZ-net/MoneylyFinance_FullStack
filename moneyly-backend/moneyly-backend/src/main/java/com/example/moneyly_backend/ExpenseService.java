
package com.example.moneyly_backend;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
public class ExpenseService
{
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    //Retrieve
    public List<ExpenseEntity> getAllExpenses() {
        return expenseRepository.findAll();
    }

    //Save
    public ExpenseEntity saveExpense(ExpenseEntity expense) {
        return expenseRepository.save(expense);
    }

    //Update
    public ExpenseEntity updateExpense(
            Long id, ExpenseEntity updatedExpense) {

        ExpenseEntity existingExpense =
                expenseRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Expense not found: " + id));
        existingExpense.setDescription(
                updatedExpense.getDescription());
        existingExpense.setAmount(
                updatedExpense.getAmount());
        existingExpense.setCategory(
                updatedExpense.getCategory());
        existingExpense.setExpenseDate(
                updatedExpense.getExpenseDate());
        return expenseRepository.save(existingExpense);
    }

    //Del existing expense
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Expense not found: " + id);
        }

        expenseRepository.deleteById(id);
    }
}
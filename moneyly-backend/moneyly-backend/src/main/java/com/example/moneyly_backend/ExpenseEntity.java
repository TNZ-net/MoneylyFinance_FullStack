
package com.example.moneyly_backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
@Entity
@Table(name = "expenses")
public class ExpenseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String description;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String category;

    @NotNull
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    public ExpenseEntity() {
    }

    public Long getId() {
        return id;
    }
    public String getDescription() {
        return description;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public String getCategory() {
        return category;
    }
    public LocalDate getExpenseDate() {
        return expenseDate;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }
}
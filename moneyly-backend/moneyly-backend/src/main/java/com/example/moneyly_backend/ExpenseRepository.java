package com.example.moneyly_backend;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository
        extends JpaRepository<ExpenseEntity, Long> {
}
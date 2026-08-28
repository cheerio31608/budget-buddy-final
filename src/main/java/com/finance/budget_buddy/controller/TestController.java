package com.finance.budget_buddy.controller;

import com.finance.budget_buddy.entity.Transaction;
import com.finance.budget_buddy.service.TransactionService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.List;

@RestController
@Profile("local")
@RequestMapping("/api/test")
public class TestController {

    private final TransactionService transactionService;

    public TestController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/db")
    public List<Transaction> testDb() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/expense/{userId}")
    public BigDecimal getTotalExpense(@PathVariable Long userId) {
        return transactionService.getTotalExpenseByUserId(userId);
    }
}

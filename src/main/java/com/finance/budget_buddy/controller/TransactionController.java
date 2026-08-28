package com.finance.budget_buddy.controller;

import com.finance.budget_buddy.dto.TransactionCreateRequest;
import com.finance.budget_buddy.entity.Transaction;
import com.finance.budget_buddy.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionCreateRequest request) {
        Transaction transaction = transactionService.createTransaction(request);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(transactionService.getTransactions(userId));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }
}

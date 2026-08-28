package com.finance.budget_buddy.dto;

import com.finance.budget_buddy.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCreateRequest(
        @NotNull(message = "userId is required.")
        Long userId,

        @NotNull(message = "categoryId is required.")
        Long categoryId,

        @NotNull(message = "amount is required.")
        @Positive(message = "amount must be positive.")
        BigDecimal amount,

        @NotNull(message = "transactionType is required.")
        TransactionType transactionType,

        String description,

        @NotNull(message = "transactionAt is required.")
        @PastOrPresent(message = "transactionAt must not be in the future.")
        LocalDateTime transactionAt,

        @Size(max = 100, message = "idempotencyKey must be 100 characters or less.")
        String idempotencyKey
) {
}

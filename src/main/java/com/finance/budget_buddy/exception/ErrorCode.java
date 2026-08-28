package com.finance.budget_buddy.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "Method not allowed."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal server error."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "Category not found."),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "Transaction not found."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "T002", "Insufficient balance."),
    TRANSACTION_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "T003", "Transaction type does not match category type.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

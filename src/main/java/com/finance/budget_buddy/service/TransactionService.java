package com.finance.budget_buddy.service;

import com.finance.budget_buddy.dto.TransactionCreateRequest;
import com.finance.budget_buddy.entity.Category;
import com.finance.budget_buddy.entity.Transaction;
import com.finance.budget_buddy.entity.TransactionType;
import com.finance.budget_buddy.entity.User;
import com.finance.budget_buddy.exception.BusinessException;
import com.finance.budget_buddy.exception.ErrorCode;
import com.finance.budget_buddy.repository.CategoryRepository;
import com.finance.budget_buddy.repository.TransactionRepository;
import com.finance.budget_buddy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates a transaction and updates the user's balance in one DB transaction.
     * Pessimistic locking serializes concurrent payments for the same user.
     */
    @Transactional
    public Transaction createTransaction(TransactionCreateRequest request) {
        User user = userRepository.findByIdForUpdate(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            return transactionRepository.findByUserIdAndIdempotencyKey(user.getUserId(), request.idempotencyKey())
                    .orElseGet(() -> createNewTransaction(request, user));
        }

        return createNewTransaction(request, user);
    }

    private Transaction createNewTransaction(TransactionCreateRequest request, User user) {
        Category category = categoryRepository.findByCategoryIdAndUserId(request.categoryId(), user.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (request.transactionAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (category.getType() != request.transactionType()) {
            throw new BusinessException(ErrorCode.TRANSACTION_TYPE_MISMATCH);
        }

        if (TransactionType.EXPENSE == request.transactionType()
                && user.getBalance().compareTo(request.amount()) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        BigDecimal balanceBefore = user.getBalance();
        user.updateBalance(request.amount(), request.transactionType());

        Transaction transaction = Transaction.builder()
                .userId(user.getUserId())
                .categoryId(category.getCategoryId())
                .amount(request.amount())
                .balanceBefore(balanceBefore)
                .balanceAfter(user.getBalance())
                .transactionType(request.transactionType())
                .description(request.description())
                .transactionAt(request.transactionAt())
                .idempotencyKey(request.idempotencyKey())
                .build();

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactions(Long userId) {
        if (userId == null) {
            return getAllTransactions();
        }

        return transactionRepository.findByUserIdOrderByTransactionAtDesc(userId);
    }

    public Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    public BigDecimal getTotalExpenseByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionType(userId, TransactionType.EXPENSE);

        if (transactions.isEmpty()) {
            throw new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND);
        }

        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

package com.finance.budget_buddy.repository;

import com.finance.budget_buddy.entity.Transaction;
import com.finance.budget_buddy.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdAndTransactionType(Long userId, TransactionType transactionType);
    List<Transaction> findByUserIdAndTransactionAtAfterOrderByTransactionAtDesc(Long userId, LocalDateTime after);
    List<Transaction> findByUserIdOrderByTransactionAtDesc(Long userId);
    Optional<Transaction> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}

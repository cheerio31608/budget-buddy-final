package com.finance.budget_buddy.repository;

import com.finance.budget_buddy.entity.Transaction;
import com.finance.budget_buddy.entity.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryQueryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Category query returns only categories owned by the user")
    void findByCategoryIdAndUserId_returnsOwnedCategoryOnly() {
        assertThat(categoryRepository.findByCategoryIdAndUserId(2L, 1L)).isPresent();
        assertThat(categoryRepository.findByCategoryIdAndUserId(2L, 999L)).isEmpty();
    }

    @Test
    @DisplayName("Idempotency query finds duplicate transaction for the same user")
    void findByUserIdAndIdempotencyKey_findsSameUserDuplicate() {
        Transaction transaction = saveTransaction(1L, 2L, "idem-001");

        assertThat(transactionRepository.findByUserIdAndIdempotencyKey(1L, "idem-001"))
                .contains(transaction);
        assertThat(transactionRepository.findByUserIdAndIdempotencyKey(1L, "missing"))
                .isEmpty();
    }

    @Test
    @DisplayName("User transaction query orders newest transaction first")
    void findByUserIdOrderByTransactionAtDesc_ordersNewestFirst() {
        saveTransaction(1L, 2L, "old", LocalDateTime.now().minusDays(2));
        Transaction latest = saveTransaction(1L, 2L, "latest", LocalDateTime.now());

        assertThat(transactionRepository.findByUserIdOrderByTransactionAtDesc(1L))
                .first()
                .isEqualTo(latest);
    }

    private Transaction saveTransaction(Long userId, Long categoryId, String idempotencyKey) {
        return saveTransaction(userId, categoryId, idempotencyKey, LocalDateTime.now());
    }

    private Transaction saveTransaction(Long userId, Long categoryId, String idempotencyKey, LocalDateTime transactionAt) {
        return transactionRepository.save(Transaction.builder()
                .userId(userId)
                .categoryId(categoryId)
                .amount(new BigDecimal("1000.00"))
                .balanceBefore(new BigDecimal("10000.00"))
                .balanceAfter(new BigDecimal("9000.00"))
                .transactionType(TransactionType.EXPENSE)
                .description("test")
                .transactionAt(transactionAt)
                .idempotencyKey(idempotencyKey)
                .build());
    }
}

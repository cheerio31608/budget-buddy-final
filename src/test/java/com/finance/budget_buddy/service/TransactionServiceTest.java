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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .passwordHash("hash")
                .balance(new BigDecimal("10000.00"))
                .build();

        expenseCategory = Category.builder()
                .categoryId(1L)
                .userId(1L)
                .name("Food")
                .type(TransactionType.EXPENSE)
                .build();
    }

    @Test
    @DisplayName("Expense transaction decreases balance and stores snapshots")
    void createTransaction_Success_Expense() {
        TransactionCreateRequest request = createRequest(1000L, TransactionType.EXPENSE);
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(categoryRepository.findByCategoryIdAndUserId(1L, 1L)).willReturn(Optional.of(expenseCategory));
        given(transactionRepository.save(any(Transaction.class))).willAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(request);

        assertThat(result.getAmount()).isEqualByComparingTo("1000.00");
        assertThat(result.getBalanceBefore()).isEqualByComparingTo("10000.00");
        assertThat(result.getBalanceAfter()).isEqualByComparingTo("9000.00");
        assertThat(user.getBalance()).isEqualByComparingTo("9000.00");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Expense over current balance throws INSUFFICIENT_BALANCE")
    void createTransaction_Fail_InsufficientBalance() {
        TransactionCreateRequest request = createRequest(20000L, TransactionType.EXPENSE);
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(categoryRepository.findByCategoryIdAndUserId(1L, 1L)).willReturn(Optional.of(expenseCategory));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_BALANCE);
    }

    @Test
    @DisplayName("Future transaction date throws INVALID_INPUT_VALUE")
    void createTransaction_Fail_FutureDate() {
        TransactionCreateRequest request = createRequest(1000L, TransactionType.EXPENSE, LocalDateTime.now().plusDays(1));
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(categoryRepository.findByCategoryIdAndUserId(1L, 1L)).willReturn(Optional.of(expenseCategory));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("Category owned by another user is rejected")
    void createTransaction_Fail_CategoryOwnership() {
        TransactionCreateRequest request = createRequest(1000L, TransactionType.EXPENSE);
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(categoryRepository.findByCategoryIdAndUserId(1L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
    }

    private TransactionCreateRequest createRequest(long amount, TransactionType type) {
        return createRequest(amount, type, LocalDateTime.now());
    }

    private TransactionCreateRequest createRequest(long amount, TransactionType type, LocalDateTime time) {
        return new TransactionCreateRequest(
                1L,
                1L,
                new BigDecimal(amount),
                type,
                null,
                time,
                null);
    }
}

package com.finance.budget_buddy.service;

import com.finance.budget_buddy.dto.TransactionCreateRequest;
import com.finance.budget_buddy.entity.Category;
import com.finance.budget_buddy.entity.TransactionType;
import com.finance.budget_buddy.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Profile("local")
@RequiredArgsConstructor
public class TestDataService {

    private final TransactionService transactionService;
    private final CategoryRepository categoryRepository;
    private final Random random = new Random();

    @Transactional
    public void generateRandomTransactions(Long userId, int count) {
        List<Category> categories = categoryRepository.findAll();

        transactionService.createTransaction(new TransactionCreateRequest(
                userId,
                1L,
                BigDecimal.valueOf(10000000),
                TransactionType.INCOME,
                "Initial test balance",
                LocalDateTime.now().minusDays(91),
                null));

        for (int i = 0; i < count; i++) {
            Category category = categories.get(random.nextInt(categories.size()));
            TransactionType type = category.getType();
            BigDecimal amount = generateRandomAmount(type);
            LocalDateTime date = LocalDateTime.now().minusDays(random.nextInt(90))
                    .minusHours(random.nextInt(24))
                    .minusMinutes(random.nextInt(60));

            TransactionCreateRequest request = new TransactionCreateRequest(
                    userId,
                    category.getCategoryId(),
                    amount,
                    type,
                    category.getName() + " - test data " + (i + 1),
                    date,
                    null);

            transactionService.createTransaction(request);
        }
    }

    private BigDecimal generateRandomAmount(TransactionType type) {
        if (TransactionType.INCOME == type) {
            int val = 100 + random.nextInt(201);
            return BigDecimal.valueOf(val * 10000L);
        }

        int val = 10 + random.nextInt(991);
        return BigDecimal.valueOf(val * 100L);
    }
}

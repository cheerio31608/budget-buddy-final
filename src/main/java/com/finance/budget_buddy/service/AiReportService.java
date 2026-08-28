package com.finance.budget_buddy.service;

import com.finance.budget_buddy.entity.AiReport;
import com.finance.budget_buddy.entity.Transaction;
import com.finance.budget_buddy.repository.AiReportRepository;
import com.finance.budget_buddy.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiReportService {

    private static final String MONTHLY_REPORT = "MONTHLY";

    private final GeminiClient geminiClient;
    private final TransactionRepository transactionRepository;
    private final AiReportRepository aiReportRepository;

    @Transactional
    public String generateMonthlyReport(Long userId) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionAtAfterOrderByTransactionAtDesc(userId, oneMonthAgo);

        if (transactions.isEmpty()) {
            return "No transactions found in the last 30 days.";
        }

        String transactionSummary = transactions.stream()
                .map(t -> String.format("- %s: [%s] %s %s (categoryId=%s)",
                        t.getTransactionAt().toLocalDate(),
                        t.getTransactionType(),
                        t.getDescription(),
                        t.getAmount().toPlainString(),
                        t.getCategoryId()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
                You are Budget Buddy AI, a financial analysis assistant.
                Analyze this user's last 30 days of household transactions.

                Transactions:
                %s

                Please respond in Korean with:
                1. A concise spending pattern summary.
                2. Three concrete saving suggestions.
                3. A financial health score out of 100 with a short reason.
                """, transactionSummary);

        String report = geminiClient.generateContent(prompt);

        aiReportRepository.save(AiReport.builder()
                .userId(userId)
                .reportType(MONTHLY_REPORT)
                .reportContent(report)
                .build());

        return report;
    }

    public List<AiReport> getReports(Long userId) {
        return aiReportRepository.findByUserIdOrderByGeneratedAtDesc(userId);
    }
}

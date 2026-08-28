package com.finance.budget_buddy.controller;

import com.finance.budget_buddy.entity.AiReport;
import com.finance.budget_buddy.service.AiReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiReportController {

    private final AiReportService aiReportService;

    /**
     * 사용자의 AI 맞춤형 한 달 지출 분석 보고서를 생성합니다.
     */
    @GetMapping("/report/monthly")
    public ResponseEntity<String> getMonthlyReport(@RequestParam Long userId) {
        String report = aiReportService.generateMonthlyReport(userId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports")
    public ResponseEntity<List<AiReport>> getReports(@RequestParam Long userId) {
        return ResponseEntity.ok(aiReportService.getReports(userId));
    }
}

package com.finance.budget_buddy.controller;

import com.finance.budget_buddy.service.TestDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("local")
@RequestMapping("/api/test/data")
@RequiredArgsConstructor
public class TestDataController {

    private final TestDataService testDataService;

    /**
     * 특정 사용자에게 랜덤한 거래 데이터를 생성합니다.
     * 
     * @param userId 생성 대상 사용자 ID
     * @param count  생성할 거래 내역 개수
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateData(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "30") int count) {
        
        testDataService.generateRandomTransactions(userId, count);
        
        return ResponseEntity.ok(count + "개의 테스트 데이터가 성공적으로 생성되었습니다.");
    }
}

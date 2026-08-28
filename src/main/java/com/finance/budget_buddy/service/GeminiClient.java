package com.finance.budget_buddy.service;

import com.finance.budget_buddy.dto.gemini.GeminiRequest;
import com.finance.budget_buddy.dto.gemini.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

    private final RestClient restClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiClient() {
        this.restClient = RestClient.builder().build();
    }

    /**
     * Gemini API에 텍스트 프롬프트를 전송하고 응답을 받습니다.
     */
    public String generateContent(String prompt) {
        GeminiRequest request = GeminiRequest.fromText(prompt);

        GeminiResponse response = restClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GeminiResponse.class);

        return response != null ? response.getText() : "응답을 생성할 수 없습니다.";
    }
}

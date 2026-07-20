package com.example.demo.service;

import com.example.demo.dto.ChatbotRequest;
import com.example.demo.dto.ChatbotResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiChatService {

    @Value("${devquery.ai.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    private static final String SYSTEM_PROMPT = "You are 'DevQuery Assistant,' an elite, senior-level software engineering AI integrated natively into the DevQuery platform.\n"
            +
            "Primary Mission: Assist developers in debugging code, explaining technical concepts, database design, and navigating the forum.\n"
            +
            "Strict Boundaries:\n" +
            "- You MUST decline ANY non-technical questions (e.g., politics, cooking, health). Fallback response: \"I am the DevQuery Assistant, specialized in software engineering. Please ask a programming-related question.\"\n"
            +
            "- Do NOT generate harmful, malicious, or hacking-related code.\n" +
            "Formatting Rules:\n" +
            "- Keep responses concise. Developers value time.\n" +
            "- Wrap all code snippets in standard Markdown formatting (e.g., ```java).\n" +
            "- Use bold text for file names and key variables.\n\n";

    public AiChatService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public ChatbotResponse getChatResponse(ChatbotRequest request) {
        if (apiKey == null || apiKey.isEmpty()) {
            return new ChatbotResponse("AI Provider is not configured.");
        }

        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key="
                + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct Gemini Payload
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> contentMap = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> partMap = new HashMap<>();

        partMap.put("text", SYSTEM_PROMPT + "User Request:\n" + request.getMessage());
        parts.add(partMap);
        contentMap.put("parts", parts);
        contents.add(contentMap);
        requestBody.put("contents", contents);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    if (content != null && content.containsKey("parts")) {
                        List<Map<String, String>> resParts = (List<Map<String, String>>) content.get("parts");
                        if (resParts != null && !resParts.isEmpty()) {
                            return new ChatbotResponse(resParts.get(0).get("text"));
                        }
                    }
                }
            }
            return new ChatbotResponse("Sorry, I could not generate a response.");
        } catch (Exception e) {
            e.printStackTrace();
            return new ChatbotResponse("Lỗi API: " + e.getMessage());
        }
    }
}

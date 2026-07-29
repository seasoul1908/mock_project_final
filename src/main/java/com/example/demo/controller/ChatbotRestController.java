package com.example.demo.controller;

import com.example.demo.dto.ChatbotRequest;
import com.example.demo.dto.ChatbotResponse;
import com.example.demo.service.AiChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class ChatbotRestController {

    private final AiChatService aiChatService;
    
    // Simple rate limiting: max 10 requests per minute per IP
    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();

    public ChatbotRestController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatbotResponse> chat(@RequestBody ChatbotRequest request, HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        
        // Rate limiting logic
        long now = System.currentTimeMillis();
        requestCounts.compute(clientIp, (ip, timestamps) -> {
            if (timestamps == null) {
                timestamps = new ArrayList<>();
            }
            // Remove timestamps older than 1 minute (60000 ms)
            timestamps.removeIf(t -> now - t > 60000);
            return timestamps;
        });
        
        if (requestCounts.get(clientIp).size() >= 10) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ChatbotResponse("Rate limit exceeded. Max 10 messages per minute."));
        }
        
        requestCounts.get(clientIp).add(now);

        ChatbotResponse response = aiChatService.getChatResponse(request);
        return ResponseEntity.ok(response);
    }
}

package com.example.demo.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CodeExecutionController {

    @PostMapping("/api/execute")
    public ResponseEntity<?> executeCode(@RequestBody String payload) {
        // Points to your local Docker container
        String pistonUrl = "http://localhost:2000/api/v2/piston/execute";
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(payload, headers);
        
        try {
            // Forward the exact JSON payload to the local Piston container
            ResponseEntity<String> response = restTemplate.postForEntity(pistonUrl, request, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
            errorResponse.put("message", "Failed to connect to local execution server. Ensure Docker is running. Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

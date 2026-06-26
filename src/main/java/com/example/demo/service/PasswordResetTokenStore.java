package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class PasswordResetTokenStore {
    private final Map<String, ResetTokenData> tokenMap = new ConcurrentHashMap<>();

    public String createToken(String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        tokenMap.put(token, new ResetTokenData(email, expiresAt));

        return token;
    }
    public String getEmailByValidToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        ResetTokenData data = tokenMap.get(token);

        if (data == null) {
            return null;
        }

        if (LocalDateTime.now().isAfter(data.expiresAt())) {
            tokenMap.remove(token);
            return null;
        }

        return data.email();
    }
    public void removeToken(String token) {
        if (token != null) {
            tokenMap.remove(token);
        }
    }

    private record ResetTokenData(String email, LocalDateTime expiresAt) {
    }

}

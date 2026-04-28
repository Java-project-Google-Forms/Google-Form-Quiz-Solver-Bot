package ru.spbstu.adminauth;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Component
public class TokenStore {
    
    private static final long TOKEN_TTL_MS = 15 * 60 * 1000; // 15 минут
    
    // Хранилище: токен -> timestamp создания
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();
    
    public String createToken() {
        String token = UUID.randomUUID().toString();
        tokens.put(token, System.currentTimeMillis());
        return token;
    }
    
    public boolean isValid(String token) {
        Long createdAt = tokens.get(token);
        if (createdAt == null) return false;
        
        boolean isValid = (System.currentTimeMillis() - createdAt) < TOKEN_TTL_MS;
        if (!isValid) {
            tokens.remove(token); // удаляем просроченный
        }
        return isValid;
    }
    
    public void invalidate(String token) {
        tokens.remove(token);
    }
}
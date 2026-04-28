package ru.spbstu.adminauth;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private final AdminAuthService authService;
    private final TokenStore tokenStore;
    
    public AdminController(AdminAuthService authService, TokenStore tokenStore) {
        this.authService = authService;
        this.tokenStore = tokenStore;
    }
    
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        boolean isAuthenticated = authService.authenticate(username, password);
        
        Map<String, String> response = new HashMap<>();
        if (isAuthenticated) {
            String token = tokenStore.createToken();
            response.put("status", "success");
            response.put("token", token);
            response.put("message", "Авторизация успешна, токен действителен 15 минут");
        } else {
            response.put("status", "error");
            response.put("message", "Неверное имя пользователя или пароль");
        }
        return response;
    }
    
    @GetMapping("/users")
    public Map<String, Object> getUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("status", "error");
            response.put("message", "Missing or invalid Authorization header");
            return response;
        }
        
        String token = authHeader.substring(7);
        if (!tokenStore.isValid(token)) {
            response.put("status", "error");
            response.put("message", "Token is invalid or expired");
            return response;
        }
        
        response.put("status", "success");
        response.put("users", "Список пользователей будет подключен позже (нужен UserService)");
        response.put("note", "Токен валиден, можно возвращать данные");
        return response;
    }
}
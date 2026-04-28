package ru.spbstu.adminauth;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private final AdminAuthService authService;
    
    public AdminController(AdminAuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        boolean isAuthenticated = authService.authenticate(username, password);
        
        Map<String, String> response = new HashMap<>();
        if (isAuthenticated) {
            response.put("status", "success");
            response.put("message", "Авторизация успешна");
        } else {
            response.put("status", "error");
            response.put("message", "Неверное имя пользователя или пароль");
        }
        return response;
    }
    
    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("users", "Список пользователей будет подключен позже");
        response.put("note", "Нужно добавить вызов UserService");
        return response;
    }
}

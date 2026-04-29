package ru.spbstu.adminauth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AdminController {

    private final AdminAuthService authService;

    public AdminController(AdminAuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> auth(@RequestParam("login") String login,
                                                          @RequestParam("pass") String pass) {
        return authService.issueKey(login, pass)
                .map(key -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("status", "success");
                    body.put("apiKey", key);
                    body.put("expiresInSeconds", AdminAuthService.KEY_TTL.toSeconds());
                    return ResponseEntity.ok(body);
                })
                .defaultIfEmpty(ResponseEntity.status(401).body(Map.of(
                        "status", "error",
                        "message", "Неверный логин или пароль"
                )));
    }
}

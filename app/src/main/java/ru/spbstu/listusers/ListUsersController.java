package ru.spbstu.listusers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.spbstu.database.repository.UserRepository;
import ru.spbstu.listusers.service.api.ApiKeyVerifier;

import java.util.Map;

@RestController
public class ListUsersController {

    private final ApiKeyVerifier apiKeyVerifier;
    private final UserRepository userRepository;

    public ListUsersController(ApiKeyVerifier apiKeyVerifier, UserRepository userRepository) {
        this.apiKeyVerifier = apiKeyVerifier;
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public Mono<ResponseEntity<Object>> listUsers(@RequestParam("key") String key) {
        if (!apiKeyVerifier.verify(key)) {
            return Mono.just(ResponseEntity.status(401).body(Map.of(
                    "status", "error",
                    "message", "Неверный или истёкший API-ключ"
            )));
        }
        return userRepository.findAll().collectList()
                .map(users -> ResponseEntity.ok((Object) users));
    }
}

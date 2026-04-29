package ru.spbstu.adminauth;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.spbstu.database.repository.AdminRepository;
import ru.spbstu.listusers.service.api.ApiKeyVerifier;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminAuthService implements ApiKeyVerifier {

    static final Duration KEY_TTL = Duration.ofMinutes(15);

    private final AdminRepository adminRepository;
    private final ConcurrentHashMap<String, Instant> apiKeys = new ConcurrentHashMap<>();

    public AdminAuthService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хеширования", e);
        }
    }

    /**
     * Validates credentials and, if correct, issues a fresh API key.
     * Empty Mono signals invalid credentials.
     */
    public Mono<String> issueKey(String login, String pass) {
        String expected = hashPassword(pass);
        return adminRepository.findByLogin(login)
                .filter(admin -> expected.equals(admin.getPassSHA()))
                .map(admin -> {
                    purgeExpired();
                    String key = UUID.randomUUID().toString();
                    apiKeys.put(key, Instant.now());
                    return key;
                });
    }

    @Override
    public boolean verify(String apiKey) {
        if (apiKey == null) return false;
        Instant created = apiKeys.get(apiKey);
        if (created == null) return false;
        if (Duration.between(created, Instant.now()).compareTo(KEY_TTL) > 0) {
            apiKeys.remove(apiKey);
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        Instant cutoff = Instant.now().minus(KEY_TTL);
        for (Map.Entry<String, Instant> e : apiKeys.entrySet()) {
            if (e.getValue().isBefore(cutoff)) {
                apiKeys.remove(e.getKey(), e.getValue());
            }
        }
    }
}

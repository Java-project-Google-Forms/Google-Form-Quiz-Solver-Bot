package ru.spbstu.adminauth;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class AdminAuthService {
    
    private final AdminRepository adminRepository;
    
    public AdminAuthService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хеширования", e);
        }
    }
    
    public Mono<Boolean> authenticate(String login, String password) {
        return adminRepository.findByLogin(login)
            .map(admin -> admin.getPassSHA().equals(hashPassword(password)))
            .defaultIfEmpty(false);
    }
}
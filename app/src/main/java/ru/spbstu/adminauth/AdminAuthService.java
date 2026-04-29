package ru.spbstu.adminauth;

import org.springframework.stereotype.Service;
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
    
    public boolean authenticate(String username, String password) {
        return adminRepository.findByUsername(username)
            .map(admin -> admin.getPasswordHash().equals(hashPassword(password)))
            .orElse(false);
    }
}

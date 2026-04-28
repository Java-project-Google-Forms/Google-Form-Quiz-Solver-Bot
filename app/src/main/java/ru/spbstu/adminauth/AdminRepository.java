package ru.spbstu.adminauth;

import org.springframework.stereotype.Repository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Repository
public class AdminRepository {
    
    private static final Admin TEST_ADMIN;
    
    static {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest("admin123".getBytes());
            String hashPassword = Base64.getEncoder().encodeToString(hash);
            TEST_ADMIN = new Admin("admin", hashPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Optional<Admin> findByUsername(String username) {
        if ("admin".equals(username)) {
            return Optional.of(TEST_ADMIN);
        }
        return Optional.empty();
    }
}
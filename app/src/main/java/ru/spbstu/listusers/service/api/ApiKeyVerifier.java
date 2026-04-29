package ru.spbstu.listusers.service.api;

/**
 * Internal contract used by ListUsersModule to ask AdminAuthModule
 * whether a given API key is valid (exists and not expired).
 * <p>
 * Per modulith convention: interface declared in the consumer module
 * ({@code listusers}), implemented in the producer module ({@code adminauth}).
 * </p>
 */
public interface ApiKeyVerifier {
    boolean verify(String apiKey);
}

package ru.spbstu.llmsolver.config;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class LlmConfig {

    @Value("${gigachat.api-key:}")
    private String apiKey;

    @Value("${gigachat.token-url:https://ngw.devices.sberbank.ru:9443/api/v2}")
    private String tokenUrl;

    @Value("${gigachat.chat-url:https://gigachat.devices.sberbank.ru/api/v1}")
    private String chatUrl;

    @Value("${gigachat.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${gigachat.max-attempts:3}")
    private int maxAttempts;

    @Bean
    public HttpClient insecureHttpClient() {
        try {
            var sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            return HttpClient.create()
                    .secure(spec -> spec.sslContext(sslContext));
        } catch (SSLException e) {
            throw new RuntimeException("Failed to create insecure SSL context", e);
        }
    }

    // getters
    public String getApiKey() { return apiKey; }
    public String getTokenUrl() { return tokenUrl; }
    public String getChatUrl() { return chatUrl; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public int getMaxAttempts() { return maxAttempts; }
}
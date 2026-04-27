package ru.spbstu.llmsolver.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;
import ru.spbstu.llmsolver.config.LlmConfig;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
public class TokenProvider {
    private final WebClient tokenWebClient;
    private final LlmConfig config;

    public TokenProvider(LlmConfig config, HttpClient httpClient) {
        this.config = config;
        this.tokenWebClient = WebClient.builder()
                .baseUrl(config.getTokenUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public Mono<String> getAccessToken() {
        return tokenWebClient.post()
                .uri("/oauth")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + config.getApiKey())
                .header("RqUID", UUID.randomUUID().toString())
                .bodyValue("scope=GIGACHAT_API_PERS")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(10))
                .map(response -> response.get("access_token").asText())
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                .onErrorMap(e -> new RuntimeException("Failed to obtain token: " + e.getMessage(), e));
    }
}
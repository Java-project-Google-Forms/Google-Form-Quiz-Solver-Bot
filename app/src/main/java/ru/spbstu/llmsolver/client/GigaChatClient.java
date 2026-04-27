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
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GigaChatClient implements LanguageModelClient {
    private final WebClient chatWebClient;
    private final TokenProvider tokenProvider;
    private final Duration timeout;
    private final int maxAttempts;

    public GigaChatClient(LlmConfig config, TokenProvider tokenProvider, HttpClient httpClient) {
        this.tokenProvider = tokenProvider;
        this.timeout = Duration.ofSeconds(config.getTimeoutSeconds());
        this.maxAttempts = config.getMaxAttempts();

        this.chatWebClient = WebClient.builder()
                .baseUrl(config.getChatUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public Mono<String> ask(String prompt) {
        return tokenProvider.getAccessToken()
                .flatMap(token -> sendChatRequest(prompt, token))
                .retryWhen(Retry.backoff(maxAttempts, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(10))
                        .filter(throwable -> throwable instanceof java.util.concurrent.TimeoutException
                                || throwable instanceof java.net.ConnectException))
                .onErrorMap(e -> new RuntimeException("LLM request failed after " + maxAttempts + " attempts", e));
    }

    private Mono<String> sendChatRequest(String prompt, String token) {
        Map<String, Object> requestBody = Map.of(
                "model", "GigaChat",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 5000
        );

        return chatWebClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(timeout)
                .map(response -> response.at("/choices/0/message/content").asText());
    }
}
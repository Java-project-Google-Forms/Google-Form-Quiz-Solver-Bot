package ru.spbstu.llmsolver.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
@Profile("!stub")
public class GigaChatClient {
    private final WebClient webClient;

    public GigaChatClient(@Value("${gigachat.api-url}") String apiUrl,
                          @Value("${gigachat.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public Mono<String> generateAnswer(String prompt) {
        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new GigaChatRequest(prompt))
                .retrieve()
                .bodyToMono(GigaChatResponse.class)
                .map(resp -> resp.choices().get(0).message().content());
    }

    record GigaChatRequest(String prompt) {}
    record GigaChatResponse(List<Choice> choices) {}
    record Choice(Message message) {}
    record Message(String content) {}
}
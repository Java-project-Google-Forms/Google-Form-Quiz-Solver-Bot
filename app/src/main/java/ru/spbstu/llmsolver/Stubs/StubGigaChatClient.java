package ru.spbstu.llmsolver.stub;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.spbstu.llmsolver.client.GigaChatClient;

@Component
@Primary
@Profile("stub")
public class StubGigaChatClient extends GigaChatClient {
    public StubGigaChatClient() {
        super("https://fake", "fake");
    }
    @Override
    public Mono<String> generateAnswer(String prompt) {
        String shortPrompt = prompt.length() > 50 ? prompt.substring(0, 50) + "..." : prompt;
        return Mono.just("[STUB] Ответ LLM на: " + shortPrompt);
    }
}
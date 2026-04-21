package ru.spbstu.llmsolver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.spbstu.llmsolver.client.GigaChatClient;
import java.util.function.Consumer;

@Service
public class LLMService {
    private static final Logger log = LoggerFactory.getLogger(LLMService.class);
    private final GigaChatClient gigaChatClient;

    public LLMService(GigaChatClient gigaChatClient) {
        this.gigaChatClient = gigaChatClient;
    }

    public void generateAnswerAsync(String prompt, Consumer<String> onSuccess, Consumer<Throwable> onError) {
        log.info("LLM request: {}", prompt);
        gigaChatClient.generateAnswer(prompt)
                .subscribe(
                        onSuccess::accept,
                        error -> {
                            log.error("LLM error", error);
                            onError.accept(error);
                        }
                );
    }
}
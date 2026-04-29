package ru.spbstu.llmsolver.client;

import reactor.core.publisher.Mono;

import java.util.function.LongConsumer;

public interface LanguageModelClient {
    Mono<String> ask(String prompt);

    /**
     * @param onRetry invoked before each retry attempt with the 1-based attempt number
     *                (i.e. {@code 1} means the first retry after the initial failure).
     */
    default Mono<String> ask(String prompt, LongConsumer onRetry) {
        return ask(prompt);
    }
}

package ru.spbstu.llmsolver.client;

import reactor.core.publisher.Mono;

public interface LanguageModelClient {
    Mono<String> ask(String prompt);
}
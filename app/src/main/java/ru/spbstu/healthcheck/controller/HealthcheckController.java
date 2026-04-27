package ru.spbstu.healthcheck.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.spbstu.healthcheck.dto.AuthorsInfo;
import ru.spbstu.healthcheck.dto.HealthStatus;

import java.io.InputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthcheckController {
    private final ObjectMapper objectMapper;

    @GetMapping("/healthcheck")
    public Mono<HealthStatus> healthcheck() {
        HealthStatus status = new HealthStatus();
        status.setService("Google Form Quiz Solver Bot");
        status.setVersion("1.0.0");
        status.setBuildInfo("Built with Java 25 + Spring 7 (no Spring Boot)");

        // Загружаем информацию об авторах из authors.json
        try {
            ClassPathResource resource = new ClassPathResource("authors.json");
            try (InputStream inputStream = resource.getInputStream()) {
                AuthorsInfo authors = objectMapper.readValue(inputStream, AuthorsInfo.class);
                status.setAuthors(authors);
            }
        } catch (Exception e) {
            log.warn("Could not load authors.json: {}", e.getMessage());
        }

        return Mono.just(status);
    }
}
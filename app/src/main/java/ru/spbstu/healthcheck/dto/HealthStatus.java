package ru.spbstu.healthcheck.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthStatus {
    private String status = "UP";
    private Instant timestamp = Instant.now();
    private String service;
    private String version;
    private AuthorsInfo authors;
    private String buildInfo;
}
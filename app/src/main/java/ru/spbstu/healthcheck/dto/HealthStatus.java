package ru.spbstu.healthcheck.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthStatus {
    private String status;
    private Instant timestamp;
    private String service;
    private String version;
    private AuthorsInfo authors;
    private String buildInfo;

    public HealthStatus() {
        this.timestamp = Instant.now();
        this.status = "UP";
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public AuthorsInfo getAuthors() {
        return authors;
    }

    public void setAuthors(AuthorsInfo authors) {
        this.authors = authors;
    }

    public String getBuildInfo() {
        return buildInfo;
    }

    public void setBuildInfo(String buildInfo) {
        this.buildInfo = buildInfo;
    }
}
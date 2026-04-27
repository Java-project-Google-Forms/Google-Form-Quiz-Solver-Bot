package ru.spbstu.healthcheck.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "ru.spbstu.healthcheck")
public class HealthcheckConfig {
    // Конфигурация для модуля healthcheck
}
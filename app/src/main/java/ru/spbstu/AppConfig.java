package ru.spbstu;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@ComponentScan("ru.spbstu")
@PropertySource("classpath:application.properties")
@EnableWebFlux
public class AppConfig {
}

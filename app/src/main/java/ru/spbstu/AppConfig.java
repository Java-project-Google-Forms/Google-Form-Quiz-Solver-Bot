package ru.spbstu;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("ru.spbstu")
@PropertySource("classpath:application.properties")
public class AppConfig {
}

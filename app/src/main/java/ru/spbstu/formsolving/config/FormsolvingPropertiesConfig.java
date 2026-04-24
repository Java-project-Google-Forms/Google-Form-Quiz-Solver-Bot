package ru.spbstu.formsolving.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.spbstu.formsolving.FormSolvingProperties;

@Configuration
@PropertySource("classpath:application.properties")
public class FormsolvingPropertiesConfig {

    @Bean
    public FormSolvingProperties formSolvingProperties(
            @Value("${kafka.topic.form-tasks:form-solving-requests}") String kafkaTopic,
            @Value("${kafka.bootstrap-servers:localhost:9092}") String kafkaBootstrapServers) {
        return new FormSolvingProperties(kafkaTopic, kafkaBootstrapServers);
    }
}
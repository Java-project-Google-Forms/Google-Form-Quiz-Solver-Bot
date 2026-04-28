package ru.spbstu.formsolving.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.spbstu.formsolving.FormSolvingProperties;


/**
 * Configuration that reads properties from {@code application.properties}
 * and creates a {@link FormSolvingProperties} bean.
 */
@Configuration
@PropertySource("classpath:application.properties")
public class FormsolvingPropertiesConfig {

    /**
     * Creates a FormSolvingProperties bean by injecting values from properties.
     *
     * @param kafkaTopic            value of {@code kafka.topic.form-tasks} (default "form-solving-requests")
     * @param kafkaBootstrapServers value of {@code kafka.bootstrap-servers} (default "localhost:9092")
     * @return FormSolvingProperties instance
     */
    @Bean
    public FormSolvingProperties formSolvingProperties(
            @Value("${kafka.topic.form-tasks:form-solving-requests}") String kafkaTopic,
            @Value("${kafka.bootstrap-servers:localhost:9092}") String kafkaBootstrapServers) {
        return new FormSolvingProperties(kafkaTopic, kafkaBootstrapServers);
    }
}
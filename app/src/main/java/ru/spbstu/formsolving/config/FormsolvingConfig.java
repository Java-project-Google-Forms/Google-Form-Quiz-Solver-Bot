package ru.spbstu.formsolving.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


/**
 * Spring configuration for the formsolving module.
 * <p>Scans components in the base package and provides an {@link ObjectMapper} bean
 * configured for JSON processing with JavaTimeModule and parameter names support.</p>
 */
@Configuration
@ComponentScan(basePackages = "ru.spbstu.formsolving")
@PropertySource("classpath:application.properties")
public class FormsolvingConfig {

    /**
     * Creates and configures an ObjectMapper.
     * <ul>
     *   <li>Registers {@code JavaTimeModule} for ISO-8601 date/time serialization</li>
     *   <li>Registers {@code ParameterNamesModule} to support Java 8+ record/constructor parameter names</li>
     *   <li>Disables writing dates as timestamps</li>
     *   <li>Enables pretty-printing for human‑readable JSON</li>
     * </ul>
     *
     * @return configured ObjectMapper bean
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new ParameterNamesModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

}
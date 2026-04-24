package ru.spbstu.formsolving;

public final class FormSolvingProperties {
    private final String kafkaTopic;
    private final String kafkaBootstrapServers;

    public FormSolvingProperties(String kafkaTopic, String kafkaBootstrapServers) {

        this.kafkaTopic = kafkaTopic;
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    public String kafkaTopic() { return kafkaTopic; }
    public String kafkaBootstrapServers() { return kafkaBootstrapServers; }
}
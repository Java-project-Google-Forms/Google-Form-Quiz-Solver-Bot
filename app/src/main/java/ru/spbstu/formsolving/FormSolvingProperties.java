package ru.spbstu.formsolving;

/**
 * Configuration properties for the formsolving module.
 *
 * @param kafkaTopic            the name of the Kafka topic where tasks are published
 * @param kafkaBootstrapServers comma-separated list of Kafka broker addresses (e.g., "localhost:9092")
 */
public record FormSolvingProperties(String kafkaTopic, String kafkaBootstrapServers) { }
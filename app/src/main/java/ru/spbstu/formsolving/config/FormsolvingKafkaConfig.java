package ru.spbstu.formsolving.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import ru.spbstu.formsolving.FormSolvingProperties;

import java.util.HashMap;
import java.util.Map;


/**
 * Kafka configuration for the formsolving module.
 * Provides a reactive Kafka producer, a consumer factory, and a listener container factory.
 * Also ensures the required topic exists by declaring a {@link NewTopic} bean.
 */
@EnableKafka
@Configuration
public class FormsolvingKafkaConfig {

    /**
     * Creates a reactive Kafka sender for string‑valued messages.
     * Uses acks=all, retries=3, and idempotence enabled.
     *
     * @param properties module configuration containing bootstrap servers
     * @return configured KafkaSender
     */
    @Bean
    public KafkaSender<String, String> kafkaSender(FormSolvingProperties properties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.kafkaBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        SenderOptions<String, String> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }

    /**
     * Creates a consumer factory for the LLM solver group.
     * Sets auto offset reset to "earliest".
     *
     * @param properties module configuration
     * @return ConsumerFactory for strings
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory(FormSolvingProperties properties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.kafkaBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "llm-solver-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a concurrent Kafka listener container factory.
     *
     * @param consumerFactory the consumer factory
     * @return listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        return factory;
    }

    /**
     * Declares the Kafka topic used for form solving requests.
     * The topic will be created automatically if it does not exist (single partition, replication factor 1).
     *
     * @param properties module configuration
     * @return NewTopic bean
     */
    @Bean
    public NewTopic formSolvingRequestsTopic(FormSolvingProperties properties) {
        return new NewTopic(properties.kafkaTopic(), 1, (short) 1);
    }
}
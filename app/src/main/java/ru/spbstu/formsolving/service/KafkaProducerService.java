package ru.spbstu.formsolving.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import ru.spbstu.formsolving.entity.FormSolvingException;
import ru.spbstu.formsolving.FormSolvingProperties;

import java.util.Map;

@Component
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;
    private final String requestTopic;

    public KafkaProducerService(KafkaSender<String, String> kafkaSender,
                                FormSolvingProperties properties,
                                ObjectMapper objectMapper) {
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
        this.requestTopic = properties.kafkaTopic();
    }

    public void sendSolveTask(String requestId) {
        try {
            Map<String, String> task = Map.of(
                    "requestId", requestId,
                    "type", "SOLVE"
            );
            String json = objectMapper.writeValueAsString(task);
            ProducerRecord<String, String> record = new ProducerRecord<>(requestTopic, requestId, json);
            kafkaSender.send(Mono.just(SenderRecord.create(record, requestId)))
                    .doOnError(e -> log.error("Failed to send solve task for requestId={}", requestId, e))
                    .subscribe(); // неблокирующе
        } catch (Exception e) {
            throw new FormSolvingException("Failed to serialize solve task", e);
        }
    }

    public void sendRescoreTask(String requestId) {
        try {
            Map<String, Object> task = Map.of(
                    "requestId", requestId,
                    "type", "RESCORE"
            );
            String json = objectMapper.writeValueAsString(task);
            ProducerRecord<String, String> record = new ProducerRecord<>(requestTopic, requestId, json);
            kafkaSender.send(Mono.just(SenderRecord.create(record, requestId)))
                    .doOnError(e -> log.error("Failed to send rescore task for requestId={}", requestId, e))
                    .subscribe();
        } catch (Exception e) {
            throw new FormSolvingException("Failed to serialize rescore task", e);
        }
    }
}
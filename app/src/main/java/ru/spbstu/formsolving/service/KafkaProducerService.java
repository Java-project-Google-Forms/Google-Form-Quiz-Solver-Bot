package ru.spbstu.formsolving.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import ru.spbstu.formsolving.model.FormSolvingException;
import ru.spbstu.formsolving.FormSolvingProperties;

import java.time.Duration;
import java.util.Map;

/**
 * Service that sends form solving tasks to Kafka with synchronous blocking (timeout 5 seconds).
 * <p>If the send operation fails (e.g., Kafka is down), a {@link FormSolvingException} is thrown.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaSender<String, String> kafkaSender;
    private final FormSolvingProperties properties;
    private final ObjectMapper objectMapper;


    private String getRequestTopic() { return properties.kafkaTopic(); }


    /**
     * Sends a SOLVE task for the given request ID.
     * The message contains {"requestId": "<uuid>", "type": "SOLVE"}.
     *
     * @param requestId unique identifier of the solving request (UUID string)
     * @throws FormSolvingException if serialization fails or Kafka send does not complete within timeout
     */
    public void sendSolveTask(String requestId) {
        try {
            Map<String, String> task = Map.of("requestId", requestId, "type", "SOLVE");
            String json = objectMapper.writeValueAsString(task);
            ProducerRecord<String, String> record = new ProducerRecord<>(getRequestTopic(), requestId, json);

            // Блокируемся на 5 секунд
            kafkaSender.send(Mono.just(SenderRecord.create(record, requestId)))
                    .next()
                    .block(Duration.ofSeconds(5));
            log.info("Solve task sent for requestId={}", requestId);
        } catch (Exception e) {
            log.error("Failed to send solve task for requestId={}", requestId, e);
            throw new FormSolvingException("Kafka unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a RESCORE task for the given request ID.
     * The message contains {"requestId": "<uuid>", "type": "RESCORE"}.
     *
     * @param requestId unique identifier of the rescore request (UUID string)
     * @throws FormSolvingException if serialization fails or Kafka send does not complete within timeout
     */
    public void sendRescoreTask(String requestId) {
        try {
            Map<String, Object> task = Map.of(
                    "requestId", requestId,
                    "type", "RESCORE"
            );
            String json = objectMapper.writeValueAsString(task);
            ProducerRecord<String, String> record = new ProducerRecord<>(getRequestTopic(),
                    requestId, json);
            kafkaSender.send(Mono.just(SenderRecord.create(record, requestId)))
                    .next()
                    .block(Duration.ofSeconds(5));
            log.info("Rescore task sent for requestId={}", requestId);
        } catch (Exception e) {
            throw new FormSolvingException("Failed to serialize rescore task", e);
        }
    }
}
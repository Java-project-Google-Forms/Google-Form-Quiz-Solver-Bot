package ru.spbstu.formsolving.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import ru.spbstu.formsolving.entity.FormSolvingException;
import ru.spbstu.formsolving.FormSolvingProperties;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaSender<String, String> kafkaSender;
    private final FormSolvingProperties properties;
    private final ObjectMapper objectMapper;


    private String getRequestTopic() { return properties.kafkaTopic(); }


    public void sendSolveTask(String requestId) {
        try {
            Map<String, String> task = Map.of(
                    "requestId", requestId,
                    "type", "SOLVE"
            );
            String json = objectMapper.writeValueAsString(task);
            ProducerRecord<String, String> record = new ProducerRecord<>(getRequestTopic(),
                    requestId, json);
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
            ProducerRecord<String, String> record = new ProducerRecord<>(getRequestTopic(),
                    requestId, json);
            kafkaSender.send(Mono.just(SenderRecord.create(record, requestId)))
                    .doOnError(e -> log.error("Failed to send rescore task for requestId={}", requestId, e))
                    .subscribe();
        } catch (Exception e) {
            throw new FormSolvingException("Failed to serialize rescore task", e);
        }
    }
}
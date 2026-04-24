package ru.spbstu.llmsolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.spbstu.formsolving.entity.FormStructure;
import ru.spbstu.formsolving.entity.Question;
import ru.spbstu.formsolving.service.FormSolvingProvider;

import ru.spbstu.formsolving.entity.SolvingResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class StubLlmSolver {

    private static final Logger log = LoggerFactory.getLogger(StubLlmSolver.class);
    private final FormSolvingProvider formSolvingProvider;
    private final ObjectMapper objectMapper;

    public StubLlmSolver(FormSolvingProvider formSolvingProvider, ObjectMapper objectMapper) {
        this.formSolvingProvider = formSolvingProvider;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "form-solving-requests", groupId = "llm-solver-group")
    public void handleRequest(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String requestId = json.get("requestId").asText();
            String type = json.get("type").asText();

            if ("SOLVE".equals(type)) {
                Optional<FormStructure> optStructure = formSolvingProvider.getFormStructure(requestId);
                if (optStructure.isEmpty()) {
                    log.error("No form structure found for requestId={}", requestId);
                    return;
                }
                Map<String, String> answers = getStringStringMap(optStructure);
                // Fake delay
                Thread.sleep(3000);

                formSolvingProvider.submitResult(requestId, new SolvingResult(answers));
                log.info("Stub result submitted for requestId={}", requestId);
            } else if ("RESCORE".equals(type)) {
                log.info("Rescore task received (stub), requestId={}", requestId);
                // При необходимости добавить логику
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
        }
    }

    private static Map<String, String> getStringStringMap(Optional<FormStructure> optStructure) {
        FormStructure structure = optStructure.get();

        // Формируем фиктивные ответы (первый вариант или текст-заглушка)
        Map<String, String> answers = new HashMap<>();
        for (Question q : structure.getQuestions()) {
            String answer;
            if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                answer = q.getOptions().getFirst();
            } else {
                answer = "Заглушка (текстовый ответ)";
            }
            answers.put(q.getId(), answer);
        }
        return answers;
    }
}
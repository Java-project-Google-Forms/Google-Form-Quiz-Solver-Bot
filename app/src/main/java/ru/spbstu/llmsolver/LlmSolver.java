package ru.spbstu.llmsolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.spbstu.formsolving.model.FormStructure;
import ru.spbstu.formsolving.model.Question;
import ru.spbstu.formsolving.model.SolvingResult;
import ru.spbstu.formsolving.service.FormSolvingProvider;
import ru.spbstu.llmsolver.client.GigaChatClient;
import ru.spbstu.llmsolver.service.LLMQuestionSolver;
import ru.spbstu.llmsolver.service.LLMQuestionSolver.AnswerWithConfidence;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmSolver {

    private final FormSolvingProvider formSolvingProvider;
    private final LLMQuestionSolver llmQuestionSolver;
    private final GigaChatClient gigaChatClient;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "form-solving-requests", groupId = "llm-solver-group")
    public void handleRequest(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String requestId = json.get("requestId").asText();
            String type = json.get("type").asText();

            if ("SOLVE".equals(type)) {
                processSolve(requestId);
            } else if ("RESCORE".equals(type)) {
                processRescore(requestId);
            } else {
                log.warn("Unknown request type: {}", type);
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
        }
    }

    private void processSolve(String requestId) {
        Optional<FormStructure> optStructure = formSolvingProvider.getFormStructure(requestId);
        if (optStructure.isEmpty()) {
            log.error("No form structure found for requestId={}", requestId);
            return;
        }
        FormStructure structure = optStructure.get();
        List<LLMQuestionSolver.Question> llmQuestions = convertToLlmQuestions(structure.getQuestions());

        int maxAttempts = gigaChatClient.getMaxAttempts();
        LongConsumer onRetry = attempt ->
                formSolvingProvider.notifyProgress(requestId,
                        String.format("⏳ Попытка %d не удалась, пробуем ещё раз (всего попыток: %d)…",
                                attempt, maxAttempts));

        try {
            Map<String, AnswerWithConfidence> answersWithConfidence =
                    llmQuestionSolver.solveQuestions(llmQuestions, onRetry)
                            .block(Duration.ofMinutes(2));

            if (answersWithConfidence == null) {
                throw new RuntimeException("LLM solver returned null");
            }

            Map<String, String> answersWithConfidenceText = answersWithConfidence.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> String.format("%s (уверенность: %d%%)", e.getValue().answer(), e.getValue().confidence()),
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));

            log.info("Solved requestId={}, answers count={}", requestId, answersWithConfidenceText.size());
            formSolvingProvider.submitResult(requestId, new SolvingResult(answersWithConfidenceText));
        } catch (Exception e) {
            log.error("LLM solving failed for requestId={}", requestId, e);
            formSolvingProvider.notifyProgress(requestId,
                    "❌ Не удалось получить ответ от LLM: " + e.getMessage()
                            + "\nПопробуйте отправить форму ещё раз позже.");
            Map<String, String> errorAnswers = buildErrorAnswers(structure, e.getMessage());
            formSolvingProvider.submitResult(requestId, new SolvingResult(errorAnswers));
        }
    }

    private Map<String, String> buildErrorAnswers(FormStructure structure, String errorMsg) {
        Map<String, String> errors = new LinkedHashMap<>();
        int idx = 0;
        for (Question q : structure.getQuestions()) {
            String key = q.getId() != null ? q.getId() : "q" + idx;
            errors.put(key, "❌ Ошибка LLM: " + errorMsg);
            idx++;
        }
        return errors;
    }

    private void processRescore(String requestId) {
        log.info("Processing rescore requestId={}", requestId);
        processSolve(requestId);
    }

    private List<LLMQuestionSolver.Question> convertToLlmQuestions(List<Question> formQuestions) {
        return formQuestions.stream()
                .map(q -> new LLMQuestionSolver.Question(
                        q.getId(),
                        q.getTitle(),
                        q.getType().name(),
                        q.getOptions() != null ? q.getOptions() : List.of()
                ))
                .collect(Collectors.toList());
    }
}

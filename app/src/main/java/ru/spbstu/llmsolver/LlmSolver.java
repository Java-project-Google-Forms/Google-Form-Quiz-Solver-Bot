package ru.spbstu.llmsolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.spbstu.formsolving.entity.FormStructure;
import ru.spbstu.formsolving.entity.Question;
import ru.spbstu.formsolving.entity.SolvingResult;
import ru.spbstu.formsolving.service.FormSolvingProvider;
import ru.spbstu.llmsolver.service.LLMQuestionSolver;
import ru.spbstu.llmsolver.service.LLMQuestionSolver.AnswerWithConfidence;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmSolver {

    private final FormSolvingProvider formSolvingProvider;
    private final LLMQuestionSolver llmQuestionSolver;
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

        try {
            Map<String, AnswerWithConfidence> answersWithConfidence = llmQuestionSolver.solveQuestions(llmQuestions)
                    .block(Duration.ofMinutes(2));

            if (answersWithConfidence == null) {
                throw new RuntimeException("LLM solver returned null");
            }

            // Добавляем уверенность прямо в текст ответа
            Map<String, String> answersWithConfidenceText = answersWithConfidence.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> String.format("%s (уверенность: %d%%)", e.getValue().answer(), e.getValue().confidence())
                    ));

            log.info("Solved requestId={}, answers count={}", requestId, answersWithConfidenceText.size());
            formSolvingProvider.submitResult(requestId, new SolvingResult(answersWithConfidenceText));
        } catch (Exception e) {
            log.error("LLM solving failed for requestId={}", requestId, e);
            Map<String, String> errorAnswers = structure.getQuestions().stream()
                    .collect(Collectors.toMap(
                            Question::getId,
                            _ -> "❌ Ошибка LLM: " + e.getMessage()
                    ));
            formSolvingProvider.submitResult(requestId, new SolvingResult(errorAnswers));
        }
    }

    private void processRescore(String requestId) {
        // Rescore uses the same logic – just solve again.
        // If needed, you could add special handling (e.g., different prompt).
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
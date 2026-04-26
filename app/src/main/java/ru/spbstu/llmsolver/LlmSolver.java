package ru.spbstu.llmsolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
public class LlmSolver {

    private static final Logger log = LoggerFactory.getLogger(LlmSolver.class);
    private final FormSolvingProvider formSolvingProvider;
    private final LLMQuestionSolver llmQuestionSolver;
    private final ObjectMapper objectMapper;

    public LlmSolver(FormSolvingProvider formSolvingProvider,
                     LLMQuestionSolver llmQuestionSolver,
                     ObjectMapper objectMapper) {
        this.formSolvingProvider = formSolvingProvider;
        this.llmQuestionSolver = llmQuestionSolver;
        this.objectMapper = objectMapper;
    }

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
                    .block(Duration.ofMinutes(2)); // block because Kafka listener is synchronous

            if (answersWithConfidence == null) {
                throw new RuntimeException("LLM solver returned null");
            }

            Map<String, String> simpleAnswers = answersWithConfidence.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().answer()
                    ));

            log.info("Solved requestId={}, answers count={}", requestId, simpleAnswers.size());
            formSolvingProvider.submitResult(requestId, new SolvingResult(simpleAnswers));
        } catch (Exception e) {
            log.error("LLM solving failed for requestId={}", requestId, e);
            // Submit fallback answers to avoid hanging the requester
            Map<String, String> errorAnswers = structure.getQuestions().stream()
                    .collect(Collectors.toMap(
                            Question::getId,
                            q -> "❌ LLM solving error: " + e.getMessage()
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
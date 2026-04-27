package ru.spbstu.llmsolver.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.spbstu.llmsolver.service.LLMQuestionSolver.AnswerWithConfidence;
import ru.spbstu.llmsolver.service.LLMQuestionSolver.Question;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnswerParser {

    private final ObjectMapper objectMapper;

    public Map<String, AnswerWithConfidence> parseAnswers(String llmText, List<Question> questions) {
        String jsonContent = extractJson(llmText);
        if (jsonContent != null) {
            try {
                return parseJson(jsonContent, questions);
            } catch (Exception e) {
                log.warn("Failed to parse JSON, falling back to regex", e);
            }
        }
        return parseRegexAnswers(llmText, questions);
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private Map<String, AnswerWithConfidence> parseJson(String json, List<Question> questions) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        Map<String, AnswerWithConfidence> result = new LinkedHashMap<>();
        for (int i = 0; i < questions.size(); i++) {
            String key = String.valueOf(i + 1);
            JsonNode node = root.get(key);
            if (node != null && node.has("answer")) {
                String answer = node.get("answer").asText();
                int confidence = 50;
                if (node.has("confidence")) {
                    JsonNode confNode = node.get("confidence");
                    if (confNode.isInt()) {
                        confidence = confNode.asInt();
                    } else if (confNode.isTextual()) {
                        String confStr = confNode.asText().replace("%", "").trim();
                        try {
                            confidence = Integer.parseInt(confStr);
                        } catch (NumberFormatException e) {
                            confidence = 0;
                        }
                    }
                }
                // Replace markers
                if ("__PERSONAL__".equals(answer)) {
                    answer = " В этом вопросе запрашиваются личные данные. Пожалуйста, ответьте вручную.";
                    confidence = 0;
                } else if ("UNKNOWN".equals(answer)) {
                    answer = " Информация недоступна или требует актуальных данных.";
                    confidence = 0;
                }
                result.put(questions.get(i).id(), new AnswerWithConfidence(answer, confidence));
            } else {
                result.put(questions.get(i).id(), new AnswerWithConfidence(" Ответ не предоставлен.", 0));
            }
        }
        return result;
    }

    private Map<String, AnswerWithConfidence> parseRegexAnswers(String text, List<Question> questions) {
        Map<String, AnswerWithConfidence> result = new LinkedHashMap<>();
        if (text.contains("__PERSONAL__") || text.contains("__SUBJECTIVE__")) {
            for (Question q : questions) {
                result.put(q.id(), new AnswerWithConfidence(
                    " Личные данные – ответьте вручную.", 0));
            }
            return result;
        }
        String[] blocks = text.split("Question \\d+:");
        if (blocks.length < 2) {
            for (Question q : questions) {
                result.put(q.id(), new AnswerWithConfidence(
                    text.length() > 100 ? text.substring(0, 100) : text, 50));
            }
            return result;
        }
        for (int i = 0; i < questions.size() && i + 1 < blocks.length; i++) {
            String block = blocks[i + 1];
            String answer = extractByRegex(block, "Answer:\\s*(.+?)(?=\\nConfidence|$)");
            Integer confidence = extractIntByRegex(block, "Confidence:\\s*(\\d+)%");
            result.put(questions.get(i).id(), new AnswerWithConfidence(
                    answer != null ? answer : "[Не распознано]",
                    confidence != null ? confidence : 0
            ));
        }
        return result;
    }

    private String extractByRegex(String text, String pattern) {
        var matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private Integer extractIntByRegex(String text, String pattern) {
        var matcher = Pattern.compile(pattern).matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }
}
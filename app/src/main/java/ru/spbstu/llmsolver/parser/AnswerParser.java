package ru.spbstu.llmsolver.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.spbstu.llmsolver.service.LLMQuestionSolver.AnswerWithConfidence;
import ru.spbstu.llmsolver.service.LLMQuestionSolver.Question;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Преобразует «сырой» текст ответа LLM в мапу
 * {@code questionId → AnswerWithConfidence}.
 *
 * <p>GigaChat обычно возвращает JSON по формату из {@code PromptBuilder},
 * но в реальности часто оборачивает его в {@code ```json … ```} или
 * добавляет вступительный/заключительный текст. Парсер делает три попытки
 * вытащить полезную информацию:
 * <ol>
 *   <li>{@link #extractJson} — ищет код-фенс {@code ```json … ```}, если нет —
 *       выполняет балансный скан скобок и возвращает первый сбалансированный
 *       JSON-объект.</li>
 *   <li>{@link #parseJson} — парсит как JSON и сопоставляет ключи
 *       {@code "1","2",…} с вопросами по порядку. Поддерживает маркеры
 *       {@code __PERSONAL__} и {@code UNKNOWN} (заменяются на дружелюбный
 *       текст с {@code confidence=0}).</li>
 *   <li>{@link #parseRegexAnswers} — fallback, если JSON не получился: на
 *       каждый вопрос ставит «❌ Не удалось распарсить ответ LLM»
 *       ({@code confidence=0}). Никаких глобальных бранчей вроде «всё —
 *       личные данные» здесь нет: маркеры обрабатываются только в
 *       {@code parseJson}.</li>
 * </ol>
 *
 * <p>Ключи мапы — {@code q.getId()} (тот же, по которому будет лукап в
 * {@code formsolving.submitResult}). Если в форме у вопросов id {@code null}
 * или повторяются — это проблема парсера форм, не наша.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnswerParser {

    private static final String PARSE_FAIL = "❌ Не удалось распарсить ответ LLM";

    /** ```json ... ``` or ``` ... ``` */
    private static final Pattern CODE_FENCE = Pattern.compile(
            "```(?:json)?\\s*(\\{.*?})\\s*```", Pattern.DOTALL);

    private final ObjectMapper objectMapper;

    /**
     * Главный метод. Возвращает мапу с ответом и уверенностью для каждого
     * из переданных вопросов. Никогда не бросает исключения и никогда не
     * возвращает {@code null}: при любой ошибке парсинга в мапе будет запись
     * с текстом-плейсхолдером и {@code confidence=0}.
     */
    public Map<String, AnswerWithConfidence> parseAnswers(String llmText, List<Question> questions) {
        String jsonContent = extractJson(llmText);
        if (jsonContent != null) {
            try {
                return parseJson(jsonContent, questions);
            } catch (Exception e) {
                log.warn("Failed to parse JSON ({}): first 200 chars: {}",
                        e.getMessage(), preview(llmText));
            }
        } else {
            log.warn("No JSON object found in LLM response. First 200 chars: {}", preview(llmText));
        }
        return parseRegexAnswers(llmText, questions);
    }

    private static String preview(String s) {
        if (s == null) return "<null>";
        String oneLine = s.replaceAll("\\s+", " ");
        return oneLine.length() > 200 ? oneLine.substring(0, 200) + "..." : oneLine;
    }

    /**
     * Pulls a JSON object out of LLM text.
     * Strategy: try ```code fence``` first, then balanced-braces scan.
     */
    private String extractJson(String text) {
        if (text == null || text.isEmpty()) return null;
        Matcher m = CODE_FENCE.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        // Balanced-braces scan: find first '{' that has a matching '}'.
        int start = -1;
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escape) { escape = false; continue; }
            if (c == '\\') { escape = true; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (inString) continue;
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private Map<String, AnswerWithConfidence> parseJson(String json, List<Question> questions) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        Map<String, AnswerWithConfidence> result = new LinkedHashMap<>();
        for (int i = 0; i < questions.size(); i++) {
            String key = String.valueOf(i + 1);
            JsonNode node = root.get(key);
            String mapKey = mapKey(questions.get(i), i);
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
                if ("__PERSONAL__".equals(answer)) {
                    answer = "В этом вопросе запрашиваются личные данные. Пожалуйста, ответьте вручную.";
                    confidence = 0;
                } else if ("UNKNOWN".equals(answer)) {
                    answer = "Информация недоступна или требует актуальных данных.";
                    confidence = 0;
                }
                result.put(mapKey, new AnswerWithConfidence(answer, confidence));
            } else {
                result.put(mapKey, new AnswerWithConfidence("Ответ не предоставлен", 0));
            }
        }
        return result;
    }

    private Map<String, AnswerWithConfidence> parseRegexAnswers(String text, List<Question> questions) {
        Map<String, AnswerWithConfidence> result = new LinkedHashMap<>();
        if (text == null) {
            for (int i = 0; i < questions.size(); i++) {
                result.put(mapKey(questions.get(i), i), new AnswerWithConfidence(PARSE_FAIL, 0));
            }
            return result;
        }
        String[] blocks = text.split("Question \\d+:");
        if (blocks.length < 2) {
            for (int i = 0; i < questions.size(); i++) {
                result.put(mapKey(questions.get(i), i), new AnswerWithConfidence(PARSE_FAIL, 0));
            }
            return result;
        }
        for (int i = 0; i < questions.size(); i++) {
            String mapKey = mapKey(questions.get(i), i);
            if (i + 1 >= blocks.length) {
                result.put(mapKey, new AnswerWithConfidence(PARSE_FAIL, 0));
                continue;
            }
            String block = blocks[i + 1];
            String answer = extractByRegex(block, "Answer:\\s*(.+?)(?=\\nConfidence|$)");
            Integer confidence = extractIntByRegex(block, "Confidence:\\s*(\\d+)%");
            result.put(mapKey, new AnswerWithConfidence(
                    answer != null ? answer : PARSE_FAIL,
                    confidence != null ? confidence : 0
            ));
        }
        return result;
    }


    /**
     * Возвращает ключ для мапы ответов. Используется одно и то же значение
     * во всех ветках парсера ({@link #parseJson}, {@link #parseRegexAnswers})
     * — это критично, потому что {@code formsolving.submitResult} ищет ответ
     * именно по {@code q.getId()}, и любой синтетический ключ привёл бы к
     * тому, что пользователь увидит «—» вместо реального ответа.
     * Параметр {@code index} оставлен на случай будущей замены, если
     * {@code q.id()} окажется ненадёжным.
     */
    private static String mapKey(Question q, int index) {
        return q.id();
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

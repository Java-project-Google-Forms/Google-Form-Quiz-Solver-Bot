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

/**
 * Главный consumer задач решения форм. Слушает Kafka-топик
 * {@code form-solving-requests}, по requestId забирает структуру формы у
 * {@link FormSolvingProvider}, прогоняет вопросы через LLM и возвращает
 * результат обратно через {@link FormSolvingProvider#submitResult(String, SolvingResult)}.
 *
 * <p>Контракт результата:
 * <ul>
 *   <li>Если LLM не ответила (после всех ретраев) — {@code submitResult}
 *       НЕ вызывается: форма не сохраняется, пользователь не получает
 *       «✅ Результат решения формы…».</li>
 *   <li>Если есть хоть одна неотвеченная позиция (UNSUPPORTED, личные данные,
 *       парс-фейл) — в title формы дописывается шапка
 *       «⚠️ Форма решена частично…», которую затем выведет
 *       {@code formsolving.submitResult} над списком вопросов.</li>
 * </ul>
 *
 * <p>Существует параллельный {@link StubLlmSolver} с тем же
 * {@code @KafkaListener}, но он включён только под профилем
 * {@code stub-llm}, поэтому в одной consumer-group активен ровно
 * один listener.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmSolver {

    private final FormSolvingProvider formSolvingProvider;
    private final LLMQuestionSolver llmQuestionSolver;
    private final GigaChatClient gigaChatClient;
    private final ObjectMapper objectMapper;

    /**
     * Точка входа Kafka-consumer'а. Разбирает входящий JSON-конверт
     * (поля {@code requestId} и {@code type}: {@code SOLVE}/{@code RESCORE})
     * и делегирует работу соответствующему обработчику. Любая исключительная
     * ситуация логируется, но не пробрасывается — иначе Kafka повторит доставку
     * того же сообщения в бесконечном цикле.
     *
     * @param message JSON-строка из топика {@code form-solving-requests}
     */
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

    /**
     * Обрабатывает задачу типа {@code SOLVE}.
     * <ol>
     *   <li>Достаёт структуру формы по requestId.</li>
     *   <li>Конвертирует вопросы в формат {@link LLMQuestionSolver.Question}
     *       и вызывает решение (с onRetry-callback'ом, который пишет каждую
     *       неудачную попытку в лог).</li>
     *   <li>На полный фейл LLM — НЕ вызывает {@code submitResult},
     *       форма не сохраняется и пользователь финального сообщения не получает.</li>
     *   <li>На частичный успех (есть и решённые, и нерешённые вопросы) —
     *       мутирует {@code title} формы, дописывая шапку со счётчиком
     *       решено/всего.</li>
     *   <li>Формирует мапу {@code questionId → текст ответа} и вызывает
     *       {@link FormSolvingProvider#submitResult}.</li>
     * </ol>
     */
    private void processSolve(String requestId) {
        Optional<FormStructure> optStructure = formSolvingProvider.getFormStructure(requestId);
        if (optStructure.isEmpty()) {
            log.error("No form structure found for requestId={}", requestId);
            return;
        }
        FormStructure structure = optStructure.get();
        List<LLMQuestionSolver.Question> llmQuestions = convertToLlmQuestions(structure.getQuestions());

        int maxAttempts = gigaChatClient.getMaxAttempts();
        // Каждая неудачная попытка LLM пишется в лог; в чат пользователю
        // промежуточные уведомления послать нельзя — нет канала вне
        // FormSolvingProvider.submitResult.
        LongConsumer onRetry = attempt ->
                log.warn("LLM попытка {}/{} не удалась — пробуем ещё раз (requestId={})",
                        attempt, maxAttempts, requestId);

        Map<String, AnswerWithConfidence> answers;
        try {
            answers = llmQuestionSolver.solveQuestions(llmQuestions, onRetry)
                    .block(Duration.ofMinutes(2));
            if (answers == null) {
                throw new RuntimeException("LLM solver returned null");
            }
        } catch (Exception e) {
            // Форму НЕ возвращаем и НЕ сохраняем — submitResult не вызывается специально.
            log.error("😔 LLM не ответила после {} попыток для requestId={}: {}",
                    maxAttempts, requestId, e.getMessage());
            return;
        }

        // Считаем ошибки/успехи (confidence == 0 → не решено).
        long failed = answers.values().stream().filter(a -> a.confidence() == 0).count();
        long total = answers.size();
        long success = total - failed;

        // Шапка о частичном решении ставится в title формы (через мутацию):
        // submitResult в formsolving выводит title сразу после
        // «✅ Результат решения формы», поэтому шапка окажется выше списка
        // вопросов. Другого канала повлиять на верхушку сообщения у нас нет.
        if (failed > 0) {
            String header = String.format(
                    "⚠️ Форма решена частично: %d из %d вопросов получили ответ%s",
                    success, total,
                    success == 0 ? "\nНи один вопрос не удалось решить." : "");
            structure.setTitle(structure.getTitle() + "\n" + header);
        }

        // Готовим финальную мапу для submitResult: ключ — q.getId() (как и
        // ожидает formsolving.submitResult), значение — текст ответа с
        // опциональной пометкой уверенности.
        Map<String, String> answersText = new LinkedHashMap<>();
        for (Question q : structure.getQuestions()) {
            AnswerWithConfidence awc = answers.get(q.getId());
            String body;
            if (awc == null) {
                body = "❌ Ответ не получен";
            } else if (awc.confidence() == 0) {
                body = awc.answer();
            } else {
                body = String.format("%s (уверенность: %d%%)", awc.answer(), awc.confidence());
            }
            answersText.put(q.getId(), body);
        }

        log.info("Solved requestId={} total={} success={} failed={}", requestId, total, success, failed);
        formSolvingProvider.submitResult(requestId, new SolvingResult(answersText));
    }

    /**
     * Обрабатывает задачу типа {@code RESCORE}. Сейчас просто переиспользует
     * {@link #processSolve(String)} — повторное решение генерирует свежие
     * ответы по той же структуре.
     */
    private void processRescore(String requestId) {
        log.info("Processing rescore requestId={}", requestId);
        processSolve(requestId);
    }

    /**
     * Адаптер из доменной модели формы ({@link Question}) в формат, понятный
     * {@link LLMQuestionSolver}. Опции, если {@code null}, заменяются на пустой
     * список — иначе {@code PromptBuilder} не сможет их перечислить.
     */
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

package ru.spbstu.llmsolver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.spbstu.llmsolver.client.LanguageModelClient;
import ru.spbstu.llmsolver.parser.AnswerParser;
import ru.spbstu.llmsolver.prompt.PromptBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongConsumer;

/**
 * Сервисный слой LLM-решателя. Берёт список вопросов формы, отделяет
 * поддерживаемые типы (whitelist {@link #SUPPORTED_TYPES}) от неподдерживаемых,
 * формирует prompt через {@link PromptBuilder}, отправляет его в
 * {@link LanguageModelClient} и парсит ответ через {@link AnswerParser}.
 *
 * <p>Контракт:
 * <ul>
 *   <li>Возвращает мапу {@code questionId → AnswerWithConfidence} в том же
 *       порядке, что и входной список (благодаря {@link LinkedHashMap}).</li>
 *   <li>Неподдерживаемые типы получают ответ
 *       {@code «❌ Тип вопроса не поддерживается»} с {@code confidence=0} и
 *       НЕ отправляются в LLM.</li>
 *   <li>Если все вопросы — неподдерживаемые, LLM не вызывается вообще.</li>
 *   <li>Перегрузка с {@code onRetry} прокидывает callback вглубь до
 *       {@link LanguageModelClient#ask(String, LongConsumer)}, где он дёргается
 *       перед каждой попыткой повторного запроса.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LLMQuestionSolver {

    /** Whitelist of question types we know how to ask the LLM about. */
    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "TEXT", "PARAGRAPH", "PARAGRAPH_TEXT",
            "MULTIPLE_CHOICE", "DROP_DOWN", "DROPDOWN",
            "CHECKBOX", "CHECKBOXES",
            "LINEAR_SCALE", "DATE", "TIME"
    );

    private static final String UNSUPPORTED_ANSWER = "❌ Тип вопроса не поддерживается";

    private final LanguageModelClient llmClient;
    private final PromptBuilder promptBuilder;
    private final AnswerParser answerParser;

    /**
     * Удобная перегрузка без callback'а на ретрай.
     */
    public Mono<Map<String, AnswerWithConfidence>> solveQuestions(List<Question> questions) {
        return solveQuestions(questions, attempt -> {});
    }

    /**
     * Решает указанные вопросы.
     *
     * @param questions список вопросов в исходном порядке (порядок будет
     *                  сохранён в результирующей мапе).
     * @param onRetry   вызывается перед каждой попыткой повторного запроса к LLM
     *                  с 1-индексным номером попытки. Используется для логирования
     *                  и/или уведомления пользователя.
     * @return {@link Mono}, эмитирующий мапу
     *         {@code questionId → AnswerWithConfidence}. Никогда не {@code null}
     *         и никогда не пустая, если входной список не пустой —
     *         для каждого вопроса всегда будет запись (хотя бы с
     *         {@code confidence=0}).
     */
    public Mono<Map<String, AnswerWithConfidence>> solveQuestions(List<Question> questions,
                                                                  LongConsumer onRetry) {
        if (questions == null || questions.isEmpty()) {
            return Mono.just(Map.of());
        }

        List<Question> supported = questions.stream()
                .filter(LLMQuestionSolver::isSupported)
                .toList();

        Map<String, AnswerWithConfidence> unsupported = new LinkedHashMap<>();
        for (Question q : questions) {
            if (!isSupported(q)) {
                log.info("Skipping LLM call for unsupported question id={} type={}", q.id(), q.type());
                unsupported.put(q.id(), new AnswerWithConfidence(UNSUPPORTED_ANSWER, 0));
            }
        }

        if (supported.isEmpty()) {
            log.info("All {} questions are unsupported — LLM not called", questions.size());
            return Mono.just(merge(questions, unsupported, Map.of()));
        }

        log.info("Sending {} of {} questions to LLM ({} unsupported skipped)",
                supported.size(), questions.size(), unsupported.size());

        String prompt = promptBuilder.buildPrompt(supported);
        return llmClient.ask(prompt, onRetry)
                .map(llmAnswer -> merge(questions, unsupported,
                        answerParser.parseAnswers(llmAnswer, supported)));
    }

    /**
     * Поддерживается ли вопрос — определяется по строковому имени типа
     * (нечувствительно к регистру). null-вопросы и null-типы сразу отбрасываются.
     */
    private static boolean isSupported(Question q) {
        return q != null && q.type() != null && SUPPORTED_TYPES.contains(q.type().toUpperCase());
    }

    /**
     * Сливает заглушки для UNSUPPORTED-вопросов с реальными ответами LLM
     * в один результат, сохраняя оригинальный порядок вопросов.
     * Если для какого-то вопроса нет ни заглушки, ни LLM-ответа — ставится
     * {@code «❌ Ответ не получен»} с {@code confidence=0}.
     */
    private static Map<String, AnswerWithConfidence> merge(
            List<Question> originalOrder,
            Map<String, AnswerWithConfidence> unsupported,
            Map<String, AnswerWithConfidence> llmParsed) {
        Map<String, AnswerWithConfidence> merged = new LinkedHashMap<>();
        for (Question q : originalOrder) {
            AnswerWithConfidence a = unsupported.get(q.id());
            if (a != null) {
                merged.put(q.id(), a);
            } else {
                AnswerWithConfidence parsed = llmParsed.get(q.id());
                merged.put(q.id(), parsed != null
                        ? parsed
                        : new AnswerWithConfidence("❌ Ответ не получен", 0));
            }
        }
        return merged;
    }

    /**
     * DTO вопроса для LLM-слоя.
     *
     * @param id      идентификатор вопроса (используется как ключ в
     *                результирующей мапе ответов)
     * @param text    текст вопроса (попадает в prompt)
     * @param type    строковое имя типа (см. {@link #SUPPORTED_TYPES})
     * @param options варианты ответа для choice-типов; пустой список для текстовых
     */
    public record Question(String id, String text, String type, List<String> options) {}

    /**
     * Ответ модели вместе с уверенностью в процентах [0..100].
     * {@code confidence=0} используется как маркер «не решено» (UNSUPPORTED,
     * личные данные, парс-фейл).
     */
    public record AnswerWithConfidence(String answer, int confidence) {}
}

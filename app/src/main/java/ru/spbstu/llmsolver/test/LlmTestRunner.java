package ru.spbstu.llmsolver.test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.spbstu.llmsolver.service.LLMQuestionSolver;

import java.util.List;

/**
 * Standalone-runner для отладки LLM-слоя без поднятия всего приложения.
 * Поднимает только модуль {@code ru.spbstu.llmsolver} (без Kafka, без бота,
 * без Mongo), отправляет в GigaChat подготовленный набор вопросов разных
 * типов и печатает в консоль ответы и сводную статистику.
 *
 * <p>Запуск: gradle-таск {@code :app:runLlmTest} (см. {@code build.gradle.kts}).
 * В CI и продакшне НЕ используется.
 */
@Configuration
@ComponentScan("ru.spbstu.llmsolver")
@PropertySource("classpath:application.properties")
public class LlmTestRunner {

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LlmTestRunner.class)) {
            LLMQuestionSolver solver = context.getBean(LLMQuestionSolver.class);

            // Расширенный набор вопросов для проверки всех граничных случаев
            List<LLMQuestionSolver.Question> questions = List.of(
                // Фактические вопросы
                new LLMQuestionSolver.Question("q1", "What is the capital of Germany?", "TEXT", List.of()),
                new LLMQuestionSolver.Question("q2", "Solve: 12 * 8 = ?", "TEXT", List.of()),
                new LLMQuestionSolver.Question("q3", "Who wrote 'Romeo and Juliet'?", "TEXT", List.of()),
                // Multiple choice (один вариант)
                new LLMQuestionSolver.Question("q4", "Which of these is a programming language?", "MULTIPLE_CHOICE",
                        List.of("HTML", "CSS", "Java", "JSON")),
                // Checkboxes (несколько вариантов)
                new LLMQuestionSolver.Question("q5", "Which of the following are databases?", "CHECKBOXES",
                        List.of("MySQL", "MongoDB", "React", "PostgreSQL")),
                // Линейная шкала (но теперь субъективная – должна стать __SUBJECTIVE__)
                new LLMQuestionSolver.Question("q6", "Rate your interest in AI from 1 to 5 (1 = not interested, 5 = very interested)", "LINEAR_SCALE",
                        List.of("1", "2", "3", "4", "5")),
                // Субъективный вопрос без вариантов
                new LLMQuestionSolver.Question("q7", "What is your favorite color?", "TEXT", List.of()),
                // Общепризнанный "лучший" – должен быть отвечен
                new LLMQuestionSolver.Question("q8", "What is the most popular database for web applications?", "TEXT", List.of()),
                // Личные данные
                new LLMQuestionSolver.Question("q9", "What is your social security number?", "TEXT", List.of()),
                new LLMQuestionSolver.Question("q10", "Enter your passport number", "TEXT", List.of()),
                // Дата и время
                new LLMQuestionSolver.Question("q11", "What is today's date?", "DATE", List.of()),
                new LLMQuestionSolver.Question("q12", "What time is it now?", "TIME", List.of()),
                // Фактический вопрос с датой (не текущей)
                new LLMQuestionSolver.Question("q13", "In which year did World War II end?", "TEXT", List.of()),
                // Развёрнутый текст
                new LLMQuestionSolver.Question("q14", "Explain the water cycle in one sentence.", "PARAGRAPH_TEXT", List.of())
            );

            System.out.println("\n=== Sending request to GigaChat with " + questions.size() + " questions ===");
            long start = System.currentTimeMillis();
            solver.solveQuestions(questions)
                    .doOnSuccess(result -> {
                        long elapsed = System.currentTimeMillis() - start;
                        System.out.println("\n=== LLM Answers (took " + elapsed + " ms) ===\n");
                        int personalCount = 0;
                        int subjectiveCount = 0;
                        int unknownCount = 0;
                        for (var entry : result.entrySet()) {
                            String qId = entry.getKey();
                            var ans = entry.getValue();
                            String questionText = questions.stream()
                                    .filter(q -> q.id().equals(qId))
                                    .map(LLMQuestionSolver.Question::text)
                                    .findFirst()
                                    .orElse("?");
                            System.out.printf("[%s] %s -> %s (confidence: %d%%)%n",
                                    qId, questionText, ans.answer(), ans.confidence());
                            if (ans.answer().contains("personal data")) personalCount++;
                            else if (ans.answer().contains("personal opinion")) subjectiveCount++;
                            else if (ans.answer().contains("not available")) unknownCount++;
                        }
                        System.out.printf("%nSummary: answered directly = %d, personal = %d, subjective = %d, unknown = %d%n",
                                questions.size() - (personalCount + subjectiveCount + unknownCount),
                                personalCount, subjectiveCount, unknownCount);
                    })
                    .block();
        }
    }
}
package ru.spbstu.llmsolver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.spbstu.llmsolver.client.LanguageModelClient;
import ru.spbstu.llmsolver.parser.AnswerParser;
import ru.spbstu.llmsolver.prompt.PromptBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.LongConsumer;

@Service
@RequiredArgsConstructor
public class LLMQuestionSolver {

    private final LanguageModelClient llmClient;
    private final PromptBuilder promptBuilder;
    private final AnswerParser answerParser;

    public Mono<Map<String, AnswerWithConfidence>> solveQuestions(List<Question> questions) {
        return solveQuestions(questions, attempt -> {});
    }

    public Mono<Map<String, AnswerWithConfidence>> solveQuestions(List<Question> questions,
                                                                  LongConsumer onRetry) {
        if (questions == null || questions.isEmpty()) {
            return Mono.just(Map.of());
        }
        String prompt = promptBuilder.buildPrompt(questions);
        return llmClient.ask(prompt, onRetry)
                .map(llmAnswer -> answerParser.parseAnswers(llmAnswer, questions));
    }

    public record Question(String id, String text, String type, List<String> options) {}
    public record AnswerWithConfidence(String answer, int confidence) {}
}

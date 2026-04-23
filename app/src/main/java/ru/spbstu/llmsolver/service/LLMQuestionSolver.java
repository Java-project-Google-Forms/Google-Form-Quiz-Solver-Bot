package ru.spbstu.llmsolver.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.spbstu.llmsolver.client.LanguageModelClient;
import ru.spbstu.llmsolver.parser.AnswerParser;
import ru.spbstu.llmsolver.prompt.PromptBuilder;

import java.util.List;
import java.util.Map;

@Service
public class LLMQuestionSolver {

    private final LanguageModelClient llmClient;
    private final PromptBuilder promptBuilder;
    private final AnswerParser answerParser;

    public LLMQuestionSolver(LanguageModelClient llmClient,
                             PromptBuilder promptBuilder,
                             AnswerParser answerParser) {
        this.llmClient = llmClient;
        this.promptBuilder = promptBuilder;
        this.answerParser = answerParser;
    }

    public Mono<Map<String, AnswerWithConfidence>> solveQuestions(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return Mono.just(Map.of());
        }
        String prompt = promptBuilder.buildPrompt(questions);
        return llmClient.ask(prompt)
                .map(llmAnswer -> answerParser.parseAnswers(llmAnswer, questions));
    }

    public record Question(String id, String text, String type, List<String> options) {}
    public record AnswerWithConfidence(String answer, int confidence) {}
}
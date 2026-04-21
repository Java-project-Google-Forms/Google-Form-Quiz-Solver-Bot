package ru.spbstu.llmsolver.stub;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.spbstu.llmsolver.api.FormResultSender;
import ru.spbstu.llmsolver.service.LLMService;

@Component
@Profile("stub")
public class LLMTestRunner implements CommandLineRunner {
    private final LLMService llmService;
    private final FormResultSender resultSender;

    public LLMTestRunner(LLMService llmService, FormResultSender resultSender) {
        this.llmService = llmService;
        this.resultSender = resultSender;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(3000);
        String testPrompt = "What is the capital of France?";
        Long testChatId = 123L;
        Integer testRequestId = 42;

        System.out.println("LLMTestRunner: sending test request...");
        llmService.generateAnswerAsync(testPrompt,
                answer -> {
                    String resultJson = String.format("{\"answer\": \"%s\"}", answer);
                    resultSender.sendResult(testChatId, testRequestId, resultJson);
                },
                error -> resultSender.sendResult(testChatId, testRequestId, "{\"error\": \"LLM failed\"}")
        );
    }
}
package ru.spbstu.llmsolver.test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.spbstu.llmsolver.api.FormResultSender;
import ru.spbstu.llmsolver.service.LLMService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Configuration
@ComponentScan("ru.spbstu.llmsolver") // сканируем только llmsolver пакет
@PropertySource("classpath:application.properties")
public class LlmOnlyTest {

    public static void main(String[] args) throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LlmOnlyTest.class)) {
            LLMService llmService = context.getBean(LLMService.class);
            FormResultSender sender = context.getBean(FormResultSender.class);

            CountDownLatch latch = new CountDownLatch(1);
            System.out.println("Sending test to LLM...");
            llmService.generateAnswerAsync("What is the capital of France?",
                answer -> {
                    System.out.println("LLM answer: " + answer);
                    sender.sendResult(123L, 1, "{\"answer\": \"" + answer + "\"}");
                    latch.countDown();
                },
                error -> {
                    System.err.println("LLM error: " + error.getMessage());
                    latch.countDown();
                }
            );

            latch.await(10, TimeUnit.SECONDS);
            System.out.println("Test finished.");
        }
    }
}
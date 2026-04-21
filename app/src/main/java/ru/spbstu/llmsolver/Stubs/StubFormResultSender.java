package ru.spbstu.llmsolver.stub;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.spbstu.llmsolver.api.FormResultSender;

@Component
@Profile("stub")
public class StubFormResultSender implements FormResultSender {
    @Override
    public void sendResult(Long chatId, Integer requestId, String resultJson) {
        System.out.println("StubFormResultSender: chatId=" + chatId + 
                           ", requestId=" + requestId + ", result=" + resultJson);
    }
}
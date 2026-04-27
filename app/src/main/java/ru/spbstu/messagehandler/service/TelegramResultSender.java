package ru.spbstu.messagehandler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.spbstu.formsolving.service.ResultSender;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramResultSender implements ResultSender {
    private final int MAX_LEN = 4000;

    private final TelegramMessageSender messageSender;

    @Override
    public void sendResult(Long chatId, String text) {
        if (text.length() <= MAX_LEN) {
            messageSender.sendMessage(chatId, text);
        } else {
            List<String> parts = splitMessage(text);
            for (String part : parts) {
                messageSender.sendMessage(chatId, part);
            }
        }
    }
    private List<String> splitMessage(String text) {
        List<String> parts = new ArrayList<>();
        String remaining = text;
        while (remaining.length() > MAX_LEN) {
            int splitPos = remaining.lastIndexOf('\n', MAX_LEN);
            if (splitPos == -1) splitPos = MAX_LEN;
            parts.add(remaining.substring(0, splitPos));
            remaining = remaining.substring(splitPos);
        }
        parts.add(remaining);
        return parts;
    }
}
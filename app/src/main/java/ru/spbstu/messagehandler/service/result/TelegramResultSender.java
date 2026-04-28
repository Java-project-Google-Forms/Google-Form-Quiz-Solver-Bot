package ru.spbstu.messagehandler.service.result;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.spbstu.formsolving.service.ResultSender;
import ru.spbstu.messagehandler.service.api.TelegramMessageSender;

import java.util.ArrayList;
import java.util.List;


/**
 * Adapter that implements {@link ResultSender}
 * using {@link TelegramMessageSender}. Handles long messages by splitting them
 * into smaller chunks (maximum set to 4000 characters, actual limit is 4096).
 */
@Component
@RequiredArgsConstructor
public class TelegramResultSender implements ResultSender {
    private final int MAX_LEN = 4000;

    private final TelegramMessageSender messageSender;

    /**
     * Sends a result message, automatically splitting it into chunks if its length
     * exceeds the Telegram limit (4096 characters). Ensures chunks are split at newlines.
     * @param chatId recipient chat identifier
     * @param text   message content (may be long)
     */
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

    /**
     * Splits a long string into parts not exceeding MAX_LEN, preferring line breaks.
     * @param text the original text
     * @return list of message parts
     */
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
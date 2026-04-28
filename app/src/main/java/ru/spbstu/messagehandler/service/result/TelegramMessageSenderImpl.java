package ru.spbstu.messagehandler.service.result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import ru.spbstu.messagehandler.bot.QuizTelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.spbstu.messagehandler.service.api.TelegramMessageSender;


/**
 * Implementation of {@link TelegramMessageSender} that uses the actual
 * {@link QuizTelegramBot} to send messages with HTML parsing enabled.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramMessageSenderImpl implements TelegramMessageSender {
    private final QuizTelegramBot bot;

    /**
     * Sends a plain text message to a Telegram chat with HTML parse mode.
     * @param chatId recipient chat identifier
     * @param text   message content
     */
    @Override
    public void sendMessage(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        msg.setParseMode(ParseMode.HTML);
        try {
            bot.execute(msg);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}", chatId, e);
        }
    }
}
package ru.spbstu.messagehandler.bot;

import ru.spbstu.messagehandler.config.BotConfig;
import ru.spbstu.messagehandler.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class QuizTelegramBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(QuizTelegramBot.class);

    private final BotConfig botConfig;
    private final MessageHandler messageHandler;

    public QuizTelegramBot(BotConfig botConfig, MessageHandler messageHandler) {
        this.botConfig = botConfig;
        this.messageHandler = messageHandler;
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage response = messageHandler.handle(update.getMessage());
            try {
                execute(response);
                log.debug("Reply sent to chat {}", update.getMessage().getChatId());
            } catch (TelegramApiException e) {
                log.error("Failed to send message", e);
            }
        }
    }
}
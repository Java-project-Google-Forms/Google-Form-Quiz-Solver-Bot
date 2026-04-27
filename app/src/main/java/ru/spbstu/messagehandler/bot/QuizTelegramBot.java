package ru.spbstu.messagehandler.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.spbstu.messagehandler.config.BotConfig;
import ru.spbstu.messagehandler.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@RequiredArgsConstructor
public class QuizTelegramBot extends TelegramLongPollingBot {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final BotConfig botConfig;
    private final MessageHandler messageHandler;

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
            executor.submit(() -> {
                SendMessage response = messageHandler.handle(update.getMessage());
                try {
                    execute(response);
                    log.debug("Reply sent to chat {}", update.getMessage().getChatId());
                } catch (TelegramApiException e) {
                    log.error("Failed to send message", e);
                }
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
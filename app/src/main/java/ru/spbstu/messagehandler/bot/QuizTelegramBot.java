package ru.spbstu.messagehandler.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import ru.spbstu.messagehandler.config.BotConfig;
import ru.spbstu.messagehandler.handler.MessageHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Telegram bot implementation using long-polling.
 * <p>
 * This bot receives updates from Telegram via {@link TelegramLongPollingBot}
 * and delegates text messages to {@link MessageHandler} for processing.
 * Message handling is performed asynchronously in a fixed thread pool
 * to avoid blocking the bot's update processing thread.
 * </p>
 */
@Component
@Slf4j
public class QuizTelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final MessageHandler messageHandler;
    private final ExecutorService executor;

    public QuizTelegramBot(BotConfig botConfig, MessageHandler messageHandler) {
        this.botConfig = botConfig;
        this.messageHandler = messageHandler;
        this.executor = Executors.newFixedThreadPool(botConfig.getThreadPoolSize());
    }

    /**
     * Returns the bot token used for authentication with Telegram API.
     * @return bot token (never null, validated in BotConfig)
     */
    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    /**
     * Returns the bot username as shown in Telegram.
     * @return bot username
     */
    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    /**
     * Processes an incoming update from Telegram.
     * If the update contains a text message, it is submitted to a worker thread.
     * The reply is sent synchronously within that thread.
     * @param update the update object received from Telegram
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            executor.submit(() -> {
                long chatId = update.getMessage().getChatId();
                try {
                    SendMessage response = messageHandler.handle(update.getMessage());
                    execute(response);
                    log.debug("Reply sent to chat {}", chatId);
                } catch (TelegramApiException e) {
                    log.error("Failed to send message to chat {}", chatId, e);
                    try {
                        SendMessage errorMsg = new SendMessage();
                        errorMsg.setChatId(String.valueOf(chatId));
                        errorMsg.setText("⚠️ Извините, произошла техническая ошибка. Мы уже работаем над её исправлением.");
                        errorMsg.setParseMode(ParseMode.HTML);
                        execute(errorMsg);
                    } catch (TelegramApiException ex) {
                        log.error("Even error message could not be sent to chat {}", chatId, ex);
                    }
                }
            });
        }
    }

    /**
     * Gracefully shuts down the executor service when the application stops.
     * Waits for currently executing tasks to finish.
     */
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
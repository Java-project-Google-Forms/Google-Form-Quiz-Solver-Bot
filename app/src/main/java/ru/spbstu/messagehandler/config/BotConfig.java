package ru.spbstu.messagehandler.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the Telegram bot.
 * Reads values from {@code application.properties} or environment variables.
 */
@Component
@Getter
public class BotConfig {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.username:MyQuizBot}")
    private String botUsername;

    @Value("${telegram.bot.thread-pool-size:10}")
    private int threadPoolSize;

    /**
     * Returns the bot token, throwing an exception if it is not configured.
     * @return bot token
     * @throws IllegalStateException if the token is missing
     */
    public String getBotToken() {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException(
                "Telegram bot token is not configured! " +
                "Please set TELEGRAM_BOT_TOKEN environment variable or add 'telegram.bot.token' to application.properties"
            );
        }
        return botToken;
    }
}
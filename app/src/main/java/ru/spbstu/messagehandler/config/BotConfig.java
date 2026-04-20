package ru.spbstu.messagehandler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BotConfig {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.username:MyQuizBot}")
    private String botUsername;

    public String getBotToken() {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException(
                    "Telegram bot token is not configured! " +
                            "Please set TELEGRAM_BOT_TOKEN environment variable or add 'telegram.bot.token' to application.properties"
            );
        }
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }
}
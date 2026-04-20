package ru.spbstu.messagehandler.initializer;

import ru.spbstu.messagehandler.bot.QuizTelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer {

    private static final Logger log = LoggerFactory.getLogger(BotInitializer.class);
    private final QuizTelegramBot quizBot;

    public BotInitializer(QuizTelegramBot quizBot) {
        this.quizBot = quizBot;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(quizBot);
            log.info("✅ Telegram bot '{}' successfully registered", quizBot.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("❌ Failed to register Telegram bot", e);
        }
    }
}
package ru.spbstu.messagehandler.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.spbstu.messagehandler.bot.QuizTelegramBot;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@Slf4j
@RequiredArgsConstructor
public class BotInitializer {
    private final QuizTelegramBot quizBot;

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
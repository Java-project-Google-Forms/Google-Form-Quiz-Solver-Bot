package ru.spbstu.messagehandler.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import ru.spbstu.messagehandler.bot.QuizTelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramMessageSenderImpl implements TelegramMessageSender {
    private final QuizTelegramBot bot;

    public TelegramMessageSenderImpl(QuizTelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        msg.setParseMode(ParseMode.HTML);
        try {
            bot.execute(msg);
        } catch (TelegramApiException e) {
            // TODO логирование ошибки
        }
    }
}
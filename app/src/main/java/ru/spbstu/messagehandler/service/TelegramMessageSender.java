package ru.spbstu.messagehandler.service;

public interface TelegramMessageSender {
    void sendMessage(Long chatId, String text);
}
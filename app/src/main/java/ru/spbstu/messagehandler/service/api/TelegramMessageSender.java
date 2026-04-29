package ru.spbstu.messagehandler.service.api;

/**
 * Abstractions for sending messages to Telegram chats.
 * <p>
 * This service is intended to be implemented by the {@code messagehandler} module.
 * </p>
 */
public interface TelegramMessageSender {
    void sendMessage(Long chatId, String text);
}
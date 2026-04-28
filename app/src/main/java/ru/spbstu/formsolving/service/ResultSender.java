package ru.spbstu.formsolving.service;

/**
 * Strategy interface for delivering textual results to the end user (e.g., via Telegram).
 */
public interface ResultSender {

    /**
     * Sends a message to the specified chat. The implementation may split long messages
     * to comply with platform limits (e.g., 4096 characters for Telegram).
     *
     * @param chatId recipient identifier
     * @param text   message content
     */
    void sendResult(Long chatId, String text);
}
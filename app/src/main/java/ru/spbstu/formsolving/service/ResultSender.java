package ru.spbstu.formsolving.service;

public interface ResultSender {
    void sendResult(Long chatId, String text);
}
package ru.spbstu.llmsolver.api;

public interface FormResultSender {
    void sendResult(Long chatId, Integer requestId, String resultJson);
}
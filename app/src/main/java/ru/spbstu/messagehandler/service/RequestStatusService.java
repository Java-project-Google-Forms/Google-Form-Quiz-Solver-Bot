package ru.spbstu.messagehandler.service;

public interface RequestStatusService {
    String getStatus(Long chatId, Integer requestId);
}
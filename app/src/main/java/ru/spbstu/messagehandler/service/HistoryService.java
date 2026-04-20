package ru.spbstu.messagehandler.service;

public interface HistoryService {
    String getHistory(Long chatId, String period); // period: day, week, month, all
    String getMyForms(Long chatId);
    String getForm(Long chatId, Integer formId);
    String removeForm(Long chatId, Integer formId);
}
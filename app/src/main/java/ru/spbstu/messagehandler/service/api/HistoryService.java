package ru.spbstu.messagehandler.service.api;

/**
 * Provides access to user history, saved forms, and form details.
 * <p>
 * This service is intended to be implemented by the {@code history} module.
 * </p>
 */
public interface HistoryService {
    String getHistory(Long chatId, String period); // period: day, week, month, all
    String getMyForms(Long chatId);
    String getForm(Long chatId, String formId);
    String removeForm(Long chatId, String formId);
}
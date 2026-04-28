package ru.spbstu.messagehandler.service;

import ru.spbstu.database.document.FormDocument;

public interface FormStorageService {
    // Сохранить решённую форму и обновить историю пользователя
    void saveForm(Long chatId, FormDocument form);
    // Обновить статус запроса
    void updateRequestStatus(Long chatId, String requestId, String status);
    // Создать новый запрос и вернуть его ID
    String createRequest(Long chatId);
    void finalizeRequest(Long chatId);
}

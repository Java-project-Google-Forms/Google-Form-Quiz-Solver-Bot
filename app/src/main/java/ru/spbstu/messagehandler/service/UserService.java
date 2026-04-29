package ru.spbstu.messagehandler.service;

import ru.spbstu.database.document.UserDocument;

public interface UserService {
    UserDocument getOrCreateUser(Long chatId, String name);
    boolean hasActiveRequest(Long chatId);
    void setActiveRequest(Long chatId, boolean active);
}

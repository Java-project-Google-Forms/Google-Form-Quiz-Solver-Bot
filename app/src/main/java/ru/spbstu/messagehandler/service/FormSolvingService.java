package ru.spbstu.messagehandler.service;

public interface FormSolvingService {
    // For long requests, we send response (ACK) asap and then actual response when ready
    boolean solveForm(Long chatId, String link);
    boolean rescoreForm(Long chatId, Integer formId);
}
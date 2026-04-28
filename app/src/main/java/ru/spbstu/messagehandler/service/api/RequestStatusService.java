package ru.spbstu.messagehandler.service.api;

/**
 * Allows querying the status of a form solving request.
 * <p>
 * This service is intended to be implemented by the {@code requeststatus} module.
 * </p>
 */
public interface RequestStatusService {
    String getStatus(Long chatId, String requestId);
}
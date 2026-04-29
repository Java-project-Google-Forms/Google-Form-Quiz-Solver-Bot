package ru.spbstu.formsolving.service.api;

/**
 * Contract for initiating form solving or rescoring.
 * <p>
 * This service is intended to be implemented by the {@code formsolving} module.
 * </p>
 */
public interface FormSolvingService {

    /**
     * Starts solving a Google Form.
     * @param chatId Telegram chat ID for result delivery
     * @param link   URL of the Google Form
     * @return true if the request was accepted (asynchronous processing started)
     */
    boolean solveForm(Long chatId, String link);

    /**
     * Requests re‑evaluation of a previously solved form.
     * @param chatId Telegram chat ID
     * @param formId identifier of the form (string UUID in practice, but currently Integer)
     * @return true if the rescore request was accepted
     */
    boolean rescoreForm(Long chatId, String formId);
}
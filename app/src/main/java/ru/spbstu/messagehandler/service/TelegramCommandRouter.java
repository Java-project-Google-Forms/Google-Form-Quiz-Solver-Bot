package ru.spbstu.messagehandler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.spbstu.formsolving.service.api.FormSolvingService;
import ru.spbstu.messagehandler.service.api.HistoryService;
import ru.spbstu.messagehandler.service.api.RequestStatusService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

import static ru.spbstu.messagehandler.handler.MessageHandler.FORM_LINK_REGEX;

/**
 * Routes Telegram commands to the appropriate underlying services
 * (form solving, history, status).
 * <p>
 * Each method returns a plain text response that will be sent back to the user.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class TelegramCommandRouter {
    private final FormSolvingService formSolving;
    private final HistoryService history;
    private final RequestStatusService requestStatus;

    /**
     * Returns the welcome message with bot description and usage hints.
     * @return formatted HTML text
     */
    public String handleStart() {
        return """
                Привет! Я бот для автоматического решения Google‑форм.
                
                <b>Функциональные возможности</b>
                
                Бот принимает ссылки на публичные Google‑формы и возвращает ответы на каждый вопрос, который может обработать.
                
                Бот не решает вопросы включающие в себя картинки, видео, загрузку файлов и сеточные типы вопросов.
                
                <b>Способы взаимодействия</b>
                
                Для инициирования обработки формы отправьте в чат ссылку на форму в одном из следующих форматов:
                • <code>https://forms.gle/...</code>
                • <code>https://docs.google.com/forms/d/...</code>
                либо воспользуйтесь командой <code> /solve &lt;link&gt;</code>.
                
                Для вывода всех команд с кратким описанием можете воспользоваться командой <code>/help</code>.
                
                <b>Приступим?</b>
                Можете отправить мне ссылку на публичную Google-форму и я сразу начну работу!
                """;
    }

    /**
     * Returns help message listing all available commands.
     * @return formatted HTML text
     */
    public String handleHelp() {
        return """
                🤖 <b>Доступные команды:</b>
                /help – показать это сообщение
                /myforms – список ваших форм
                /solve &lt;link to form&gt; – начать решение формы
                /rescore &lt;formId&gt; – пересчитать результаты
                /history [day|week|month|all] – история ответов
                /get_form &lt;formId&gt; – получить содержимое формы
                /remove_form &lt;formId&gt; – удалить форму
                /status &lt;formId&gt; – статус обработки формы
                """;
    }

    /**
     * Displays the user's saved forms.
     * @param chatId Telegram chat identifier
     * @return formatted list of forms or an error message
     */
    public String handleMyForms(Long chatId) {
        // TODO: получить из БД формы пользователя
        return history.getMyForms(chatId);
    }

    /**
     * Initiates solving of a Google Form and returns a confirmation.
     * @param argument the raw argument (expected to contain the form URL)
     * @param chatId   user's chat identifier
     * @return confirmation or error text
     */
    public String handleSolve(String argument, Long chatId) {
        // TODO: инициировать процесс решения формы
        if (argument == null || argument.isBlank()) {
            return "❌ Укажите ссылку на форму: /solve &lt;link to form&gt;";
        }
        Matcher matcher = FORM_LINK_REGEX.matcher(argument.strip());
        if (matcher.find()) {
            if (formSolving.solveForm(chatId, matcher.group())) {
                return "Отправили запрос на обработку формы, ожидайте...";
            } else {
                return "Не удалось принять форму проверьте переданную ссылку.";
            }
        } else {
            return "❌ Укажите ссылку на форму: /solve &lt;link to form&gt;";
        }

    }

    /**
     * Requests re‑evaluation of a previously solved form.
     * @param argument the raw argument (integer formId)
     * @param chatId   user's chat identifier
     * @return acceptance message or error
     */
    public String handleRescore(String argument, Long chatId) {
        // TODO: отправить запрос на пересчёт
        if (argument == null || argument.isBlank()) {
            return "❌ Укажите ID формы: /rescore &lt;formId&gt;";
        }
        try {
            Integer formId = Integer.parseInt(argument.trim());
            if (formSolving.rescoreForm(chatId, formId)) {
                return "Отправили запрос на повторную обработку формы, ожидайте...";
            } else {
                return "Не удалось принять форму на повторную обработку.";
            }
        } catch (NumberFormatException e) {
            return "❌ ID формы должен быть числом";
        }
    }

    /**
     * Returns the user's answer history for a given time period.
     * @param argument period (day, week, month, all) or empty
     * @param chatId   user's chat identifier
     * @return formatted history or error
     */
    public String handleHistory(String argument, Long chatId) {
        // TODO: получить историю из БД
        List<String> allowedPeriods = Arrays.asList("day", "week", "month", "all");
        if (argument != null && !argument.isBlank()) {
            String period = argument.trim().toLowerCase();
            if (allowedPeriods.contains(period)) {
                return history.getHistory(chatId, period);
            } else {
                return "❌ Неверный период. Используйте: day, week, month, all";
            }
        } else {
            return "📜 История за всё время (заглушка)";
        }
    }

    /**
     * Returns the full content of a previously solved form with answers.
     * @param argument form UUID
     * @param chatId   user's chat identifier
     * @return formatted form content or error
     */
    public String handleGetForm(String argument, Long chatId) {
        // TODO: получить данные формы
        if (argument == null || argument.isBlank()) {
            return "❌ Укажите ID формы: /get_form &lt;formId&gt;";
        }
        try {
            int formId = Integer.parseInt(argument.trim());
            return history.getForm(chatId, formId);
        } catch (NumberFormatException e) {
            return "❌ ID формы должен быть числом";
        }
    }

    /**
     * Removes a form from the user's saved list.
     * @param argument form UUID
     * @param chatId   user's chat identifier
     * @result success or error message
     */
    public String handleRemoveForm(String argument, Long chatId) {
        // TODO: удалить форму из БД
        if (argument == null || argument.isBlank()) {
            return "❌ Укажите ID формы: /remove_form &lt;formId&gt;";
        }
        try {
            Integer formId = Integer.parseInt(argument.trim());
            return history.removeForm(chatId, formId);
        } catch (NumberFormatException e) {
            return "❌ ID формы должен быть числом";
        }
    }

    /**
     * Retrieves the current processing status of a request.
     * @param argument request UUID
     * @param chatId   user's chat identifier
     * @return status text or error
     */
    public String handleStatus(String argument, Long chatId) {
        if (argument == null || argument.isBlank()) {
            return "❌ Укажите ID запроса: /status <requestId>";
        }
        String requestId = argument.trim();
        if (!isValidUuid(requestId)) {
            return "❌ Неверный формат ID запроса.";
        }
        return requestStatus.getStatus(chatId, requestId);
    }

    /**
     * Returns an error message for unrecognised commands.
     * @return error text
     */
    public String handleUnknownCommand() {
        return "❓ Неизвестная команда! \nВведите /help для списка команд.";
    }

    /**
     * Validates that a string can be parsed as a UUID.
     * @param str string to validate
     * @return true if valid UUID, false otherwise
     */
    private boolean isValidUuid(String str) {
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
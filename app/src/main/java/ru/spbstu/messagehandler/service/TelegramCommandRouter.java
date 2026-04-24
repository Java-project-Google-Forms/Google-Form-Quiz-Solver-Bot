package ru.spbstu.messagehandler.service;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import static ru.spbstu.messagehandler.handler.MessageHandler.FORM_LINK_REGEX;

/**
 * Маршрутизатор команд Telegram.
 * Координирует вызовы других модулей (MongoDB, Kafka, GigaChat и т.д.)
 */
@Component
public class TelegramCommandRouter {
    private final FormSolvingService formSolving;
    private final HistoryService history;
    private final RequestStatusService requestStatus;

    public TelegramCommandRouter(FormSolvingService formSolving, HistoryService history, RequestStatusService requestStatus) {
        this.formSolving = formSolving;
        this.history = history;
        this.requestStatus = requestStatus;
    }

    /**
     * Обработка команды /help
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
     * Обработка команды /myforms
     * Показывает список форм/викторин пользователя
     */
    public String handleMyForms(Long chatId) {
        // TODO: получить из БД формы пользователя
        return history.getMyForms(chatId);
    }

    /**
     * Обработка команды /solve &lt;link to form&gt;
     * Начинает решение формы по её идентификатору
     *
     * @param argument аргумент после /solve
     * @param chatId   идентификатор чата
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
     * Обработка команды /rescore &lt;formId&gt;
     * Запрашивает пересчёт результатов формы
     *
     * @param argument аргумент после /rescore
     * @param chatId   идентификатор чата
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
     * Обработка команды /history [период]
     * Показывает историю ответов пользователя
     *
     * @param argument аргумент после /history (day/week/month/all или пусто)
     * @param chatId   идентификатор чата
     */
     public String handleHistory(String argument, Long chatId) {
    	List<String> allowedPeriods = Arrays.asList("day", "week", "month", "all");
    	if (argument != null && !argument.isBlank()) {
            String period = argument.trim().toLowerCase();
            if (allowedPeriods.contains(period)) {
                return history.getHistory(chatId, period);
            } else {
                return "❌ Неверный период. Используйте: day, week, month, all";
            }
        } else {
            return history.getHistory(chatId, "week");
        }
     }

    /**
     * Обработка команды /get_form &lt;formId&gt;
     * Возвращает содержимое формы по ID
     *
     * @param argument аргумент после /get_form
     * @param chatId   идентификатор чата
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
     * Обработка команды /remove_form &lt;formId&gt;
     * Удаляет форму пользователя
     *
     * @param argument аргумент после /remove_form
     * @param chatId   идентификатор чата
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
     * Обработка команды /status &lt;formId&gt;
     * Проверяет статус обработки формы
     *
     * @param argument аргумент после /status
     * @param chatId   идентификатор чата
     */
    public String handleStatus(String argument, Long chatId) {
        // TODO: запросить статус через Kafka или из БД
        if (argument == null || argument.isBlank()) {
            return "❌ Укажите ID формы: /status &lt;formId&gt;";
        }
        try {
            Integer requestId = Integer.parseInt(argument.trim());
            return requestStatus.getStatus(chatId, requestId);
        } catch (NumberFormatException e) {
            return "❌ ID формы должен быть числом";
        }
    }

    /**
     * Обработка неизвестной команды
     */
    public String handleUnknownCommand() {
        return "❓ Неизвестная команда! \nВведите /help для списка команд.";
    }
}

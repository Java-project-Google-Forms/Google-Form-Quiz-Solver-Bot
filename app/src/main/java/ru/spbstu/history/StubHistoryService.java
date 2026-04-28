package ru.spbstu.history;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.spbstu.messagehandler.service.api.HistoryService;

@Component
@Profile("stub")
public class StubHistoryService implements HistoryService {
    @Override
    public String getHistory(Long chatId, String period) {
        return "📜 История (заглушка): для chatId=" + chatId + ", период=" + period;
    }

    @Override
    public String getMyForms(Long chatId) {
        return "📜 Сохраненные формы (заглушка): для chatId=" + chatId + ".";
    }

    @Override
    public String getForm(Long chatId, Integer formId) {
        return "Получение формы по id " + formId + " для chatId = " + chatId + ".";
    }

    @Override
    public String removeForm(Long chatId, Integer formId) {
        return "Удаление формы по id " + formId + " для chatId = " + chatId + ".";
    }
}
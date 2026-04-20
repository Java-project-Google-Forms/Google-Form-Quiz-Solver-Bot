package ru.spbstu.formsolving;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.spbstu.messagehandler.service.FormSolvingService;

@Component
@Profile("stub")
public class StubFormSolvingService implements FormSolvingService {
    @Override
    public boolean solveForm(Long chatId, String link) {
        if (true) {
            // if form is in right format and everything ok - send back true.
            return true;
        }
        return false;
    }
    @Override
    public boolean rescoreForm(Long chatId, Integer formId) {
        if (true) {
            // if form is in right format and everything ok - send back true.
            return true;
        }
        return false;
    }
}
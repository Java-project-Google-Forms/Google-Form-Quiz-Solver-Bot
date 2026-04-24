package ru.spbstu.formsolving;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.spbstu.messagehandler.service.FormSolvingService;

@Component
@Profile("mongo")
public class MongoFormSolvingService implements FormSolvingService {

    @Override
    public boolean solveForm(Long chatId, String link) {
        return true;
    }

    @Override
    public boolean rescoreForm(Long chatId, Integer formId) {
        return true;
    }
}

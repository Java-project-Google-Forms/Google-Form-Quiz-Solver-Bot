package ru.spbstu.database;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.spbstu.database.document.FormDocument;
import ru.spbstu.database.document.HistoryEntryDocument;
import ru.spbstu.database.document.RequestStatusDocument;
import ru.spbstu.database.repository.FormRepository;
import ru.spbstu.database.repository.RequestStatusRepository;
import ru.spbstu.database.repository.UserRepository;
import ru.spbstu.messagehandler.service.FormStorageService;

import java.time.Instant;
import java.util.Random;

@Component
@Profile("mongo")
public class MongoFormStorageService implements FormStorageService {

    private final FormRepository formRepository;
    private final UserRepository userRepository;
    private final RequestStatusRepository requestStatusRepository;

    public MongoFormStorageService(FormRepository formRepository,
                                   UserRepository userRepository,
                                   RequestStatusRepository requestStatusRepository) {
        this.formRepository = formRepository;
        this.userRepository = userRepository;
        this.requestStatusRepository = requestStatusRepository;
    }

    @Override
public void saveForm(Long chatId, FormDocument form) {
    // Получаем userId пользователя и устанавливаем в форму
    userRepository.findByChatId(chatId.toString())
            .flatMap(user -> {
                form.setOwnerId(user.getUserId());
                return formRepository.save(form)
                        .flatMap(saved -> {
                            if (!user.getSavedForms().contains(form.getFormId())) {
                                user.getSavedForms().add(form.getFormId());
                            }
                            HistoryEntryDocument entry = new HistoryEntryDocument();
                            entry.setFormId(form.getFormId());
                            entry.setStatus("COMPLETED");
                            entry.setSolvedDate(Instant.now());
                            user.getHistory().add(entry);
                            return userRepository.save(user);
                        });
            })
            .block();
}

    @Override
    public void updateRequestStatus(Long chatId, Integer requestId, String status) {
        requestStatusRepository.findByRequestIdAndChatId(requestId, chatId.toString())
                .flatMap(doc -> {
                    doc.setStatus(status);
                    return requestStatusRepository.save(doc);
                })
                .block();
    }

    @Override
    public Integer createRequest(Long chatId) {
        RequestStatusDocument doc = new RequestStatusDocument();
        Integer requestId = new Random().nextInt(100000);
        doc.setRequestId(requestId);
        doc.setChatId(chatId.toString());
        doc.setStatus("PENDING");
        doc.setCreatedAt(Instant.now());
        requestStatusRepository.save(doc).block();

        // Отмечаем что у пользователя есть активный запрос
        userRepository.findByChatId(chatId.toString())
                .flatMap(user -> {
                    user.setHasCurrentRequest(true);
                    return userRepository.save(user);
                })
                .block();

        return requestId;
    }
}

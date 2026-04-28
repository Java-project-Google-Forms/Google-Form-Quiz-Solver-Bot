package ru.spbstu.database;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.spbstu.database.document.FormDocument;
import ru.spbstu.database.document.HistoryEntryDocument;
import ru.spbstu.database.document.RequestStatusDocument;
import ru.spbstu.database.repository.FormRepository;
import ru.spbstu.database.repository.RequestStatusRepository;
import ru.spbstu.database.repository.UserRepository;
import ru.spbstu.formsolving.model.FormStructure;
import ru.spbstu.formsolving.model.QuestionType;
import ru.spbstu.messagehandler.service.FormStorageService;
import ru.spbstu.database.document.UserDocument;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile("mongo")
@RequiredArgsConstructor
public class MongoFormStorageService implements FormStorageService {

    private final FormRepository formRepository;
    private final UserRepository userRepository;
    private final RequestStatusRepository requestStatusRepository;

    @Override
    public void saveForm(Long chatId, FormDocument form) {
        userRepository.findByChatId(chatId.toString())
                // Если пользователь не найден (например, не нажал /start), создаем заглушку
                .switchIfEmpty(Mono.defer(() -> {
                    UserDocument newUser = new UserDocument();
                    newUser.setChatId(chatId.toString());
                    newUser.setUserId(Math.abs(chatId.intValue()));
                    newUser.setName("Unknown User");
                    return userRepository.save(newUser);
                }))
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
                .block(); // 
    }

    @Override
    public void updateRequestStatus(Long chatId, String requestId, String status) {
        requestStatusRepository.findByRequestIdAndChatId(requestId, chatId.toString())
                .flatMap(doc -> {
                    doc.setStatus(status);
                    return requestStatusRepository.save(doc);
                })
                .block();
    }

    @Override
    public String createRequest(Long chatId) {
        RequestStatusDocument doc = new RequestStatusDocument();
        String requestId = UUID.randomUUID().toString();
        doc.setRequestId(requestId);
        doc.setChatId(chatId.toString());
        doc.setStatus("PENDING");
        doc.setCreatedAt(Instant.now());
        requestStatusRepository.save(doc).block(); // Это у вас работает

        // ФОРСИРУЕМ сохранение пользователя, если его нет
        userRepository.findByChatId(chatId.toString())
                .switchIfEmpty(Mono.defer(() -> {
                    UserDocument newUser = new UserDocument();
                    newUser.setChatId(chatId.toString());
                    newUser.setUserId(Math.abs(chatId.intValue()));
                    newUser.setName("User_" + chatId);
                    return userRepository.save(newUser);
                }))
                .flatMap(user -> {
                    user.setHasCurrentRequest(true);
                    return userRepository.save(user);
                })
                .block();

        return requestId;
    }

    public void finalizeRequest(Long chatId) {
        userRepository.findByChatId(chatId.toString())
                .flatMap(user -> {
                    user.setHasCurrentRequest(false);
                    return userRepository.save(user);
                }).block();
    }

    public FormStructure getFormStructure(Long chatId, String formId) throws NoSuchFieldException {
        FormDocument formDocument =
                formRepository.findByOwnerIdAndFormId((Integer) chatId.intValue(),
                formId).block();
        // TODO REMOVE
        System.out.println("THIS IS GET FORM_STRUCTURE test:"+ chatId.intValue() + " "+ formId);

        if (formDocument == null) throw new NoSuchFieldException();

        return toFormStructure(formDocument);

    }

    public FormStructure toFormStructure(FormDocument formDocument) {
        if (formDocument.getQuestions() == null) {
            return new FormStructure(formDocument.getFormName(), "", List.of());
        }
        List<ru.spbstu.formsolving.model.Question> modelQuestions = formDocument.getQuestions().stream()
                .map(qDoc -> {
                    ru.spbstu.formsolving.model.Question q = new ru.spbstu.formsolving.model.Question();
                    q.setTitle(qDoc.getBody());
                    q.setType(parseType(qDoc.getType()));
                    // остальные поля (required, options, scale и т.д.) не сохраняются в БД, поэтому остаются null/значения по умолчанию
                    q.setRequired(false);
                    q.setOptions(null);
                    q.setShuffle(false);
                    q.setScale(null);
                    q.setGrid(null);
                    q.setDate(null);
                    q.setTime(null);
                    q.setId(null);
                    q.setDescription(null);
                    return q;
                })
                .collect(Collectors.toList());
        return new FormStructure(formDocument.getFormName(), "", modelQuestions);
    }

    private static QuestionType parseType(String typeStr) {
        if (typeStr == null) return QuestionType.UNSUPPORTED;
        try {
            return QuestionType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return QuestionType.UNSUPPORTED;
        }
    }
}

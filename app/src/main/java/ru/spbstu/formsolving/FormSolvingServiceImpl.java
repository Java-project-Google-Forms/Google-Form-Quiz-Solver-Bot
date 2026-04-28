package ru.spbstu.formsolving;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.spbstu.formsolving.entity.*;
import ru.spbstu.formsolving.parser.FormatStructure;
import ru.spbstu.formsolving.parser.GoogleFormsJsonParser;
import ru.spbstu.formsolving.service.FormSolvingProvider;
import ru.spbstu.formsolving.service.KafkaProducerService;
import ru.spbstu.formsolving.service.ResultSender;
import ru.spbstu.messagehandler.service.FormSolvingService;


import ru.spbstu.database.MongoFormStorageService;
import ru.spbstu.database.document.FormDocument;
import ru.spbstu.database.document.QuestionDocument;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class FormSolvingServiceImpl implements FormSolvingService, FormSolvingProvider {

    private final GoogleFormsJsonParser parser;
    private final KafkaProducerService kafkaProducer;
    private final ResultSender resultSender;
    //
    private final MongoFormStorageService storageService;

    private final Map<String, FormTaskInfo> tasks = new ConcurrentHashMap<>();

    public FormSolvingServiceImpl(GoogleFormsJsonParser parser,
                                  KafkaProducerService kafkaProducer,
                                  @Lazy ResultSender resultSender,
                                  MongoFormStorageService storageService) { //
        this.parser = parser;
        this.kafkaProducer = kafkaProducer;
        this.resultSender = resultSender;
        this.storageService = storageService;
    }

    @Override
    public boolean solveForm(Long chatId, String link) {
        try {
            FormStructure structure = parser.parse(link);
            if (!parser.isValid(structure)) {
                resultSender.sendResult(chatId, "❌ Форма содержит неподдерживаемые типы вопросов или не содержит вопросов.");
                return false;
            }
            //Integer internalId = storageService.createRequest(chatId);
            //String requestId = internalId.toString();
            String requestId = storageService.createRequest(chatId);
            tasks.put(requestId, new FormTaskInfo(chatId, link, structure));

            // Отправляем в Kafka только requestId (неблокирующе)
            kafkaProducer.sendSolveTask(requestId);

            // Отправляем пользователю подтверждение
            resultSender.sendResult(chatId, "✅ Форма принята в обработку. ID запроса: " + requestId);
            return true;
        } catch (Exception e) {
            resultSender.sendResult(chatId, "❌ Ошибка при обработке ссылки: " + e.getMessage());
            log.error("solveForm failed", e);
            return false;
        }
    }

    @Override
    public boolean rescoreForm(Long chatId, Integer formId) {
        String requestId = UUID.randomUUID().toString();
        FormStructure stubStructure = new FormStructure(
                "Stub form", "No description", java.util.List.of()
        );
        tasks.put(requestId, new FormTaskInfo(chatId, null, stubStructure));
        kafkaProducer.sendRescoreTask(requestId);
        log.info("Rescore task enqueued (stub): requestId={}, chatId={}, formId={}",
                requestId, chatId, formId);
        return true;
    }

    // === Реализация FormSolvingProvider ===

    @Override
    public Optional<FormStructure> getFormStructure(String requestId) {
        FormTaskInfo info = tasks.get(requestId);
        return info != null ? Optional.of(info.structure()) : Optional.empty();
    }

    @Override
    public void submitResult(String requestId, SolvingResult result) {
        FormTaskInfo info = tasks.remove(requestId);
        if (info == null) {
            log.error("No task found for requestId={}", requestId);
            return;
        }
        //
        try {
            FormDocument doc = new FormDocument();
            doc.setFormId(requestId);
            doc.setFormName(info.structure().getTitle());
            doc.setSolved(true);

            doc.setFormLink(info.formUrl()); 
        
            doc.setSolved(true);
            // Здесь можно добавить цикл по вопросам, если нужно сохранять их в БД

            java.util.List<ru.spbstu.database.document.QuestionDocument> questionDocs = info.structure().getQuestions().stream()
                    .map(q -> {
                        ru.spbstu.database.document.QuestionDocument qDoc = new ru.spbstu.database.document.QuestionDocument();
                        qDoc.setBody(q.getTitle());
                        qDoc.setType(q.getType().name());
                        
                        Object answer = result.answers().get(q.getId());
                        qDoc.setAnswer(answer != null ? answer : "Ответ не найден");
                        return qDoc;
                    })
                    .collect(java.util.stream.Collectors.toList());

            doc.setQuestions(questionDocs);
            
            storageService.saveForm(info.chatId(), doc);
            storageService.updateRequestStatus(info.chatId(), requestId, "COMPLETED");
            storageService.finalizeRequest(info.chatId()); // Тот самый метод для сброса флага
        } catch (Exception e) {
            log.error("Failed to save to MongoDB", e);
        }
        //


        StringBuilder sb = new StringBuilder();
        sb.append("✅ Результат решения формы \n\n").append(FormatStructure.escapeHtml(info.structure().getTitle()))
                .append(":\n\n");
        int idx = 1;
        for (Question q : info.structure().getQuestions()) {
            String answer = result.answers().getOrDefault(q.getId(), "—");
            sb.append(idx++).append(". ").append(FormatStructure.escapeHtml(q.getTitle())).append(
                    "\n");
            sb.append("   ▶ ").append(FormatStructure.escapeHtml(answer)).append("\n\n");
        }
        resultSender.sendResult(info.chatId(), sb.toString());
        log.info("Result sent to user {} for requestId={}", info.chatId(), requestId);
    }
}
package ru.spbstu.formsolving;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.spbstu.formsolving.model.*;
import ru.spbstu.formsolving.parser.FormatStructure;
import ru.spbstu.formsolving.parser.GoogleFormsJsonParser;
import ru.spbstu.formsolving.service.FormSolvingProvider;
import ru.spbstu.formsolving.service.KafkaProducerService;
import ru.spbstu.formsolving.service.ResultSender;
import ru.spbstu.formsolving.service.api.FormSolvingService;


import ru.spbstu.database.document.FormDocument;
import ru.spbstu.messagehandler.service.FormStorageService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


// TODO requestId should be stored in persistence not locally
/**
 * Main service that implements both the external {@link FormSolvingService}
 * (called by the message handler) and the internal {@link FormSolvingProvider}
 * (called by the LLM solver).
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Parse a Google Form using {@link GoogleFormsJsonParser}</li>
 *   <li>Generate a unique UUID request ID</li>
 *   <li>Store the request metadata in a local in‑memory cache</li>
 *   <li>Send a Kafka task (synchronously with timeout) via {@link KafkaProducerService}</li>
 *   <li>Upon result submission, deliver the answers back to the user via {@link ResultSender}</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
public class FormSolvingServiceImpl implements FormSolvingService, FormSolvingProvider {

    private final GoogleFormsJsonParser parser;
    private final KafkaProducerService kafkaProducer;
    private final ResultSender resultSender;
    //
    private final FormStorageService storageService;

    private final Map<String, FormTaskInfo> tasks = new ConcurrentHashMap<>();

    /**
     * Creates a new instance.
     *
     * @param parser          the HTML/JSON parser
     * @param kafkaProducer   service for sending Kafka messages
     * @param resultSender    service for delivering final answers to the user
     */
    public FormSolvingServiceImpl(GoogleFormsJsonParser parser,
                                  KafkaProducerService kafkaProducer,
                                  @Lazy ResultSender resultSender,
                                  FormStorageService storageService) { //
        this.parser = parser;
        this.kafkaProducer = kafkaProducer;
        this.resultSender = resultSender;
        this.storageService = storageService;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method parses the form, validates it, stores it in the local cache,
     * and synchronously sends a Kafka task. Returns {@code true} only if the
     * entire flow succeeds (including Kafka send). In case of any failure,
     * an error message is sent to the user.
     * </p>
     *
     * @param chatId Telegram chat ID
     * @param link   Google Form URL
     * @return {@code true} if the request was accepted and the Kafka task was sent
     */
    @Override
    public boolean solveForm(Long chatId, String link) {
        try {
            if (storageService.hasActiveRequest(chatId)) {
            resultSender.sendResult(chatId, "❌ У вас уже есть активный запрос. Пожалуйста, дождитесь его завершения перед отправкой новой формы.");
            return false;
            }
            FormStructure structure = parser.parse(link);
            if (!parser.isValid(structure)) {
                resultSender.sendResult(chatId, "❌ Форма содержит неподдерживаемые типы вопросов или не содержит вопросов.");
                return false;
            }
            String requestId = storageService.createRequest(chatId);
            tasks.put(requestId, new FormTaskInfo(chatId, link, structure));

            // Отправляем в Kafka только requestId (неблокирующе)
            kafkaProducer.sendSolveTask(requestId);

            // Отправляем пользователю подтверждение
            resultSender.sendResult(chatId, "✅ Форма принята в обработку. ID запроса: " + requestId);
            return true;
        } catch (RuntimeException e) {        
            resultSender.sendResult(chatId, "❌ " + e.getMessage());
            return false;
        } catch (Exception e) {
            resultSender.sendResult(chatId, "❌ Ошибка при обработке ссылки: " + e.getMessage());
            log.error("solveForm failed", e);
            return false;
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * This is a stub implementation that always generates a new UUID and sends a RESCORE task.
     * The actual rescore logic will be implemented when the database module is integrated.
     * </p>
     *
     * @param chatId Telegram chat ID
     * @param formId numeric form identifier (from database)
     * @return {@code true} (always, unless an exception occurs)
     */
    @Override
    public boolean rescoreForm(Long chatId, String formId) {
        try {
            String link = storageService.getFormLink(chatId, formId);
            FormStructure structure = parser.parse(link);
            if (!parser.isValid(structure)) {
                resultSender.sendResult(chatId, "❌ Ошибка чтения формы по id:." + formId + ".");
                return false;
            }

            String requestId = storageService.createRequest(chatId);
            tasks.put(requestId, new FormTaskInfo(chatId, "", structure));

            // Отправляем в Kafka только requestId (неблокирующе)
            kafkaProducer.sendSolveTask(requestId);

            // Отправляем пользователю подтверждение
            resultSender.sendResult(chatId, "✅ Форма принята в обработку. ID запроса: " + requestId);
            return true;

        } catch (NoSuchFieldException e) {
            resultSender.sendResult(chatId, "❌ Не удалось получить форму по переданному id.");
            return false;
        } catch (Exception e) {
            resultSender.sendResult(chatId, "❌ Ошибка при обработке формы: " + formId);
            log.error("rescoreForm failed", e);
            return false;
        }
    }


    // === Реализация FormSolvingProvider ===

    /**
     * {@inheritDoc}
     * <p>
     * Retrieves the cached form structure for the given request ID.
     * </p>
     *
     * @param requestId UUID of the request
     * @return Optional with the structure if the request is still in the local cache
     */
    @Override
    public Optional<FormStructure> getFormStructure(String requestId) {
        FormTaskInfo info = tasks.get(requestId);
        return info != null ? Optional.of(info.structure()) : Optional.empty();
    }

    @Override
    public void notifyProgress(String requestId, String message) {
        FormTaskInfo info = tasks.get(requestId);
        if (info == null) {
            log.warn("notifyProgress: no task for requestId={}", requestId);
            return;
        }
        resultSender.sendResult(info.chatId(), message);
    }


    /**
     * {@inheritDoc}
     * <p>
     * Removes the request from the local cache, builds a human‑readable message
     * containing all questions and the generated answers, and sends it to the user
     * via the {@link ResultSender}. Also marks the user's active request as finished.
     * </p>
     *
     * @param requestId UUID of the request
     * @param result    solving result with answers
     */
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
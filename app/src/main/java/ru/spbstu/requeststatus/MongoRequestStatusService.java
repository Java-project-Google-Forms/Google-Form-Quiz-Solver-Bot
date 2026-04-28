package ru.spbstu.requeststatus;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.spbstu.database.repository.RequestStatusRepository;
import ru.spbstu.messagehandler.service.RequestStatusService;

@Component
@Profile("mongo")
public class MongoRequestStatusService implements RequestStatusService {

    private final RequestStatusRepository requestStatusRepository;

    public MongoRequestStatusService(RequestStatusRepository requestStatusRepository) {
        this.requestStatusRepository = requestStatusRepository;
    }

    @Override
    public String getStatus(Long chatId, String requestId) {
        return requestStatusRepository.findByRequestIdAndChatId(requestId, chatId.toString())
                .map(doc -> "📊 Запрос #" + requestId + "\nСтатус: " + formatStatus(doc.getStatus()))
                .switchIfEmpty(Mono.just("❌ Запрос #" + requestId + " не найден."))
                .block();
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "PENDING"    -> "⏳ Ожидает";
            case "PROCESSING" -> "⚙️ В обработке";
            case "COMPLETED"  -> "✅ Завершен";
            case "FAILED"     -> "❌ Неудача";
            default           -> status;
        };
    }
}

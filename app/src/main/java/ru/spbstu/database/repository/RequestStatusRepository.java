package ru.spbstu.database.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.spbstu.database.document.RequestStatusDocument;

@Repository
public interface RequestStatusRepository extends ReactiveMongoRepository<RequestStatusDocument, String> {
    Mono<RequestStatusDocument> findByRequestIdAndChatId(String requestId, String chatId);
}

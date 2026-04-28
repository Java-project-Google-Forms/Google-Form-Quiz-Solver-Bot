package ru.spbstu.database.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.spbstu.database.document.FormDocument;

@Repository
public interface FormRepository extends ReactiveMongoRepository<FormDocument, String> {
    Flux<FormDocument> findByOwnerId(Integer ownerId);
    Mono<FormDocument> findByOwnerIdAndFormId(Integer ownerId, String formId);
}

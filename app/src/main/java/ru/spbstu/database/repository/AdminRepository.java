package ru.spbstu.database.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.spbstu.database.document.AdminDataDocument;

@Repository
public interface AdminRepository extends ReactiveMongoRepository<AdminDataDocument, String> {
    Mono<AdminDataDocument> findByLogin(String login);
}

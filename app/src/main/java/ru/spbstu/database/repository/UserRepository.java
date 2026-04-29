package ru.spbstu.database.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.spbstu.database.document.UserDocument;

@Repository
public interface UserRepository extends ReactiveMongoRepository<UserDocument, String> {
    Mono<UserDocument> findByChatId(String chatId);
}

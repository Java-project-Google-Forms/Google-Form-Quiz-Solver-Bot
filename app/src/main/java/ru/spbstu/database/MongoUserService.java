package ru.spbstu.database;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.spbstu.database.document.UserDocument;
import ru.spbstu.database.repository.UserRepository;
import ru.spbstu.messagehandler.service.UserService;

@Component
@Profile("mongo")
public class MongoUserService implements UserService {

    private final UserRepository userRepository;

    public MongoUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDocument getOrCreateUser(Long chatId, String name) {
    return userRepository.findByChatId(chatId.toString())
            .switchIfEmpty(Mono.defer(() -> {
                UserDocument user = new UserDocument();
                user.setChatId(chatId.toString());
                user.setName(name);
                user.setUserId(Math.abs(chatId.intValue()));
                user.setHasCurrentRequest(false);
                return userRepository.save(user);
            }))
            .block();
}

    @Override
    public boolean hasActiveRequest(Long chatId) {
        UserDocument user = userRepository.findByChatId(chatId.toString()).block();
        return user != null && user.isHasCurrentRequest();
    }

    @Override
    public void setActiveRequest(Long chatId, boolean active) {
        userRepository.findByChatId(chatId.toString())
                .flatMap(user -> {
                    user.setHasCurrentRequest(active);
                    return userRepository.save(user);
                })
                .block();
    }
}

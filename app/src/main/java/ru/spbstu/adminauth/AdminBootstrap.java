package ru.spbstu.adminauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.spbstu.database.document.AdminDataDocument;
import ru.spbstu.database.repository.AdminRepository;

@Component
@Profile("mongo")
public class AdminBootstrap {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final String DEFAULT_LOGIN = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    private final AdminRepository adminRepository;

    public AdminBootstrap(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void seedDefaultAdmin() {
        adminRepository.findByLogin(DEFAULT_LOGIN)
                .switchIfEmpty(Mono.defer(() -> {
                    AdminDataDocument doc = new AdminDataDocument();
                    doc.setLogin(DEFAULT_LOGIN);
                    doc.setPassSHA(AdminAuthService.hashPassword(DEFAULT_PASSWORD));
                    log.info("Seeding default admin '{}'", DEFAULT_LOGIN);
                    return adminRepository.save(doc);
                }))
                .block();
    }
}

package ru.spbstu.requeststatus;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.spbstu.messagehandler.service.RequestStatusService;

@Component
@Profile("stub")
public class StubRequestStatusService implements RequestStatusService {
    @Override
    public String getStatus(Long chatId, String requestId) {
        return "Статус запроса " + requestId + ": выполняется (заглушка)";
    }
}

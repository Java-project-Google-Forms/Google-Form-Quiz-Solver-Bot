package ru.spbstu.history;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.spbstu.database.document.FormDocument;
import ru.spbstu.database.document.HistoryEntryDocument;
import ru.spbstu.database.document.UserDocument;
import ru.spbstu.database.repository.FormRepository;
import ru.spbstu.database.repository.UserRepository;
import ru.spbstu.messagehandler.service.api.HistoryService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Profile("mongo")
public class MongoHistoryService implements HistoryService {

    private final UserRepository userRepository;
    private final FormRepository formRepository;

    public MongoHistoryService(UserRepository userRepository, FormRepository formRepository) {
        this.userRepository = userRepository;
        this.formRepository = formRepository;
    }

    private Mono<UserDocument> getOrCreateUser(Long chatId) {
        return userRepository.findByChatId(chatId.toString())
                .switchIfEmpty(Mono.defer(() -> {
                    UserDocument newUser = new UserDocument();
                    newUser.setChatId(chatId.toString());
                    newUser.setHasCurrentRequest(false);
                    return userRepository.save(newUser);
                }));
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

    @Override
    public String getHistory(Long chatId, String period) {
        return getOrCreateUser(chatId)
                .map(user -> {
                    List<HistoryEntryDocument> history = user.getHistory();
                    if (history == null || history.isEmpty()) {
                        return "📜 История пуста.";
                    }
                    Instant from = switch (period.trim().toLowerCase()) {
    			case "day"   -> Instant.now().minus(1, ChronoUnit.DAYS);
    			case "month" -> Instant.now().minus(30, ChronoUnit.DAYS);
    			case "all"   -> Instant.EPOCH;
    			default      -> Instant.now().minus(7, ChronoUnit.DAYS); // week по умолчанию
		    };
                    List<HistoryEntryDocument> filtered = history.stream()
                            .filter(e -> e.getSolvedDate() != null && e.getSolvedDate().isAfter(from))
                            .toList();
                    if (filtered.isEmpty()) {
                        return "📜 За выбранный период записей нет.";
                    }
                    StringBuilder sb = new StringBuilder("📜 <b>История:</b>\n");
		    for (HistoryEntryDocument e : filtered) {
    			sb.append("• requestId=<code>").append(e.getFormId()).append("</code>")
      			  .append(", статус=").append(formatStatus(e.getStatus()))
      			  .append(", дата=").append(e.getSolvedDate().toString(), 0, 10)
      			  .append("\n");
		    }
                    return sb.toString();
                })
                .block();
    }

    @Override
    public String getMyForms(Long chatId) {
        return getOrCreateUser(chatId)
                .flatMap(user -> {
                    List<String> savedForms = user.getSavedForms();
                    if (savedForms == null || savedForms.isEmpty()) {
                        return Mono.just("📋 У вас нет сохранённых форм.");
                    }

                    return formRepository.findByOwnerId(user.getUserId())
                            .collectList()
                            .map(forms -> {
                                // ФИЛЬТРАЦИЯ: Оставляем только те формы, которые есть в savedForms
                                List<FormDocument> filteredForms = forms.stream()
                                        .filter(f -> savedForms.contains(f.getFormId()))
                                        .collect(Collectors.toList());

                                if (filteredForms.isEmpty()) {
                                    return "📋 У вас нет сохранённых форм.";
                                }

                                StringBuilder sb = new StringBuilder("📋 <b>Ваши формы:</b>\n");
                                for (FormDocument f : filteredForms) {
                                    sb.append("• id=<code>").append(f.getFormId()).append("</code>")
                                    .append(", название=").append(f.getFormName()).append("\n");
                                }
                                return sb.toString();
                            });
                })
                .block();
    }

    @Override
    public String getForm(Long chatId, String formId) {
        return getOrCreateUser(chatId)
                .flatMap(user -> formRepository.findByOwnerIdAndFormId(user.getUserId(), formId))
                .map(form -> {
                    if (!form.isSolved()) {
                        return "❌ Форма не найдена или нет успешных решений.";
                    }
                    StringBuilder sb = new StringBuilder("📄 <b>" + form.getFormName() + "</b>\n");
                    form.getQuestions().forEach(q ->
                        sb.append("• ").append(q.getBody()).append("\n  Ответ: ").append(q.getAnswer()).append("\n")
                    );
                    return sb.toString();
                })
                .switchIfEmpty(Mono.just("❌ Форма не найдена или нет успешных решений."))
                .block();
    }

    @Override
    public String removeForm(Long chatId, String formId) {
        return userRepository.findByChatId(chatId.toString())
            .flatMap(user -> {
                List<String> forms = user.getSavedForms();
                
                // Проверяем наличие и удаляем
                if (forms != null && forms.contains(formId)) {
                    forms.remove(formId); 
                    
                    // ВАЖНО: Мы должны вернуть Mono от сохранения, 
                    // чтобы flatMap дождался записи в БД
                    return userRepository.save(user)
                        .thenReturn("✅ Форма успешно скрыта из вашего списка.");
                } else {
                    return Mono.just("❌ Форма не найдена в вашем списке.");
                }
            })
            .switchIfEmpty(Mono.just("❌ Пользователь не найден, возможно, вы слишком быстро отправили запрос после команды /start или не отправляли команду /start вовсе."))
            .block();
    }

    
}


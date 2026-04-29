# Database Module

## Версии
- `mongodb-driver-reactivestreams 5.6.1`
- `spring-data-mongodb 5.0.0`
- MongoDB server 7.0

## Конфигурация
`MongoConfig.java` — подключение к БД через URI из `application.properties`, регистрация всех реактивных репозиториев.

## Коллекции БД

### `AdminData` → `AdminDataDocument.java`
- поля: `login`, `passSHA`
- репозиторий: `AdminRepository` с методом `findByLogin`

### `Users` → `UserDocument.java`
- поля: `chatId`, `userId`, `name`, `hasCurrentRequest`, `savedForms`, `history`
- вложенный документ `HistoryEntryDocument` — `formId`, `status`, `solvedDate`
- репозиторий: `UserRepository` с методом `findByChatId`

### `Forms` → `FormDocument.java` + `QuestionDocument.java`
- поля: `ownerId`, `formId`, `formName`, `isSolved`, `questions` (type, body, answer)
- репозиторий: `FormRepository` с методами `findByOwnerId`, `findByOwnerIdAndFormId`

### `RequestStatuses` → `RequestStatusDocument.java`
- поля: `requestId`, `chatId`, `status`, `createdAt`
- репозиторий: `RequestStatusRepository` с методом `findByRequestIdAndChatId`

## Сервисы

### `MongoUserService` — реализует `UserService`
- `getOrCreateUser(chatId, name)` — создаёт пользователя при первом обращении к боту
- `hasActiveRequest(chatId)` — проверка лимита в 1 активный запрос
- `setActiveRequest(chatId, active)` — установка флага активного запроса

### `MongoHistoryService` — реализует `HistoryService`
- `getHistory(chatId, period)` — `/history [day|week|month|all]`, без параметра показывает за неделю
- `getMyForms(chatId)` — `/myforms` список сохранённых форм
- `getForm(chatId, formId)` — `/get_form <id>` последние ответы формы
- `removeForm(chatId, formId)` — `/remove_form <id>` удаляет из списка, история не удаляется

### `MongoRequestStatusService` — реализует `RequestStatusService`
- `getStatus(chatId, requestId)` — `/status <requestId>` возвращает статусы PENDING/PROCESSING/COMPLETED/FAILED

### `MongoFormStorageService` — реализует `FormStorageService`
- `createRequest(chatId)` — создаёт запись в `RequestStatuses` со статусом `PENDING`, устанавливает `hasCurrentRequest=true`, возвращает `requestId`
- `saveForm(chatId, form)` — сохраняет `FormDocument`, добавляет в `savedForms` и `history` пользователя
- `updateRequestStatus(chatId, requestId, status)` — обновляет статус запроса

## Как использовать другим модулям

### Для `formsolving` модуля
Заинжектить `FormStorageService`:

```java
// При получении запроса на решение формы:
Integer requestId = formStorageService.createRequest(chatId);

// При начале обработки:
formStorageService.updateRequestStatus(chatId, requestId, "PROCESSING");

// После успешного решения:
FormDocument form = new FormDocument();
form.setFormId(...);
form.setFormName(...);
form.setSolved(true);
form.setQuestions(...); // список QuestionDocument с type, body, answer
formStorageService.saveForm(chatId, form);
formStorageService.updateRequestStatus(chatId, requestId, "COMPLETED");

// При ошибке:
formStorageService.updateRequestStatus(chatId, requestId, "FAILED");
```

### Для `adminauth` модуля
Использовать `AdminRepository`:
```java
adminRepository.findByLogin(login) // поиск администратора
adminRepository.save(adminDoc)     // сохранение нового администратора
```

### Для `listusers` модуля
Использовать `UserRepository`:
```java
userRepository.findAll() // список всех пользователей
```

## Профили запуска
- `stub` — все заглушки, MongoDB не нужна
- `mongo` — реальная MongoDB, запуск: `./gradlew run -Dspring.profiles.active=mongo`

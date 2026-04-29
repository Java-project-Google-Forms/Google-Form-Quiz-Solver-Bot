# 🤖 Google Form Quiz Solver Bot
Telegram‑бот для автоматического решения Google‑форм с помощью LLM (GigaChat).  
Выполнен в рамках курсовой работы.


Документы с подробным описанием требований и архитектуры находятся в каталоге [`docs/`](docs/).


<img width="640" height="640" alt="photo_5411087781812966293_c" src="https://github.com/user-attachments/assets/acfe94b4-e08f-4766-88a6-86039d18a93f" />

---


## 🔗 Ссылки
- **Docker Hub образ**: [docker.io](///TO DO)

- **Telegram бот**: [QuizSolverBot](https://t.me/formsolverbot)

---

## 📋 Технологический стек

- **Язык**: Java 25  
- **Фреймворк**: Spring 7 (WebFlux, Data MongoDB Reactive, Modulith)  
- **База данных**: MongoDB  
- **Брокер сообщений**: Kafka  
- **LLM провайдер**: GigaChat API  
- **Telegram API**: Java Telegram Bot API  
- **Контейнеризация**: Docker, Docker Compose  
- **Сборка**: Gradle (fat‑JAR)  
- **Тестирование**: JUnit, Testcontainers  

---

## 💡 Основные функции

1. **Решение Google‑форм**  
   - Приём ссылки на форму через `/solve`.  
   - Генерация ответов через **GigaChat** (3 попытки, таймаут 30 сек).  
   - Вывод ответов с уровнем уверенности (0–100%).  

2. **История и управление формами**  
   - `/history [day|week|month|all]` – история решений.  
   - `/myforms` – список сохранённых форм.  
   - `/get_form <id>` – просмотр ответов сохранённой формы.  
   - `/remove_form <id>` – удаление формы.  
   - `/rescore <id>` – повторное решение формы.  

3. **Статус запросов**  
   - `/status <requestId>` – проверка состояния обработки (Ожидает, В обработке, Завершен, Неудача).  

4. **Администрирование**  
   - `/healthcheck` – статус сервера + список авторов (без авторизации).  
   - `/auth` (POST) – получить API‑ключ для доступа к `/users`.  
   - `/users` (GET) – список всех пользователей (только для админов). 

---

## ⚙️ Требования к окружению

- **Docker** 20.10+ и **Docker Compose** 2.20+
- **Gradle** 8.10+ (для локальной сборки)
- **Java 25** (если запуск без контейнера)

---

## 🔧 Настройка переменных окружения

Создайте в корне проекта файл **`.env`**:

```env
# Telegram Bot
TELEGRAM_BOT_TOKEN=ваш_токен_от_BotFather
TELEGRAM_BOT_USERNAME=QuizSolverBot 

# GigaChat API
GIGACHAT_API_KEY=ваш_base64_ключ

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# MongoDB
MONGODB_URI=mongodb://mongo:27017
MONGODB_DATABASE=quiz_bot

# JWT для endpoint /auth
JWT_SECRET=случайная_длинная_строка

```

---

## 🚀 Запуск приложения

### Локальный запуск

```bash
git clone https://github.com/Java-project-Google-Forms/Google-Form-Quiz-Solver-Bot.git
cd Google-Form-Quiz-Solver-Bot
```

#### 1. Экспорт переменных окружения (Windows – set, Linux – export)
```bash
set TELEGRAM_BOT_TOKEN=...
```

#### 2. Сборка fat‑JAR
Выполните команду в корне проекта:

```bash
./gradlew :app:shadowJar
```

#### 3. Запуск

```bash
java -jar app/build/libs/app.jar
```

### Запуск через Docker Compose

Убедитесь, что файл .env создан.

#### 1. Сборка и запуск всех сервисов.

```bash
docker compose up --build -d
```

#### 2. Проверка логов.

```bash
docker logs -f app
```


#### 3. Остановка.

```bash
docker compose down
```
---

## 🖼 Пример взаимодействия с ботом

- Отправьте боту ссылку на Google Форму:

```user
/solve https://forms.gle/example
```
Ответ:

```bot
✅ Форма принята в обработку. ID запроса: 550e8400-e29b-41d4-a716-446655440000
```

- Через некоторое время бот пришлёт результат:

```bot
✅ Результат решения формы "Тест по географии":

1. Столица Франции:
   ▶ Париж (уверенность: 100%)

2. Введите вашу фамилию:
   ▶ В этом вопросе запрашиваются личные данные. Пожалуйста, ответьте вручную. (уверенность: 0%)
```

- Проверить статус длительного запроса:

```user
/status 550e8400-e29b-41d4-a716-446655440000
```

```bot
Статус: Завершен
```

---

## 🔌 HTTP эндпоинты (администрирование)

- GET /healthcheck

Пример ответа:

```json
{
  "status": "UP",
  "authors": ["Янковский Э.", "Мелещенко С.", "Гордиенко Ю.", "Волнухина В."]
}
```

- POST /auth – получение API‑ключа

```bash
curl -X POST http://localhost:8080/auth -d "login=admin&password=admin"
```

- GET /users – список пользователей (требуется Bearer‑токен)

```bash
curl -H "Authorization: Bearer <полученный_токен>" http://localhost:8080/users
```

---

## 📁 Структура проекта (основные модули)

- messagehandler – обработка команд Telegram, маршрутизация.

- formsolving – парсинг Google Forms, отправка задач в Kafka.

- llmsolver – интеграция с GigaChat, генерация ответов и уверенности.

- database – работа с MongoDB (сохранение форм, истории).

- adminauth – авторизация администраторов, JWT‑токены.

- requeststatus – управление статусами запросов.


---

## Команда
гр. 5130201/30101
- Янковский Э.
- Мелещенко С.
- Гордиенко Ю.
- Волнухина В.


<img width="1000" height="150" alt="698763d33117f393221d99de00f2b0412399c772685102f76a580d7bebf9791c" src="https://github.com/user-attachments/assets/228811c2-336e-4b0d-ab62-5c6b9e9b9ceb" />


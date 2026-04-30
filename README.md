# 🤖 Google Form Quiz Solver Bot
Telegram‑бот для автоматического решения Google‑форм с помощью LLM (GigaChat).


Документы с подробным описанием требований и архитектуры находятся в каталоге [`docs/`](docs/).


<img width="640" height="640" alt="photo_5411087781812966293_c" src="https://github.com/user-attachments/assets/acfe94b4-e08f-4766-88a6-86039d18a93f" />

---


## 🔗 Ссылки
- **Docker Hub образ**: [docker.io](https://hub.docker.com/r/derikey/google-form-quiz-bot)

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
   -  `http://localhost:8080/healthcheck` – статус сервера + список авторов (без авторизации).
   - `http://localhost:8080/auth?login=…&pass=…` – получить API‑ключ для доступа к `/users` (живёт 15 минут).
   - `http://localhost:8080/users?key=…` – список всех пользователей (только при валидном API‑ключе).

---

## ⚙️ Требования к окружению

- **Docker** 20.10+ и **Docker Compose** 2.20+
- **Java 25** и **Gradle Wrapper** (`gradlew.bat`/`gradlew`) — нужен для пересборки jar.

---

## 🔧 Настройка переменных окружения

Создайте в корне проекта файл **`.env`**:

```env
# Профиль Spring (mongo — реальная БД, stub — заглушки)
SPRING_PROFILES_ACTIVE=mongo

# Telegram Bot
TELEGRAM_BOT_TOKEN=ваш_токен_от_BotFather
TELEGRAM_BOT_USERNAME=QuizSolverBot

# GigaChat API (Base64-ключ из личного кабинета Sber)
GIGACHAT_API_KEY=ваш_base64_ключ

# Kafka (хост — имя сервиса в docker-compose)
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# MongoDB (хост — имя сервиса в docker-compose)
MONGODB_URI=mongodb://mongodb:27017/quiz_bot
MONGODB_DATABASE=quiz_bot

# Секрет для администрирования
JWT_SECRET=случайная_длинная_строка

# Порт, на который пробросится приложение
APP_PORT=8080
```

> Без `SPRING_PROFILES_ACTIVE=mongo` контекст не поднимется: реализации `HistoryService`/`RequestStatusService`/`FormStorageService` активируются именно под этим профилем.

---

## 🚀 Запуск через Docker

Убедитесь, что файл `.env` создан и Docker Desktop запущен.

#### 1. Сборка fat‑jar (gradlew)

```powershell
.\gradlew.bat :app:shadowJar --no-daemon
```

Артефакт окажется в `app/build/libs/app.jar`.

#### 2. Сборка docker‑образа и запуск всех сервисов (app + Kafka + MongoDB)

```powershell
docker compose up --build -d
```

Если в коде ничего не менялось и образ уже свежий — можно просто:

```powershell
docker compose up -d
```

#### 3. Проверка логов

```powershell
docker compose logs -f app
# выйти — Ctrl+C
```


#### 4. Остановка

```powershell
docker compose down            # остановить + удалить контейнеры (Mongo volume сохранится)
docker compose down -v         # ... + снести Mongo volume (чистая БД)
```

---

## 🖥 Локальный запуск

#### 1. Поднимите только инфраструктуру (Kafka + MongoDB)

```powershell
docker compose up -d kafka mongodb
```

#### 2. Установите переменные окружения для приложения

В PowerShell для текущей сессии (хосты — `localhost`, потому что приложение бежит вне docker‑сети):

```powershell
$env:SPRING_PROFILES_ACTIVE = "mongo"
$env:TELEGRAM_BOT_TOKEN     = "ваш_токен"
$env:TELEGRAM_BOT_USERNAME  = "QuizSolverBot"
$env:GIGACHAT_API_KEY       = "ваш_base64_ключ"
$env:JWT_SECRET             = "случайная_длинная_строка"
$env:KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
$env:MONGODB_URI             = "mongodb://localhost:27017/quiz_bot"
```

#### 3. Соберите jar и запустите

```powershell
.\gradlew.bat :app:shadowJar --no-daemon
java -jar app\build\libs\app.jar
```

---

## 🖼 Пример взаимодействия с ботом

- Команда /start

<img width="658" height="804" alt="image" src="https://github.com/user-attachments/assets/1a962343-2950-4913-bdfd-34461953fcbe" />


- Команда /help

<img width="654" height="302" alt="image" src="https://github.com/user-attachments/assets/dc72bae6-0be6-4453-90d4-d7e4b642478d" />


- Команда /history

<img width="662" height="414" alt="image" src="https://github.com/user-attachments/assets/0dab3db8-6c0e-4ad1-a8f4-b2b80186cf94" />

- Команда /myforms

<img width="657" height="407" alt="image" src="https://github.com/user-attachments/assets/80547fce-d164-44dc-9367-43afce38bdde" />

- Команда /solve <link to form>

<img width="626" height="656" alt="image" src="https://github.com/user-attachments/assets/51d515e6-f546-4c7a-9b0b-60ac4110eefd" />

- Команда /get_form <formId>

<img width="651" height="333" alt="image" src="https://github.com/user-attachments/assets/e8532a75-043a-4153-92cf-2a903773b885" />

- Команда /status <requestId>

  <img width="626" height="150" alt="image" src="https://github.com/user-attachments/assets/83debf25-41cc-4257-b537-3577bf30d69b" />

- Команда /rescore <formId>

<img width="628" height="450" alt="image" src="https://github.com/user-attachments/assets/136afdd4-4315-413f-aa8b-f6ca3759aed3" />

- Команда /remove_form <formId>

<img width="624" height="117" alt="image" src="https://github.com/user-attachments/assets/da502189-4273-407c-a71b-18fda25ab7ba" />




## 🔌 HTTP эндпоинты (администрирование)

- http://localhost:8080/healthcheck

<img width="469" height="427" alt="image" src="https://github.com/user-attachments/assets/2fae2200-3938-490a-b3f7-41593b1f79f5" />

- http://localhost:8080/auth?login=...&pass=...

<img width="675" height="86" alt="image" src="https://github.com/user-attachments/assets/24d937c2-e11c-4d5e-8d50-6b41331b196b" />

- http://localhost:8080/users?key=19a09026-9ce4-4b67-ab62-c96571a56beb

<img width="478" height="698" alt="image" src="https://github.com/user-attachments/assets/ff25cec1-fd34-4c40-bedd-c6dd06b0bffc" />

> API‑ключ из `/auth` действителен **15 минут**. По истечении — `/users` вернёт `401`, нужно получить новый ключ.

---

## 📁 Структура проекта (основные модули)

- **messagehandler** – обработка команд Telegram, маршрутизация.
- **formsolving** – парсинг Google Forms, отправка задач в Kafka.
- **llmsolver** – интеграция с GigaChat, генерация ответов и уверенности.
- **database** – работа с MongoDB (сохранение форм, истории).
- **adminauth** – авторизация администраторов, выдача и проверка API‑ключей.
- **listusers** – endpoint `/users` со списком всех пользователей.
- **requeststatus** – управление статусами запросов.
- **history** – история решённых форм.
- **healthcheck** – endpoint `/healthcheck`.


---

## Команда
гр. 5130201/30101
- Янковский Э.
- Мелещенко С.
- Гордиенко Ю.
- Волнухина В.


<img width="1000" height="150" alt="698763d33117f393221d99de00f2b0412399c772685102f76a580d7bebf9791c" src="https://github.com/user-attachments/assets/228811c2-336e-4b0d-ab62-5c6b9e9b9ceb" />

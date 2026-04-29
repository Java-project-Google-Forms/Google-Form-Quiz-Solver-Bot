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

- Команда /start

<img width="658" height="804" alt="image" src="https://github.com/user-attachments/assets/1a962343-2950-4913-bdfd-34461953fcbe" />


- Команда /help

<img width="654" height="302" alt="image" src="https://github.com/user-attachments/assets/dc72bae6-0be6-4453-90d4-d7e4b642478d" />


- Команда /history

<img width="662" height="414" alt="image" src="https://github.com/user-attachments/assets/0dab3db8-6c0e-4ad1-a8f4-b2b80186cf94" />

- Команда /myforms

<img width="657" height="407" alt="image" src="https://github.com/user-attachments/assets/80547fce-d164-44dc-9367-43afce38bdde" />

- Команда /solve <link to form>

<img width="654" height="822" alt="image" src="https://github.com/user-attachments/assets/25b38ce8-2172-4cf2-b539-99a5e0f3c08d" />

- Команда /get_form <formId>

<img width="651" height="333" alt="image" src="https://github.com/user-attachments/assets/e8532a75-043a-4153-92cf-2a903773b885" />

- Команда /status <requestId>

  <img width="626" height="150" alt="image" src="https://github.com/user-attachments/assets/83debf25-41cc-4257-b537-3577bf30d69b" />

- Команда /rescore <formId>

<img width="628" height="450" alt="image" src="https://github.com/user-attachments/assets/136afdd4-4315-413f-aa8b-f6ca3759aed3" />

- Команда /remove_form <formId>






## 🔌 HTTP эндпоинты (администрирование)



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


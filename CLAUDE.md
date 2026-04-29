# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & run

Gradle Kotlin DSL, single subproject `app`, Java **25** toolchain, dependency catalogue in `gradle/libs.versions.toml`.

- Build fat jar: `./gradlew :app:shadowJar` → `app/build/libs/app.jar` (entry point `ru.spbstu.Application`).
- Run locally: `./gradlew :app:run` (uses the `application` plugin's `mainClass = ru.spbstu.Application`).
- Run all tests: `./gradlew :app:test` (JUnit Jupiter; tests under `app/src/test/java`).
- Run a single test: `./gradlew :app:test --tests "ru.spbstu.AppTest"` (or `--tests "...#methodName"`).
- Run the live GigaChat smoke test: `./gradlew :app:runLlmTest` — custom `JavaExec` task defined in `app/build.gradle.kts` that boots a minimal context scanning only `ru.spbstu.llmsolver` and calls the real GigaChat API. Requires `GIGACHAT_API_KEY` to be set.
- Docker: `docker compose up --build` (multi-stage build via `Dockerfile`, runtime depends on a Kafka container). Env vars come from `.env` (template in `.env.example`).

The HTTP server listens on `server.host:server.port` (default `0.0.0.0:8080`); `/healthcheck` returns build/author info. The Telegram bot uses long polling, registered at `ContextRefreshedEvent` time — there is no webhook handler despite the `telegram.webhook.path` property.

## Architecture

This is **Spring 7 (WebFlux + Modulith) without Spring Boot**. `Application.main` constructs an `AnnotationConfigApplicationContext` from `AppConfig`, then hand-builds a Reactor Netty `HttpServer` around a `DispatcherHandler` — there is no auto-configuration, so any new infrastructure (MongoDB driver, additional WebClients, etc.) must be wired as explicit `@Bean`s. `AppConfig` does `@ComponentScan("ru.spbstu")` + `@PropertySource("classpath:application.properties")` + `@EnableWebFlux`.

Code is organised by Spring Modulith convention: each top-level package under `ru.spbstu` is a module (`messagehandler`, `formsolving`, `llmsolver`, `healthcheck`, `history`, `requeststatus`, `adminauth`, `listusers`). Cross-module calls go through interfaces declared in the *consumer* module (e.g. `messagehandler.service.FormSolvingService`, `messagehandler.service.HistoryService`) and implemented in the *producer* module — keep that direction when adding new collaborations.

### End-to-end request flow (Telegram → answer)

1. `QuizTelegramBot.onUpdateReceived` (long polling, fixed pool of 10 threads) hands the `Message` to `MessageHandler`.
2. `MessageHandler` matches on text via a `switch` with pattern matching: explicit `/start`, `/help`, `/myforms`, and `String s when s.startsWith("/solve")` etc., plus `FORM_LINK_REGEX` to auto-detect a bare Google Forms URL and route it as `/solve`.
3. `TelegramCommandRouter` returns a synchronous reply string and delegates side effects to `FormSolvingService` / `HistoryService` / `RequestStatusService`.
4. `FormSolvingServiceImpl.solveForm` (in module `formsolving`) parses the form via `GoogleFormsJsonParser` (Jsoup fetch + regex over `FB_PUBLIC_LOAD_DATA_` + Jackson tree walk), stores the parsed `FormStructure` in an **in-memory `ConcurrentHashMap<requestId, FormTaskInfo>`** keyed by a fresh `UUID`, and publishes `{"requestId":..., "type":"SOLVE"|"RESCORE"}` JSON to Kafka topic `form-solving-requests` via `KafkaProducerService` (Reactor Kafka, fire-and-forget `.subscribe()`).
5. `LlmSolver` (`@KafkaListener` on the same topic, group `llm-solver-group`) pulls the `requestId`, looks the structure back up via `FormSolvingProvider.getFormStructure`, asks the LLM, and calls `FormSolvingProvider.submitResult`. `submitResult` formats the answer, hands it to `ResultSender` (the `TelegramResultSender` implementation in the `messagehandler` module splits messages > 4000 chars), and **removes the entry from the map**.

Two consequences worth knowing:
- The `tasks` map is the only handoff between request and response — it lives in a single JVM and is lost on restart. Anything that needs durability has to replace it.
- `FormSolvingServiceImpl` is referenced both as `FormSolvingService` (inbound, from `messagehandler`) and `FormSolvingProvider` (outbound, from `llmsolver`). The `ResultSender` dependency is `@Lazy` to break the `formsolving ↔ messagehandler` cycle.

### LLM subsystem

`llmsolver` is split into `client` (HTTP — `GigaChatClient` uses Spring `WebClient` over Reactor Netty with bearer-token retry/backoff via `TokenProvider`), `prompt` (`PromptBuilder`), `parser` (`AnswerParser`), and `service` (`LLMQuestionSolver` orchestrates them and returns `Mono<Map<String, AnswerWithConfidence>>`). The `LlmSolver` Kafka listener calls `.block(Duration.ofMinutes(2))` because the listener is synchronous, and on failure submits per-question fallback answers so the requester is never left hanging.

### Profiles & stubs

`spring.profiles.active=stub` is set in `application.properties`. Components annotated `@Profile("stub")` (`StubHistoryService`, `StubRequestStatusService`) are the only implementations of those interfaces today — disabling the profile will fail context startup until real services exist. **`StubLlmSolver` is *not* profile-gated**, so by default both `StubLlmSolver` and `LlmSolver` register `@KafkaListener` on `form-solving-requests` with the same `groupId=llm-solver-group`; only one will receive each message. Add a `@Profile` (or remove the stub) when wiring up real LLM behaviour.

### Configuration & external services

Properties resolve from `application.properties` with `${ENV_VAR:default}` fallback. The defaults committed to the repo include real-looking Telegram bot and GigaChat credentials (also duplicated in `properties.env`, which is *not* gitignored — only `.env` is). Treat any commit touching `application.properties` or `properties.env` as a credential-handling change and prefer relying on `.env` + `docker-compose.yml` env injection.

MongoDB is referenced in `application.properties` and `gradle/libs.versions.toml` (`spring-data-mongodb`, `mongodb-driver-reactivestreams`) but **not yet wired** — the `app/build.gradle.kts` has `// TODO Add MongoDB` and no Mongo beans exist. History/forms/users persistence is currently the in-memory map plus stub services.

Kafka is configured in `formsolving/config/FormsolvingKafkaConfig.java`: Reactor Kafka `KafkaSender` for producing, classic Spring `ConcurrentKafkaListenerContainerFactory` for consuming, with a `NewTopic` bean to auto-create the topic. `missingTopicsFatal=false` so the app boots without a broker, but listeners will be silently idle.

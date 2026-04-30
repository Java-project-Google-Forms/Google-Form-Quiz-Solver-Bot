package ru.spbstu.llmsolver.client;

import reactor.core.publisher.Mono;

import java.util.function.LongConsumer;

/**
 * Абстракция над конкретным LLM-провайдером. Реализована
 * {@link GigaChatClient}, но интерфейс позволяет подменить его на любой
 * другой (например, mock в тестах, OpenAI, локальную модель и т.п.).
 */
public interface LanguageModelClient {

    /**
     * Отправить prompt модели и получить «сырой» текстовый ответ.
     * Реализация сама занимается ретраями и тайм-аутами.
     */
    Mono<String> ask(String prompt);

    /**
     * Расширенная версия с уведомлением о ретраях.
     *
     * @param onRetry callback, вызываемый перед каждой попыткой повторного
     *                запроса с 1-индексным номером попытки. Реализация по
     *                умолчанию игнорирует callback и делегирует к
     *                {@link #ask(String)} — переопределяйте, если ваша
     *                реализация поддерживает ретраи.
     */
    default Mono<String> ask(String prompt, LongConsumer onRetry) {
        return ask(prompt);
    }
}

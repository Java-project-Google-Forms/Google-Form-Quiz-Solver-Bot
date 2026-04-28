package ru.spbstu.llmsolver.prompt;

import org.springframework.stereotype.Component;
import ru.spbstu.llmsolver.service.LLMQuestionSolver.Question;

import java.util.List;

@Component
public class PromptBuilder {

    public String buildPrompt(List<Question> questions) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ты — полезный ассистент, отвечающий на вопросы викторины или формы. Твоя задача — дать точный и полезный ответ на каждый вопрос.\n");
        sb.append("Отвечай строго в формате JSON без лишнего текста:\n");
        sb.append("{\n");
        sb.append("  \"1\": { \"answer\": \"твой ответ\", \"confidence\": число },\n");
        sb.append("  \"2\": { \"answer\": \"твой ответ\", \"confidence\": число }\n");
        sb.append("}\n");
        sb.append("Поле confidence — целое число от 0 до 100, где 100 означает полную уверенность.\n\n");
        
        sb.append("**ПРАВИЛА (соблюдай их точно):**\n");
        sb.append("1. Если вопрос запрашивает **личную, конфиденциальную информацию конкретного человека** (например: ФИО, дата рождения, телефон, email, адрес, паспорт, кредитная карта, девичья фамилия матери) — тогда ответь \"__PERSONAL__\" и поставь confidence = 0.\n");
        sb.append("2. Если вопрос про **текущую дату или время** (например: \"сегодняшняя дата\", \"который час\") — ответь \"UNKNOWN\" и confidence = 0.\n");
        sb.append("3. Во **всех остальных случаях** (факты, математика, география, выбор варианта, даже субъективные предпочтения или оценки) — **дай конкретный ответ**, основанный на твоих знаниях или общепринятых представлениях. НЕ используй \"__PERSONAL__\" для обычных вопросов.\n");
        sb.append("   Примеры правильных ответов:\n");
        sb.append("   - \"Столица Франции?\" → \"Париж\"\n");
        sb.append("   - \"Сколько лап у паука?\" → \"8\"\n");
        sb.append("   - \"Какая оценка наивысшая в российских школах?\" → \"5\"\n");
        sb.append("   - \"Введите дату начала Первой мировой войны\" → \"28 июля 1914 года\"\n");
        sb.append("   - \"Как здороваться 'Добрый вечер'?\" → \"после 18:00\"\n");
        sb.append("4. Никогда не оставляй поле answer пустым. Если ты не знаешь ответ, напиши \"UNKNOWN\" (но это редко).\n");
        sb.append("5. Отвечай на том же языке, на котором задан вопрос (русский → русский, английский → английский).\n\n");
        
        sb.append("Теперь ответь на следующие вопросы строго в JSON формате. НЕ используй '__PERSONAL__' для обычных фактов!\n");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            sb.append(i + 1).append(". ").append(q.text());
            if (q.options() != null && !q.options().isEmpty()) {
                sb.append(" (варианты: ").append(String.join(", ", q.options())).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
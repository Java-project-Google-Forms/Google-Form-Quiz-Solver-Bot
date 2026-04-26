package ru.spbstu.llmsolver.prompt;

import org.springframework.stereotype.Component;
import ru.spbstu.llmsolver.service.LLMQuestionSolver.Question;

import java.util.List;

@Component
public class PromptBuilder {

    public String buildPrompt(List<Question> questions) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an assistant that answers quiz questions. Return answers in JSON format:\n");
        sb.append("{\n");
        sb.append("  \"1\": { \"answer\": \"your answer\", \"confidence\": number },\n");
        sb.append("  \"2\": { \"answer\": \"your answer\", \"confidence\": number }\n");
        sb.append("}\n");
        sb.append("No extra text, only JSON. Confidence is integer 0-100.\n");
        sb.append("RULES:\n");
        sb.append("- For factual questions (e.g., capital of France, 15*3, Red Planet), provide correct answer and high confidence.\n");
        sb.append("- For questions asking for personal information (full name, date of birth, phone, email, address, credit card, mother's maiden name), answer with \"__PERSONAL__\" and confidence 0.\n");
        sb.append("- For questions asking for current date or current time (today's date, what time is it now), answer with \"UNKNOWN\" and confidence 0.\n");
        sb.append("- For subjective opinion questions that ask for the user's personal preference, rating, or satisfaction (e.g., 'choose your favorite season', 'how satisfied are you with online learning', 'rate your experience'), answer with \"__SUBJECTIVE__\" and confidence 0. Such questions require the user's personal opinion, not a generic AI answer.\n");
        sb.append("- However, questions asking for generally accepted best choice (e.g., 'best programming language for beginners', 'best framework for web development') are NOT considered subjective — they can be answered with common knowledge.\n");
        sb.append("- Always provide an answer field, never leave it empty. If none of the above fits, answer \"UNKNOWN\" with confidence 0.\n");
        sb.append("- Provide answers in the language in which questions are provided.\n\n");
        sb.append("Questions:\n");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            sb.append(i + 1).append(". ").append(q.text());
            if (q.options() != null && !q.options().isEmpty()) {
                sb.append(" (options: ").append(String.join(", ", q.options())).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
package ru.spbstu.formsolving.parser;

import lombok.experimental.UtilityClass;
import ru.spbstu.formsolving.model.FormStructure;
import ru.spbstu.formsolving.model.Question;
import ru.spbstu.formsolving.model.QuestionType;

/**
 * Utility class for converting a {@link FormStructure} into a formatted HTML message
 * suitable for sending via Telegram.
 */
@UtilityClass
public class FormatStructure {

    /**
     * Formats the form structure into an HTML message.
     * Includes title, description, question texts, types, options, scale info, etc.
     * Special HTML characters are escaped.
     *
     * @param structure the form structure
     * @return HTML‑formatted string
     */
    public static String formatStructureMessage(FormStructure structure) {
        StringBuilder sb = new StringBuilder();
        sb.append("📄 <b>").append(escapeHtml(structure.getTitle())).append("</b>\n");
        if (structure.getDescription() != null && !structure.getDescription().isEmpty()) {
            sb.append("<i>").append(escapeHtml(structure.getDescription())).append("</i>\n");
        }
        sb.append("\n");

        int idx = 1;
        for (Question q : structure.getQuestions()) {
            sb.append(idx++).append(". ").append(escapeHtml(q.getTitle()));
            if (q.isRequired()) sb.append(" ⚠️");
            sb.append(" [").append(getQuestionTypeName(q.getType())).append("]");
            sb.append("\n");

            // Описание вопроса
            if (q.getDescription() != null && !q.getDescription().isEmpty()) {
                sb.append("   📌 ").append(escapeHtml(q.getDescription())).append("\n");
            }

            // Варианты ответов
            if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                sb.append("   📌 Варианты: ");
                sb.append(String.join(", ",
                        q.getOptions().stream().map(FormatStructure::escapeHtml).toList()));
                sb.append("\n");
                if (q.isShuffle()) sb.append("   🔀 Порядок перемешивается\n");
            }

            // Шкала
            if (q.getScale() != null) {
                Question.ScaleInfo s = q.getScale();
                sb.append("   📊 Шкала: ").append(s.getLow()).append(" – ").append(s.getHigh());
                if (s.getLowLabel() != null || s.getHighLabel() != null) {
                    sb.append(" (");
                    if (s.getLowLabel() != null) sb.append(s.getLowLabel()).append(" – ");
                    if (s.getHighLabel() != null) sb.append(s.getHighLabel());
                    sb.append(")");
                }
                sb.append("\n");
            }


            // Дата
            if (q.getDate() != null) {
                Question.DateInfo d = q.getDate();
                sb.append("   📅 Дата");
                if (d.isIncludeTime()) sb.append(" + время");
                if (d.isIncludeYear()) sb.append(" + год");
                sb.append("\n");
            }

            // Время
            if (q.getTime() != null) {
                sb.append("   ⏰ Время");
                if (q.getTime().isDuration()) sb.append(" (длительность)");
                sb.append("\n");
            }

            sb.append("\n");
        }
        return sb.toString();
    }

    private static String getQuestionTypeName(QuestionType type) {
        return switch (type) {
            case TEXT -> "Короткий текст";
            case PARAGRAPH -> "Абзац";
            case DATE -> "Дата";
            case TIME -> "Время";
            case LINEAR_SCALE -> "Линейная шкала";
            case DROP_DOWN -> "Выпадающий список";
            case MULTIPLE_CHOICE -> "Одиночный выбор";
            case CHECKBOX -> "Множественный выбор";
            default -> "Неизвестный тип";
        };
    }


    /**
     * Escapes XML/HTML special characters: {@code &, <, >, "} to their entity equivalents.
     *
     * @param text input string (may be null)
     * @return escaped string, or empty string if input was null
     */
    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}

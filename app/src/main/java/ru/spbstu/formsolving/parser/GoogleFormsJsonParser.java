package ru.spbstu.formsolving.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import ru.spbstu.formsolving.entity.FormStructure;
import ru.spbstu.formsolving.entity.QuestionType;
import ru.spbstu.formsolving.entity.Question;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GoogleFormsJsonParser {

    private static final Pattern FB_PUBLIC_LOAD_DATA_PATTERN =
            Pattern.compile("var\\s+FB_PUBLIC_LOAD_DATA_\\s*=\\s*(\\[.*?]);\\s*</script>", Pattern.DOTALL);
    private final ObjectMapper objectMapper;

    public GoogleFormsJsonParser() {
        this.objectMapper = new ObjectMapper();
    }

    public FormStructure parse(String formUrl) throws IOException {
        Document doc = Jsoup.connect(formUrl).userAgent("Mozilla/5.0").get();
        String html = doc.html();
        String jsonString = extractJson(html);
        JsonNode root = objectMapper.readTree(jsonString);
        String title = doc.select("div[role=heading]").first() != null
                ? Objects.requireNonNull(doc.select("div[role=heading]").first()).text()
                : "Без названия";
        return parseFormStructure(root, title);
    }

    private String extractJson(String html) {
        Matcher matcher = FB_PUBLIC_LOAD_DATA_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("FB_PUBLIC_LOAD_DATA_ not found. Form may not be public.");
    }

    private FormStructure parseFormStructure(JsonNode root, String title) {
        JsonNode questionsArray = root.path(1).path(1);
        if (questionsArray.isMissingNode() || !questionsArray.isArray()) {
            throw new RuntimeException("Invalid form structure");
        }
        String description = root.path(1).path(0).asText();
        List<Question> questions = new ArrayList<>();
        for (JsonNode qNode : questionsArray) {
            parseQuestion(qNode).ifPresent(questions::add);
        }

        return new FormStructure(title, description, questions);
    }

    private Optional<Question> parseQuestion(JsonNode qNode) {
        int typeCode = qNode.path(3).asInt(-1);
        if (typeCode == -1) return Optional.empty();

        Question q = new Question();
        q.setTitle(qNode.path(1).asText());
        q.setDescription(qNode.path(2).asText());
        q.setType(mapTypeCode(typeCode));
        q.setRequired(extractRequired(qNode));

        JsonNode entryData = qNode.path(4).path(0);
        if (!entryData.isMissingNode()) {
            q.setId(entryData.path(0).asText());
            q.setShuffle(entryData.path(8).asInt(0) == 1);
        }

        switch (typeCode) {
            case 2: case 3: case 4: // MULTIPLE_CHOICE, DROPDOWN, CHECKBOXES
                q.setOptions(extractOptions(entryData.path(1)));
                break;
            case 5: // LINEAR_SCALE
                q.setScale(extractScaleInfo(entryData));
                break;
            case 7: // GRID
                q.setGrid(extractGridInfo(qNode));
                break;
            case 9: // DATE
                q.setDate(extractDateInfo(entryData));
                break;
            case 10: // TIME
                q.setTime(extractTimeInfo(entryData));
                break;
            default:
                break;
        }
        return Optional.of(q);
    }

    private boolean extractRequired(JsonNode qNode) {
        JsonNode entryData = qNode.path(4).path(0);
        if (!entryData.isMissingNode()) {
            return entryData.path(2).asInt(0) == 1;
        }
        return false;
    }

    private List<String> extractOptions(JsonNode optionsRaw) {
        List<String> options = new ArrayList<>();
        if (optionsRaw.isArray()) {
            for (JsonNode opt : optionsRaw) {
                if (opt.isArray() && !opt.isEmpty()) {
                    String text = opt.get(0).asText();
                    if (!text.isEmpty()) options.add(text);
                }
            }
        }
        return options;
    }

    private Question.ScaleInfo extractScaleInfo(JsonNode entryData) {
        // Парсинг шкалы: entryData[1] – значения, entryData[3] – метки (low_label, high_label)
        Question.ScaleInfo scale = new Question.ScaleInfo();
        JsonNode valuesRaw = entryData.path(1);
        List<String> values = new ArrayList<>();
        if (valuesRaw.isArray()) {
            for (JsonNode v : valuesRaw) {
                if (v.isArray() && !v.isEmpty()) {
                    values.add(v.get(0).asText());
                }
            }
        }
        if (!values.isEmpty()) {
            scale.setLow(values.getFirst());
            scale.setHigh(values.getLast());
            scale.setValues(values);
        }

        JsonNode labelsRaw = entryData.path(3);
        if (labelsRaw.isArray() && labelsRaw.size() >= 2) {
            scale.setLowLabel(labelsRaw.get(0).asText());
            scale.setHighLabel(labelsRaw.get(1).asText());
        }
        return scale;
    }

    private Question.GridInfo extractGridInfo(JsonNode qNode) {
        Question.GridInfo grid = new Question.GridInfo();
        JsonNode rowsData = qNode.path(4);
        if (!rowsData.isArray()) return grid;

        List<String> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        List<String> entryIds = new ArrayList<>();
        Integer gridSubtype = null;

        for (JsonNode row : rowsData) {
            if (!row.isArray()) continue;
            String entryId = row.path(0).asText(null);
            if (entryId != null) entryIds.add(entryId);
            String rowText = row.path(3).path(0).asText("");
            rows.add(rowText);

            // Извлекаем столбцы из первой строки
            if (columns.isEmpty()) {
                JsonNode colsRaw = row.path(1);
                if (colsRaw.isArray()) {
                    for (JsonNode col : colsRaw) {
                        if (col.isArray() && !col.isEmpty()) {
                            columns.add(col.get(0).asText());
                        }
                    }
                }
            }

            // Определяем подтип сетки по последнему элементу строки
            JsonNode last = row.path(row.size() - 1);
            if (last.isArray() && last.size() == 1 && (last.get(0).isInt() || last.get(0).isLong())) {
                gridSubtype = last.get(0).asInt();
            }
        }

        grid.setRows(rows);
        grid.setColumns(columns);
        grid.setEntryIds(entryIds);
        if (gridSubtype != null) {
            if (gridSubtype == 0) grid.setType("MULTIPLE_CHOICE_GRID");
            else if (gridSubtype == 1) grid.setType("CHECKBOX_GRID");
            else grid.setType("UNKNOWN_GRID");
        }
        return grid;
    }

    private Question.DateInfo extractDateInfo(JsonNode entryData) {
        Question.DateInfo date = new Question.DateInfo();
        JsonNode flags = entryData.path(7);
        if (flags.isArray() && flags.size() >= 2) {
            date.setIncludeTime(flags.get(0).asInt(0) == 1);
            date.setIncludeYear(flags.get(1).asInt(0) == 1);
        }
        return date;
    }

    private Question.TimeInfo extractTimeInfo(JsonNode entryData) {
        Question.TimeInfo time = new Question.TimeInfo();
        JsonNode flags = entryData.path(6);
        if (flags.isArray() && !flags.isEmpty()) {
            time.setDuration(flags.get(0).asInt(0) == 1);
        }
        return time;
    }

    private QuestionType mapTypeCode(int code) {
        switch (code) {
            case 0: return QuestionType.TEXT;
            case 1: return QuestionType.PARAGRAPH;
            case 2: return QuestionType.MULTIPLE_CHOICE;
            case 3: return QuestionType.DROP_DOWN;
            case 4: return QuestionType.CHECKBOX;
            case 5: return QuestionType.LINEAR_SCALE;
            case 9: return QuestionType.DATE;
            case 10: return QuestionType.TIME;
            default: return QuestionType.UNSUPPORTED;
        }
    }

    public boolean isValid(FormStructure structure) {
        if (structure.getQuestions() == null && structure.getQuestions().isEmpty()) return false;
        return structure.getQuestions().stream()
                .anyMatch(q -> q.getType() != QuestionType.UNSUPPORTED);
    }
}
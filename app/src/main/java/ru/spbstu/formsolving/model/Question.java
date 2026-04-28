package ru.spbstu.formsolving.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a single question in a Google Form.
 * Supports various question types: text, multiple choice, checkbox, grid, scale, date, time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private String id;               // entry_id или UUID
    private String title;            // текст вопроса
    private String description;      // текст под вопросом (описание)
    private QuestionType type;
    private boolean required;
    private List<String> options;    // варианты для radio/checkbox/dropdown
    private boolean shuffle;         // перемешивание вариантов
    private ScaleInfo scale;         // для LINEAR_SCALE
    private GridInfo grid;
    private DateInfo date;           // для DATE
    private TimeInfo time;           // для TIME

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScaleInfo {
        private String low;
        private String high;
        private String lowLabel;
        private String highLabel;
        private List<String> values;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateInfo {
        private boolean includeTime;
        private boolean includeYear;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeInfo { private boolean isDuration; }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GridInfo {
        private String type;
        private List<String> rows = new ArrayList<>();
        private List<String> columns = new ArrayList<>();
        private List<String> entryIds = new ArrayList<>();
    }
}


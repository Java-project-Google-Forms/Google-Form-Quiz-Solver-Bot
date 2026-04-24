package ru.spbstu.formsolving.entity;

import java.util.ArrayList;
import java.util.List;


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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public ScaleInfo getScale() {
        return scale;
    }

    public void setScale(ScaleInfo scale) {
        this.scale = scale;
    }

    public DateInfo getDate() {
        return date;
    }

    public void setDate(DateInfo date) {
        this.date = date;
    }

    public TimeInfo getTime() {
        return time;
    }

    public void setTime(TimeInfo time) {
        this.time = time;
    }

    public GridInfo getGrid() {
        return grid;
    }

    public void setGrid(GridInfo grid) {
        this.grid = grid;
    }


    public static class ScaleInfo {
        private String low;
        private String high;
        private String lowLabel;
        private String highLabel;
        private List<String> values;

        public String getLow() {
            return low;
        }

        public void setLow(String low) {
            this.low = low;
        }

        public String getHigh() {
            return high;
        }

        public void setHigh(String high) {
            this.high = high;
        }

        public String getLowLabel() {
            return lowLabel;
        }

        public void setLowLabel(String lowLabel) {
            this.lowLabel = lowLabel;
        }

        public String getHighLabel() {
            return highLabel;
        }

        public void setHighLabel(String highLabel) {
            this.highLabel = highLabel;
        }

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }
    }

    public static class DateInfo {
        private boolean includeTime;
        private boolean includeYear;

        public boolean isIncludeTime() {
            return includeTime;
        }

        public void setIncludeTime(boolean includeTime) {
            this.includeTime = includeTime;
        }

        public boolean isIncludeYear() {
            return includeYear;
        }

        public void setIncludeYear(boolean includeYear) {
            this.includeYear = includeYear;
        }
    }

    public static class TimeInfo {
        private boolean isDuration;

        public boolean isDuration() {
            return isDuration;
        }

        public void setDuration(boolean duration) {
            isDuration = duration;
        }
    }

    public static class GridInfo {
        private String type;
        private List<String> rows = new ArrayList<>();
        private List<String> columns = new ArrayList<>();
        private List<String> entryIds = new ArrayList<>();
        // геттеры/сеттеры
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public List<String> getRows() { return rows; }
        public void setRows(List<String> rows) { this.rows = rows; }
        public List<String> getColumns() { return columns; }
        public void setColumns(List<String> columns) { this.columns = columns; }
        public List<String> getEntryIds() { return entryIds; }
        public void setEntryIds(List<String> entryIds) { this.entryIds = entryIds; }
    }
}


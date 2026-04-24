package ru.spbstu.formsolving.entity;

import java.util.List;

public class FormStructure  {
    String title;
    String description;
    List<Question> questions;


    public FormStructure(String title, String description, List<Question> questions) {
        this.title = title;
        this.description = description;
        this.questions = questions;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
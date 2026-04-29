package ru.spbstu.formsolving.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


/**
 * Represents the parsed structure of a Google Form.
 * Contains metadata, title, description, and the list of questions.
 */
@Data
@AllArgsConstructor
public class FormStructure  {
    private String title;
    private String description;
    private List<Question> questions;
}
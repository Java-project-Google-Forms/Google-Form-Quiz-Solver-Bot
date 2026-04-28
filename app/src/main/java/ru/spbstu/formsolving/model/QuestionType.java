package ru.spbstu.formsolving.model;


/**
 * Enumeration of supported Google Form question types.
 * UNSUPPORTED is used for types that cannot be processed (e.g., file upload).
 */
public enum QuestionType {
    TEXT, PARAGRAPH, DATE, TIME, LINEAR_SCALE, DROP_DOWN,
    MULTIPLE_CHOICE, CHECKBOX, UNSUPPORTED
}
package ru.spbstu.formsolving.model;


/**
 * Enumeration of supported Google Form question types.
 * UNSUPPORTED is used for types that cannot be processed (e.g., file upload).
 */
public enum QuestionType {
    TEXT, PARAGRAPH, MULTIPLE_CHOICE, DROP_DOWN, CHECKBOX, LINEAR_SCALE, GRID, DATE,
    TIME, IMAGE, VIDEO, FILE_UPLOAD, UNSUPPORTED
}
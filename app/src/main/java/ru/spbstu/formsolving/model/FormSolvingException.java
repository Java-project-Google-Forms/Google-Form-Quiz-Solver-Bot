package ru.spbstu.formsolving.model;

/**
 * Runtime exception thrown when an unrecoverable error occurs during form solving.
 */
public class FormSolvingException extends RuntimeException {
    public FormSolvingException(String message) { super(message); }
    public FormSolvingException(String message, Throwable cause) { super(message, cause); }
}
package ru.spbstu.formsolving.entity;

public class FormSolvingException extends RuntimeException {
    public FormSolvingException(String message) { super(message); }
    public FormSolvingException(String message, Throwable cause) { super(message, cause); }
}
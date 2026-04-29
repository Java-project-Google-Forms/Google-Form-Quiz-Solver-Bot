package ru.spbstu.formsolving.model;


/**
 * Internal record that holds metadata of a processing request.
 * Stored in memory until the result is delivered.
 */
public record FormTaskInfo(Long chatId, String formUrl, FormStructure structure) { }
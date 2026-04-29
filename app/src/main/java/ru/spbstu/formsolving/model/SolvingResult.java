package ru.spbstu.formsolving.model;

import java.util.Map;

/**
 * Contains the generated answers for a form solving request.
 * @param answers map from question ID to answer text.
 */
public record SolvingResult(Map<String, String> answers) {}
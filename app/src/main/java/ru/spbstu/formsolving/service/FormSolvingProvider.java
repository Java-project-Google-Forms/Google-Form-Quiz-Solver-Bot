package ru.spbstu.formsolving.service;

import ru.spbstu.formsolving.entity.FormStructure;
import ru.spbstu.formsolving.entity.SolvingResult;

import java.util.Optional;

public interface FormSolvingProvider {
    Optional<FormStructure> getFormStructure(String requestId);
    void submitResult(String requestId, SolvingResult result);
}
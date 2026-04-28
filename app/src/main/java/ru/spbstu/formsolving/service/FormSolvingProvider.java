package ru.spbstu.formsolving.service;

import ru.spbstu.formsolving.model.FormStructure;
import ru.spbstu.formsolving.model.SolvingResult;

import java.util.Optional;


/**
 * Internal contract between the formsolving module and the LLM solver.
 * Allows the solver to retrieve the form structure for a request and to submit the result.
 */
public interface FormSolvingProvider {

    /**
     * Retrieves the cached form structure for the given request ID.
     *
     * @param requestId UUID of the request
     * @return Optional containing the structure if the request is known
     */
    Optional<FormStructure> getFormStructure(String requestId);

    /**
     * Submits the solving result for the request and triggers delivery to the user.
     *
     * @param requestId UUID of the request
     * @param result    generated answers
     */
    void submitResult(String requestId, SolvingResult result);
}
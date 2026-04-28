package ru.spbstu.database.document;

import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;

public class HistoryEntryDocument {

    @Field("formId")
    private String formId;

    @Field("status")
    private String status;

    @Field("solvedDate")
    private Instant solvedDate;

    public String getFormId() { return formId; }
    public void setFormId(String formId) { this.formId = formId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getSolvedDate() { return solvedDate; }
    public void setSolvedDate(Instant solvedDate) { this.solvedDate = solvedDate; }
}

package ru.spbstu.database.document;

import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;

public class HistoryEntryDocument {

    @Field("formId")
    private Integer formId;

    @Field("status")
    private String status;

    @Field("solvedDate")
    private Instant solvedDate;

    public Integer getFormId() { return formId; }
    public void setFormId(Integer formId) { this.formId = formId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getSolvedDate() { return solvedDate; }
    public void setSolvedDate(Instant solvedDate) { this.solvedDate = solvedDate; }
}

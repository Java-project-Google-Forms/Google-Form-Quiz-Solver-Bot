package ru.spbstu.database.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;

@Setter
@Getter
public class HistoryEntryDocument {

    @Field("formId")
    private String formId;

    @Field("status")
    private String status;

    @Field("solvedDate")
    private Instant solvedDate;

}

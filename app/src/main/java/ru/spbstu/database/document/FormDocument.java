package ru.spbstu.database.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Document(collection = "Forms")
public class FormDocument {

    @Id
    private String id;

    @Field("ownerId")
    private Integer ownerId;

    @Field("formId")
    private String formId;

    @Field("formLink")
    private String formLink;

    @Field("formName")
    private String formName;

    @Field("isSolved")
    private boolean isSolved;

    @Field("questions")
    private List<QuestionDocument> questions = new ArrayList<>();

}

package ru.spbstu.database.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "Forms")
public class FormDocument {

    @Id
    private String id;

    @Field("ownerId")
    private Integer ownerId;

    @Field("formId")
    private Integer formId;

    @Field("formName")
    private String formName;

    @Field("isSolved")
    private boolean isSolved;

    @Field("questions")
    private List<QuestionDocument> questions = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }

    public Integer getFormId() { return formId; }
    public void setFormId(Integer formId) { this.formId = formId; }

    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }

    public boolean isSolved() { return isSolved; }
    public void setSolved(boolean solved) { isSolved = solved; }

    public List<QuestionDocument> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDocument> questions) { this.questions = questions; }
}

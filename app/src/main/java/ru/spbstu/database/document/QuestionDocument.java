package ru.spbstu.database.document;

import org.springframework.data.mongodb.core.mapping.Field;

public class QuestionDocument {

    @Field("type")
    private String type;

    @Field("body")
    private String body;

    @Field("answer")
    private Object answer;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Object getAnswer() { return answer; }
    public void setAnswer(Object answer) { this.answer = answer; }
}

package ru.spbstu.database.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Setter
@Getter
public class QuestionDocument {

    @Field("type")
    private String type;

    @Field("body")
    private String body;

    @Field("answer")
    private Object answer;

}

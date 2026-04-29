package ru.spbstu.database.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Setter
@Getter
@Document(collection = "AdminData")
public class AdminDataDocument {

    @Id
    private String id;

    @Field("login")
    private String login;

    @Field("passSHA")
    private String passSHA;

}

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
@Document(collection = "Users")
public class UserDocument {

    @Id
    private String id;

    @Field("chatId")
    private String chatId;

    @Field("userId")
    private Integer userId;

    @Field("name")
    private String name;

    @Field("hasCurrentRequest")
    private boolean hasCurrentRequest;

    @Field("savedForms")
    private List<String> savedForms = new ArrayList<>();

    @Field("history")
    private List<HistoryEntryDocument> history = new ArrayList<>();

}

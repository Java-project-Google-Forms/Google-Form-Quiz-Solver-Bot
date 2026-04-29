package ru.spbstu.database.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;

@Setter
@Getter
@Document(collection = "RequestStatuses")
public class RequestStatusDocument {

    @Id
    private String id;

    @Field("requestId")
    private String requestId;

    @Field("chatId")
    private String chatId;

    @Field("status")
    private String status;

    @Field("createdAt")
    private Instant createdAt;

}

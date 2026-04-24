package ru.spbstu.database.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;

@Document(collection = "RequestStatuses")
public class RequestStatusDocument {

    @Id
    private String id;

    @Field("requestId")
    private Integer requestId;

    @Field("chatId")
    private String chatId;

    @Field("status")
    private String status;

    @Field("createdAt")
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

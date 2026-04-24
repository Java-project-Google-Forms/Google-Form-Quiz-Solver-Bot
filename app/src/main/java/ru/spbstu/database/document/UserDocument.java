package ru.spbstu.database.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.ArrayList;
import java.util.List;

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
    private List<Integer> savedForms = new ArrayList<>();

    @Field("history")
    private List<HistoryEntryDocument> history = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isHasCurrentRequest() { return hasCurrentRequest; }
    public void setHasCurrentRequest(boolean hasCurrentRequest) { this.hasCurrentRequest = hasCurrentRequest; }

    public List<Integer> getSavedForms() { return savedForms; }
    public void setSavedForms(List<Integer> savedForms) { this.savedForms = savedForms; }

    public List<HistoryEntryDocument> getHistory() { return history; }
    public void setHistory(List<HistoryEntryDocument> history) { this.history = history; }
}

package ru.spbstu.database.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "AdminData")
public class AdminDataDocument {

    @Id
    private String id;

    @Field("login")
    private String login;

    @Field("passSHA")
    private String passSHA;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassSHA() { return passSHA; }
    public void setPassSHA(String passSHA) { this.passSHA = passSHA; }
}

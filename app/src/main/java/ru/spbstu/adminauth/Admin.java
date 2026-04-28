package ru.spbstu.adminauth;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "admins")
public class Admin {
    @Id
    private String id;
    private String login;
    private String passSHA;
    
    public Admin() {}
    
    public Admin(String login, String passSHA) {
        this.login = login;
        this.passSHA = passSHA;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassSHA() { return passSHA; }
    public void setPassSHA(String passSHA) { this.passSHA = passSHA; }
}
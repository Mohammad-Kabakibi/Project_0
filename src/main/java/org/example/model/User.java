package org.example.model;

import java.sql.Date;

public class User {
    private int id;
    private String name;
    private String password;
    private Date member_since;

    public User(){

    }

    public User(int id, String name, String password, Date member_since) {
        this.id = id;
        this.name = name;
        this.member_since = member_since;
        this.password = password;

    }

    public User(int id, String name, Date member_since) {
        this.id = id;
        this.name = name;
        this.member_since = member_since;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getMember_since() {
        return member_since;
    }

    public void setMember_since(Date member_since) {
        this.member_since = member_since;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

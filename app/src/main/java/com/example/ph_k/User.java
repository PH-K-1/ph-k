package com.example.ph_k;

public class User {
    private String username;
    private String password;

    public User(String username, String email, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

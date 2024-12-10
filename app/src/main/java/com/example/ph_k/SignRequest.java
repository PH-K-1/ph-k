package com.example.ph_k;

public class SignRequest {
    private String email;
    private String password;
    private String name;

    // 기본 생성자
    public SignRequest() {}

    // 생성자
    public SignRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    // Getter 및 Setter 메소드
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

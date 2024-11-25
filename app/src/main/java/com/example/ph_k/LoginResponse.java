package com.example.ph_k;

public class LoginResponse {
    private String message;
    private String token;  // 서버에서 반환되는 token 필드

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

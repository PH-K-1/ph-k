package com.example.ph_k;

public class LoginResponse {
    private String message;
    private String token;  // 서버에서 반환되는 token 필드
    private String userId;  // 추가된 user_id 필드
    private String username; // 추가된 username 필드

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

    // 새로운 getter와 setter 추가
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // 새로운 username 필드에 대한 getter와 setter 추가
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

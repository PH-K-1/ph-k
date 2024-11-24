package com.example.ph_k;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/register") // 회원가입 API
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);

    @POST("/login") // 로그인 API
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);
}

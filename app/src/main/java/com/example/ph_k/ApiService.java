package com.example.ph_k;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);

    @POST("/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);
}


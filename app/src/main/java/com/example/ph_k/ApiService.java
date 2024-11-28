package com.example.ph_k;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("signup")
    Call<SignupResponse> signupUser(@Body SignRequest signupRequest);
}

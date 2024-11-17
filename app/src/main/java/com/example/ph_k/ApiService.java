package com.example.ph_k;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/register")
    Call<String> registerUser(@Body User user);

    @POST("/login")
    Call<String> loginUser(@Body User user);
}

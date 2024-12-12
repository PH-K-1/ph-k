package com.example.ph_k;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("signup")
    Call<SignupResponse> signupUser(@Body SignRequest signupRequest);

    // 게시글 삭제 API 추가
    @DELETE("posts/{itemId}")
    // {itemId} 경로 파라미터로 게시글 ID를 받음
    Call<Void> deletePost(@Path("itemId") String itemId);

    // 게시글 수정 API 추가
    @PUT("posts/{itemId}")
    Call<PostData> updatePost(@Path("itemId") int itemId, @Body PostData postData);  // 수정된 postData 전달

}

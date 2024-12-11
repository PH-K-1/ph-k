package com.example.ph_k;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    // 로그인
    @POST("login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    // 회원가입
    @POST("signup")
    Call<SignupResponse> signupUser(@Body SignRequest signupRequest);

    // 게시글 삭제
    @DELETE("posts/{itemId}")
    Call<Void> deletePost(@Path("itemId") String itemId);

    // 게시글 수정
    @PUT("posts/{itemId}")
    Call<PostData> updatePost(@Path("itemId") int itemId, @Body PostData postData);

    // 채팅방 생성 API

    @POST("chatroom/create")
    Call<CreateChatRoomResponse> createChatRoom(@Body CreateChatRoomRequest request);


}


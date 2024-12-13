package com.example.ph_k;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = BuildConfig.BASE_URL;  // 실제 서버 URL로 변경

    // Retrofit 인스턴스를 반환하는 메서드
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // getInstance() 메서드 추가
    public static Retrofit getInstance() {
        return getRetrofitInstance();  // 실제로는 getRetrofitInstance() 메서드를 호출
    }

    // ApiService 반환하는 메서드
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);  // Retrofit 인스턴스를 사용하여 ApiService 생성
    }
}
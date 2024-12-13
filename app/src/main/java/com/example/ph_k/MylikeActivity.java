package com.example.ph_k;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MylikeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> itemList;
    private static final String URL = BuildConfig.BASE_URL+"/get_items";

    // 로그인된 user_id 저장할 변수
    private String loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_like);

        // SharedPreferences에서 로그인된 user_id 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        loggedInUserId = sharedPreferences.getString("username", null); // "username" 키로 로그인된 사용자 ID 가져오기

        // Toolbar 설정 (상단 뒤로가기 버튼 포함)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // 뒤로가기 버튼 활성화
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();  // 뒤로가기 동작
            }
        });

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // DividerItemDecoration 추가
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        itemList = new ArrayList<>();
        // ItemAdapter에 로그인된 user_id를 전달
        adapter = new ItemAdapter(this, itemList, loggedInUserId);
        recyclerView.setAdapter(adapter);

        fetchItems();  // 서버에서 게시글 데이터를 가져옵니다.
    }


    private void fetchItems() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // URL에 user_id를 쿼리 파라미터로 추가
        String urlWithUserId = URL + "?user_id=" + loggedInUserId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlWithUserId, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MylikeActivity", "Response: " + response.toString());
                        try {
                            JSONArray items = response.getJSONArray("items");
                            itemList.clear();  // 데이터 갱신 시 기존 아이템을 제거

                            // 서버에서 받은 게시글 목록을 로그인한 사용자가 좋아요를 눌렀는지 확인
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);

                                // 좋아요 상태 확인
                                boolean isLiked = item.getBoolean("isLiked");

                                // 좋아요를 누른 게시글만 필터링
                                if (isLiked) {
                                    String itemUserId = item.getString("user_id"); // 게시글의 user_id

                                    JSONArray imageUrls = item.getJSONArray("image_urls");
                                    List<String> images = new ArrayList<>();
                                    for (int j = 0; j < imageUrls.length(); j++) {
                                        images.add(imageUrls.getString(j)); // 이미지 URL 리스트에 추가
                                    }

                                    String deadline = item.getString("deadline");  // 데드라인 추가

                                    Item newItem = new Item(
                                            item.getInt("id"),
                                            item.getString("title"),
                                            item.getString("description"),
                                            item.getString("price"),
                                            images, // 여러 이미지 URL을 리스트로 전달
                                            itemUserId, // user_id를 String으로 처리
                                            deadline, // 데드라인을 아이템에 추가
                                            isLiked // 좋아요 상태 추가
                                    );
                                    itemList.add(newItem); // 새로운 아이템 추가
                                }
                            }

                            // 아이템 목록이 갱신되었음을 RecyclerView에 알림
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e("MylikeActivity", "JSON Parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MylikeActivity", "Volley error: " + error.getMessage());
                    }
                });

        queue.add(request);  // 네트워크 요청 큐에 추가
    }




}
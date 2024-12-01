package com.example.ph_k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MylistActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> itemList;
    private static final String URL = "http://192.168.55.231:7310/get_items";

    // 로그인된 user_id 저장할 변수
    private String loggedInUserId;

    // 하단 네비게이션 관련 변수
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        // SharedPreferences에서 로그인된 user_id 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        loggedInUserId = sharedPreferences.getString("username", null); // "user_id" 키로 로그인된 사용자 ID 가져오기

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("내가 쓴 게시글");  // 제목 설정


        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        adapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        // 하단 네비게이션 설정
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_mypage);  // 하단 네비게이션에서 현재 액티비티에 해당하는 메뉴 선택

        // 하단 네비게이션 뷰 선택 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_register) {
                // 현재 액티비티는 이미 선택되어 있으므로 아무 작업도 하지 않음
                return true;
            } else if (itemId == R.id.nav_border) {
                Intent intent = new Intent(MylistActivity.this, BoardActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_mypage) {
                Intent intent = new Intent(MylistActivity.this, MyPageActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // 게시글 불러오기
        fetchItems();
    }

    private void fetchItems() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // 서버로부터 받은 게시글 목록을 JSON 배열로 변환
                            JSONArray items = response.getJSONArray("items");

                            // 게시글 리스트를 초기화
                            itemList.clear();

                            // 서버에서 받은 게시글을 하나씩 처리
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                String itemUserId = item.getString("user_id"); // 서버에서 받은 게시글의 user_id

                                // 로그인된 user_id와 비교하여 로그인한 사용자의 게시글만 필터링
                                if (itemUserId.equals(loggedInUserId)) {
                                    Item newItem = new Item(
                                            item.getInt("id"),
                                            item.getString("title"),
                                            item.getString("description"),
                                            item.getString("price"),
                                            item.getString("image_url"),
                                            itemUserId
                                    );
                                    itemList.add(newItem);  // 로그인한 사용자의 게시글만 추가
                                }
                            }

                            // 데이터 변경을 RecyclerView에 알림
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            Log.e("MylistActivity", "JSON Parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MylistActivity", "Volley error: " + error.getMessage());
                    }
                });

        queue.add(request);
    }
}

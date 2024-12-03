package com.example.ph_k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

public class MylistActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> itemList;
    private static final String URL = "http://192.168.200.114:7310/get_items";

    // 드로어 관련 변수
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ActionBarDrawerToggle toggle;

    // 로그인된 user_id 저장할 변수
    private String loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        // SharedPreferences에서 로그인된 user_id 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        loggedInUserId = sharedPreferences.getString("username", null); // "username" 키로 로그인된 사용자 ID 가져오기

        // 드로어와 하단 네비게이션 설정
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // ActionBarDrawerToggle 설정
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 네비게이션 아이템 선택 리스너
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_register) {
                startActivity(new Intent(MylistActivity.this, RegisterActivity.class));
                return true;
            } else if (itemId == R.id.nav_mypage) {
                startActivity(new Intent(MylistActivity.this, MyPageActivity.class));
                return true;
            }
            drawerLayout.closeDrawers();
            return false;
        });

        // 하단 네비게이션 아이템 선택 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_register) {
                startActivity(new Intent(MylistActivity.this, RegisterActivity.class));
                return true;
            } else if (itemId == R.id.nav_border) {
                startActivity(new Intent(MylistActivity.this, BoardActivity.class));
                return true;
            } else if (itemId == R.id.nav_mypage) {
                startActivity(new Intent(MylistActivity.this, MyPageActivity.class));
                return true;
            }
            return false;
        });

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // DividerItemDecoration 추가
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        itemList = new ArrayList<>();
        adapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        fetchItems();  // 서버에서 게시글 데이터를 가져옵니다.
    }

    private void fetchItems() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MylistActivity", "Response: " + response.toString());
                        try {
                            JSONArray items = response.getJSONArray("items");
                            itemList.clear();  // 데이터 갱신 시 기존 아이템을 제거

                            // 서버에서 받은 게시글 목록을 로그인한 사용자와 비교하여 필터링
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                String itemUserId = item.getString("user_id"); // 게시글의 user_id

                                // 로그인된 사용자만 필터링하여 아이템 리스트에 추가
                                if (itemUserId.equals(loggedInUserId)) {
                                    Item newItem = new Item(
                                            item.getInt("id"),
                                            item.getString("title"),
                                            item.getString("description"),
                                            item.getString("price"),
                                            item.getString("image_url"),
                                            itemUserId
                                    );
                                    itemList.add(newItem);
                                }
                            }

                            // 아이템 목록이 갱신되었음을 RecyclerView에 알림
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

        queue.add(request);  // 네트워크 요청 큐에 추가
    }
}

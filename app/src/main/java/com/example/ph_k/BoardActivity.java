package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast; // Toast 추가
import androidx.annotation.NonNull;
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

public class BoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> itemList;
    private static final String URL = BuildConfig.BASE_URL + "/get_items";

    // 드로어 관련 변수
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

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

        // 현재 Activity와 연결된 메뉴 항목 선택
        bottomNavigationView.setSelectedItemId(R.id.nav_border);

        // 네비게이션 아이템 선택 리스너
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_register) {
                Intent intent = new Intent(BoardActivity.this, RegisterActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_mypage) {
                Intent intent = new Intent(BoardActivity.this, MyPageActivity.class);
                startActivity(intent);
                return true;
            }
            drawerLayout.closeDrawers();
            return false;
        });

        // 하단 네비게이션 아이템 선택 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_register) {
                Intent intent = new Intent(BoardActivity.this, RegisterActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_border) {
                // 현재 Activity가 BoardActivity이므로 아무 일도 하지 않음
                return true;
            } else if (itemId == R.id.nav_mypage) {
                Intent intent = new Intent(BoardActivity.this, MyPageActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.chat) { // 채팅 메뉴 항목 선택 시
                Intent intent = new Intent(BoardActivity.this, ChatActivity.class);
                startActivity(intent);
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

        // 채팅 버튼 설정 (여전히 별도의 버튼으로 채팅 화면으로 이동)
        Button chatButton = findViewById(R.id.chatButton); // activity_board.xml에서 추가한 버튼
        chatButton.setOnClickListener(v -> {
            // 채팅 버튼 클릭 시 로그 추가
            Log.d("BoardActivity", "채팅 버튼 클릭됨");

            // 채팅 화면으로 이동
            Intent intent = new Intent(BoardActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        // 아이템 데이터 요청
        fetchItems();
    }

    private void fetchItems() {
        // 네트워크 요청 대기 중 로딩 표시
        Toast.makeText(this, "데이터를 불러오는 중...", Toast.LENGTH_SHORT).show();

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("BoardActivity", "Response: " + response.toString());
                        try {
                            JSONArray items = response.getJSONArray("items");
                            itemList.clear(); // 데이터가 갱신될 때마다 리스트 초기화
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);

                                // 여러 이미지 URL을 JSONArray로 받아서 리스트로 저장
                                JSONArray imageUrls = item.getJSONArray("image_urls");
                                List<String> images = new ArrayList<>();
                                for (int j = 0; j < imageUrls.length(); j++) {
                                    images.add(imageUrls.getString(j)); // 이미지 URL 리스트에 추가
                                }

                                Item newItem = new Item(
                                        item.getInt("id"),
                                        item.getString("title"),
                                        item.getString("description"),
                                        item.getString("price"),
                                        images, // 여러 이미지 URL을 리스트로 전달
                                        item.getString("user_id") // user_id를 String으로 처리
                                );
                                itemList.add(newItem); // 새로운 아이템 추가
                            }

                            // 디버깅 로그 추가
                            Log.d("BoardActivity", "Item list size: " + itemList.size());
                            for (Item item : itemList) {
                                Log.d("BoardActivity", "Item: " + item.getTitle()); // 각 아이템의 제목 출력
                            }

                            adapter.notifyDataSetChanged(); // 데이터 변경을 RecyclerView에 알림
                        } catch (JSONException e) {
                            Log.e("BoardActivity", "JSON Parsing error: " + e.getMessage());
                            Toast.makeText(BoardActivity.this, "아이템을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("BoardActivity", "Volley error: " + error.getMessage());
                        Toast.makeText(BoardActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }
}

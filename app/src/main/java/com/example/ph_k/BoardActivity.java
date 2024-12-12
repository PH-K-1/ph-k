package com.example.ph_k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> itemList;
    private List<Item> filteredList; // 검색된 아이템을 저장
    private static final String URL = "http://192.168.200.114:7310/get_items";

    private String loggedInUserId;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        // 로그인된 사용자 ID 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        loggedInUserId = sharedPreferences.getString("username", null);

        // 드로어와 네비게이션 설정
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

        bottomNavigationView.setSelectedItemId(R.id.nav_border);

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_register) {
                startActivity(new Intent(BoardActivity.this, RegisterActivity.class));
                return true;
            } else if (itemId == R.id.nav_mypage) {
                startActivity(new Intent(BoardActivity.this, MyPageActivity.class));
                return true;
            }
            drawerLayout.closeDrawers();
            return false;
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_register) {
                startActivity(new Intent(BoardActivity.this, RegisterActivity.class));
                return true;
            } else if (itemId == R.id.nav_border) {
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(BoardActivity.this, ChatActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_mypage) {
                startActivity(new Intent(BoardActivity.this, MyPageActivity.class));
                return true;
            }
            return false;
        });

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        itemList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ItemAdapter(this, filteredList, loggedInUserId);
        recyclerView.setAdapter(adapter);

        // SearchView 설정
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterItems(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterItems(newText);
                return false;
            }
        });

        fetchItems();
    }

    private void fetchItems() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                response -> {
                    try {
                        JSONArray items = response.getJSONArray("items");
                        itemList.clear();
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);

                            // 이미지 URLs 리스트 추가
                            JSONArray imageUrls = item.getJSONArray("image_urls");
                            List<String> images = new ArrayList<>();
                            for (int j = 0; j < imageUrls.length(); j++) {
                                images.add(imageUrls.getString(j));
                            }

                            // 데드라인과 좋아요 상태 추가
                            String deadline = item.getString("deadline");
                            boolean isLiked = item.getBoolean("isLiked");

                            // 아이템 리스트에 추가
                            itemList.add(new Item(
                                    item.getInt("id"),
                                    item.getString("title"),
                                    item.getString("description"),
                                    item.getString("price"),
                                    images,
                                    item.getString("user_id"),
                                    deadline,
                                    isLiked
                            ));
                        }
                        filteredList.clear();
                        filteredList.addAll(itemList);
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("BoardActivity", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("BoardActivity", "Volley error: " + error.getMessage()));

        queue.add(request);
    }

    private void filterItems(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(itemList);
        } else {
            for (Item item : itemList) {
                if (item.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}

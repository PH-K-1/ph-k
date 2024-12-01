package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private static final String URL = "http://192.168.200.114:7310/get_items";

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

        fetchItems();
    }

    private void fetchItems() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray items = response.getJSONArray("items");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                Item newItem = new Item(
                                        item.getInt("id"),
                                        item.getString("title"),
                                        item.getString("description"),
                                        item.getString("price"),
                                        item.getString("image_url")
                                );
                                itemList.add(newItem);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e("BoardActivity", "JSON Parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("BoardActivity", "Volley error: " + error.getMessage());
                    }
                });

        queue.add(request);
    }
}
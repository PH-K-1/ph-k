package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // ActionBarDrawerToggle 설정
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 네비게이션 아이템 선택 리스너
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    // 홈 선택 시 처리
                    return true;
                } else if (itemId == R.id.nav_register) {
                    // 등록 선택 시 처리
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class); // 수정해야함
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_mypage) {
                    // 마이 페이지 선택 시 처리
                    Intent intent = new Intent(MainActivity.this, MainActivity.class); // 수정해야함
                    startActivity(intent);
                    return true;
                }
                drawerLayout.closeDrawers(); // 드로어 닫기
                return false;
            }
        });

        // 하단 네비게이션 뷰 선택 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    // 홈 선택 시 처리
                    return true;
                } else if (itemId == R.id.nav_mypage) {
                    // 마이 페이지 선택 시 처리
                    Intent intent = new Intent(MainActivity.this, MyPageActivity.class); // 수정해야함
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_register) {
                    // 등록 선택 시 처리
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class); // 수정해야함
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 토글 버튼 클릭 시 드로어 열고 닫기
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

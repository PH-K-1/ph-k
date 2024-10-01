package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class LoginActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // XML 레이아웃 설정

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
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    // 홈 선택 시 처리
                    return true;
                } else if (itemId == R.id.nav_register) {
                    // 이미 로그인 화면에 있으므로 처리하지 않음
                    return true;
                } else if (itemId == R.id.nav_mypage) {
                    // 마이 페이지 선택 시 처리
                    Intent intent = new Intent(LoginActivity.this, MyPageActivity.class);
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
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    // 홈 선택 시 처리
                    return true;
                } else if (itemId == R.id.nav_mypage) {
                    // 마이 페이지 선택 시 처리
                    Intent intent = new Intent(LoginActivity.this, MyPageActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_register){
                    // 이미 로그인 화면에 있으므로 처리하지 않음
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

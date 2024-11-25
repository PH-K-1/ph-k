package com.example.ph_k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MyPageActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        // 사용자 정보 불러오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "host");

        // 사용자 이름을 텍스트뷰에 표시
        TextView usernameTextView = findViewById(R.id.usernameTextView);
        usernameTextView.setText(username);

        // 네비게이션과 하단 네비게이션 설정
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 회원가입 버튼 클릭 리스너
        Button registerButton = findViewById(R.id.btn_signup);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPageActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 로그인 버튼 클릭 리스너
        Button loginButton = findViewById(R.id.btn_login);

        // 사용자가 로그인 상태라면 로그인 버튼을 '로그아웃'으로 변경
        if (!username.equals("host")) {
            loginButton.setText("로그아웃");
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 로그아웃 처리
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear(); // 저장된 사용자 정보 제거
                    editor.apply();

                    // 로그아웃 후 로그인 화면으로 이동
                    Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // 현재 액티비티 종료
                }
            });
        } else {
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 로그인 화면으로 이동
                    Intent intent = new Intent(MyPageActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 네비게이션 아이템 선택 리스너
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_register) {
                    Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_mypage) {
                    Intent intent = new Intent(MyPageActivity.this, MyPageActivity.class);
                    startActivity(intent);
                    return true;
                }
                drawerLayout.closeDrawers();
                return false;
            }
        });

        // 하단 네비게이션 뷰 선택 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_mypage) {
                    Intent intent = new Intent(MyPageActivity.this, MyPageActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_register) {
                    Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

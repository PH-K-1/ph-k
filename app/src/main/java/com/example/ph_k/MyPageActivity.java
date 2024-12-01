package com.example.ph_k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        String username = sharedPreferences.getString("username", null);

        // 사용자 이름을 텍스트뷰에 표시
        TextView usernameTextView = findViewById(R.id.usernameTextView);
        if (username != null) {
            usernameTextView.setText(username);
        } else {
            usernameTextView.setText("로그인 후 이용 가능합니다.");
        }

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

        // 현재 Activity와 연결된 메뉴 항목 선택
        bottomNavigationView.setSelectedItemId(R.id.nav_mypage);

        // 로그인 버튼 클릭 리스너
        Button loginButton = findViewById(R.id.btn_login);

        // 사용자가 로그인 상태라면 로그인 버튼을 '로그아웃'으로 변경
        if (username != null) {
            loginButton.setText("로그아웃");
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 로그아웃 처리
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear(); // 저장된 사용자 정보 제거
                    editor.apply();

                    // 로그아웃 후 로그인 화면으로 이동
                    Intent intent = new Intent(MyPageActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // 현재 액티비티 종료
                }
            });
        } else {
            loginButton.setText("로그인");
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 로그인 화면으로 이동
                    Intent intent = new Intent(MyPageActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 내가 쓴 게시글 버튼 클릭 리스너
        Button writtenPostsButton = findViewById(R.id.btn_written_posts);
        writtenPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 상태 확인
                if (username == null) {
                    // 로그인되지 않은 경우, Toast 메시지 표시
                    Toast.makeText(MyPageActivity.this, "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    // 로그인된 경우, 내가 쓴 게시글 화면으로 이동
                    Intent intent = new Intent(MyPageActivity.this, MylistActivity.class);
                    startActivity(intent);
                }
            }
        });

        // 네비게이션 아이템 선택 리스너
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_border) {
                    Intent intent = new Intent(MyPageActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_mypage) {
                    // 현재 Activity가 MyPageActivity이므로 아무 일도 하지 않음
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
                if (itemId == R.id.nav_register) {
                    Intent intent = new Intent(MyPageActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_border) {
                    Intent intent = new Intent(MyPageActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_mypage) {
                    // 현재 Activity가 MyPageActivity이므로 아무 일도 하지 않음
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

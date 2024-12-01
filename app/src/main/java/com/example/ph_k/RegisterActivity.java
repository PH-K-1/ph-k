package com.example.ph_k;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_CODE = 1000;
    private ImageView imagePreview;
    private Uri imageUri;

    // 드로어 및 하단 네비게이션 관련 변수들
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 뷰 설정
        imagePreview = findViewById(R.id.image_preview);
        EditText editTitle = findViewById(R.id.edit_title);
        EditText editDescription = findViewById(R.id.edit_description);
        EditText editPrice = findViewById(R.id.edit_price);
        Button buttonUploadImage = findViewById(R.id.button_upload_image);
        Button buttonRegister = findViewById(R.id.button_register);

        // 드로어 및 하단 네비게이션 설정
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

        // ** 현재 Activity와 연결된 메뉴 항목 선택 **
        bottomNavigationView.setSelectedItemId(R.id.nav_register);

        // 네비게이션 아이템 선택 리스너
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_register) {
                return true;
            } else if (itemId == R.id.nav_mypage) {
                Intent intent = new Intent(RegisterActivity.this, MyPageActivity.class);
                startActivity(intent);
                return true;
            }
            drawerLayout.closeDrawers(); // 드로어 닫기
            return false;
        });

        // 하단 네비게이션 뷰 선택 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_register) {
                return true;
            } else if (itemId == R.id.nav_border) {
                Intent intent = new Intent(RegisterActivity.this, BoardActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_mypage) {
                Intent intent = new Intent(RegisterActivity.this, MyPageActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // 이미지 업로드 버튼 클릭 리스너
        buttonUploadImage.setOnClickListener(v -> pickImage());

        // 등록 버튼 클릭 리스너
        buttonRegister.setOnClickListener(v -> {
            String title = editTitle.getText().toString();
            String description = editDescription.getText().toString();
            String price = editPrice.getText().toString();

            if (imageUri == null || title.isEmpty() || description.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadData(title, description, price, imageUri);
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri); // 선택한 이미지 미리보기
        }
    }

    private void uploadData(String title, String description, String price, Uri imageUri) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // 이미지 변환
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] imageData = stream.toByteArray();

                // SharedPreferences에서 로그인된 사용자 정보 가져오기
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String username = sharedPreferences.getString("username", null);  // 로그인된 사용자 이름 가져오기

                if (username == null) {
                    // 로그인되지 않은 경우 처리 (예: 로그인 화면으로 이동)
                    runOnUiThread(() -> Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show());
                    return;
                }

                // 서버 요청 생성
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title", title)
                        .addFormDataPart("description", description)
                        .addFormDataPart("price", price)
                        .addFormDataPart("user_id", username)  // USER_ID 추가
                        .addFormDataPart("image", "image.jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageData))
                        .build();

                Request request = new Request.Builder()
                        .url("http://192.168.55.231:7310/upload") // Flask 서버 URL
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(this, "등록 성공", Toast.LENGTH_SHORT).show();
                        // 등록 성공 후 BoardActivity로 이동
                        Intent intent = new Intent(RegisterActivity.this, BoardActivity.class);
                        startActivity(intent); // BoardActivity로 이동
                        finish(); // 현재 Activity 종료
                    } else {
                        Toast.makeText(this, "등록 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("UploadError", "Error: " + e.getMessage());
            }
        }).start();
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

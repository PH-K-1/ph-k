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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_CODE = 1000;
    private ImageView imagePreview1, imagePreview2, imagePreview3, imagePreview4, imagePreview5;

    private List<Uri> imageUris = new ArrayList<>();  // 선택된 이미지 URI들을 저장

    // 드로어 관련 변수들
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 뷰 설정
        imagePreview1 = findViewById(R.id.image_preview_1);
        imagePreview2 = findViewById(R.id.image_preview_2);
        imagePreview3 = findViewById(R.id.image_preview_3);
        imagePreview4 = findViewById(R.id.image_preview_4);
        imagePreview5 = findViewById(R.id.image_preview_5);

        EditText editTitle = findViewById(R.id.edit_title);
        EditText editDescription = findViewById(R.id.edit_description);
        EditText editPrice = findViewById(R.id.edit_price);
        ImageView buttonUploadImage = findViewById(R.id.button_upload_image); // ImageView로 변경
        Button buttonRegister = findViewById(R.id.button_register);

        // 드로어 설정
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
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

        // 이미지 업로드 버튼 클릭 리스너
        buttonUploadImage.setOnClickListener(v -> pickImage());

        // 등록 버튼 클릭 리스너
        buttonRegister.setOnClickListener(v -> {
            String title = editTitle.getText().toString();
            String description = editDescription.getText().toString();
            String price = editPrice.getText().toString();

            if (imageUris == null || title.isEmpty() || description.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadData(title, description, price, imageUris);
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 여러 이미지 선택 허용
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                if (count > 5) {
                    Toast.makeText(this, "최대 5개의 이미지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    count = 5; // 최대 5개까지만 처리
                }

                imageUris.clear();  // 기존 선택된 이미지들을 초기화

                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);  // 선택된 이미지들 추가
                }

                // 선택된 이미지를 각 ImageView에 설정
                if (imageUris.size() > 0) {
                    imagePreview1.setImageURI(imageUris.get(0)); // 첫 번째 이미지
                }
                if (imageUris.size() > 1) {
                    imagePreview2.setImageURI(imageUris.get(1)); // 두 번째 이미지
                }
                if (imageUris.size() > 2) {
                    imagePreview3.setImageURI(imageUris.get(2)); // 세 번째 이미지
                }
                if (imageUris.size() > 3) {
                    imagePreview4.setImageURI(imageUris.get(3)); // 네 번째 이미지
                }
                if (imageUris.size() > 4) {
                    imagePreview5.setImageURI(imageUris.get(4)); // 다섯 번째 이미지
                }
            } else if (data.getData() != null) {
                // 단일 이미지 선택 시 처리
                imageUris.clear();
                imageUris.add(data.getData());
                imagePreview1.setImageURI(imageUris.get(0)); // 첫 번째 이미지만 미리보기로 설정
            }
        }
    }

    // 업로드 처리 메소드
    private void uploadData(String title, String description, String price, List<Uri> imageUris) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                builder.addFormDataPart("title", title);
                builder.addFormDataPart("description", description);
                builder.addFormDataPart("price", price);

                // SharedPreferences에서 로그인된 사용자 정보 가져오기
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String username = sharedPreferences.getString("username", null);  // 로그인된 사용자 이름 가져오기

                if (username == null) {
                    // 로그인되지 않은 경우 처리 (예: 로그인 화면으로 이동)
                    runOnUiThread(() -> Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show());
                    return;
                }

                builder.addFormDataPart("user_id", username);  // USER_ID 추가

                // 여러 이미지를 처리
                for (int i = 0; i < imageUris.size(); i++) {
                    Uri imageUri = imageUris.get(i);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    byte[] imageData = stream.toByteArray();
                    builder.addFormDataPart("image" + i, "image" + i + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageData));
                }

                RequestBody requestBody = builder.build();
                Request request = new Request.Builder()
                        .url("http://192.168.200.114:7310/upload")
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

}
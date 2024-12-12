package com.example.ph_k;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_CODE = 1000;
    private ImageView imagePreview1, imagePreview2, imagePreview3, imagePreview4, imagePreview5;

    private List<Uri> imageUris = new ArrayList<>();
    private Button buttonSelectDeadline;
    private String selectedDeadline;

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
        buttonSelectDeadline = findViewById(R.id.edit_deadline);
        ImageView buttonUploadImage = findViewById(R.id.button_upload_image);
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

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_register) {
                return true;
            } else if (itemId == R.id.nav_mypage) {
                Intent intent = new Intent(RegisterActivity.this, MyPageActivity.class);
                startActivity(intent);
                return true;
            }
            drawerLayout.closeDrawers();
            return false;
        });

        // 날짜와 시간 선택 버튼 클릭 리스너
        buttonSelectDeadline.setOnClickListener(v -> showDateTimePicker());

        // 등록 버튼 클릭 리스너
        buttonRegister.setOnClickListener(v -> {
            String title = editTitle.getText().toString();
            String description = editDescription.getText().toString();
            String price = editPrice.getText().toString();

            if (imageUris.isEmpty() || title.isEmpty() || description.isEmpty() || price.isEmpty() || selectedDeadline == null || selectedDeadline.isEmpty()) {
                showCustomToast("모든 항목을 입력하세요.");
                return;
            }

            uploadData(title, description, price, selectedDeadline, imageUris);
        });

        // 이미지 업로드 버튼 클릭 리스너
        buttonUploadImage.setOnClickListener(v -> pickImage());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth1) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay1, minute1) -> {
                selectedDeadline = year1 + "-" + (month1 + 1) + "-" + dayOfMonth1 + " " + hourOfDay1 + ":" + minute1;
                buttonSelectDeadline.setText("경매 마감일:  " + selectedDeadline);
            }, hourOfDay, minute, false);
            timePickerDialog.show();
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                if (count > 5) {
                    showCustomToast("최대 5개의 이미지만 선택할 수 있습니다.");
                    count = 5;
                }

                imageUris.clear();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                }

                // 이미지 미리보기 설정
                if (imageUris.size() > 0) imagePreview1.setImageURI(imageUris.get(0));
                if (imageUris.size() > 1) imagePreview2.setImageURI(imageUris.get(1));
                if (imageUris.size() > 2) imagePreview3.setImageURI(imageUris.get(2));
                if (imageUris.size() > 3) imagePreview4.setImageURI(imageUris.get(3));
                if (imageUris.size() > 4) imagePreview5.setImageURI(imageUris.get(4));
            } else if (data.getData() != null) {
                imageUris.clear();
                imageUris.add(data.getData());
                imagePreview1.setImageURI(imageUris.get(0));
            }
        }
    }

    private void uploadData(String title, String description, String price, String deadline, List<Uri> imageUris) {
        new Thread(() -> {
            try {
                String cleanPrice = price.replaceAll("[,]", "");

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                builder.addFormDataPart("title", title);
                builder.addFormDataPart("description", description);
                builder.addFormDataPart("price", cleanPrice);
                builder.addFormDataPart("deadline", deadline);

                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String username = sharedPreferences.getString("username", null);

                if (username == null) {
                    runOnUiThread(() -> showCustomToast("로그인된 사용자가 없습니다."));
                    return;
                }

                builder.addFormDataPart("user_id", username);

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
                        showCustomToast("등록 성공");
                        Intent intent = new Intent(RegisterActivity.this, BoardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        showCustomToast("등록 실패");
                    }
                });
            } catch (Exception e) {
                Log.e("UploadError", "Error: " + e.getMessage());
            }
        }).start();
    }

    public void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View customView = inflater.inflate(R.layout.custom_toast, null);

        TextView toastMessage = customView.findViewById(R.id.toast_message);
        toastMessage.setText(message);

        Toast customToast = new Toast(getApplicationContext());
        customToast.setDuration(Toast.LENGTH_SHORT);
        customToast.setView(customView);
        customToast.show();
    }
}

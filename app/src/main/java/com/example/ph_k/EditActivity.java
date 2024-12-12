package com.example.ph_k;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

public class EditActivity extends AppCompatActivity {

    private EditText editTitle, editDescription, editPrice;
    private Button saveButton, buttonSelectDeadline;
    private ImageView imagePreview1, imagePreview2, imagePreview3, imagePreview4, imagePreview5;
    private ArrayList<String> imageUrls = new ArrayList<>();
    private int postId;
    private String selectedDeadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // UI 초기화
        editTitle = findViewById(R.id.edit_title);
        editDescription = findViewById(R.id.edit_description);
        editPrice = findViewById(R.id.edit_price);
        buttonSelectDeadline = findViewById(R.id.edit_deadline);
        saveButton = findViewById(R.id.button_save);

        imagePreview1 = findViewById(R.id.image_preview_1);
        imagePreview2 = findViewById(R.id.image_preview_2);
        imagePreview3 = findViewById(R.id.image_preview_3);
        imagePreview4 = findViewById(R.id.image_preview_4);
        imagePreview5 = findViewById(R.id.image_preview_5);

        // Intent로 전달된 데이터 설정
        Intent intent = getIntent();
        postId = intent.getIntExtra("item_id", -1);
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String price = intent.getStringExtra("price");
        imageUrls = intent.getStringArrayListExtra("image_urls");

        editTitle.setText(title);
        editDescription.setText(description);
        editPrice.setText(price);

        // 이미지 로드
        loadImages(imageUrls);

        // 날짜와 시간 선택 버튼 리스너 설정
        buttonSelectDeadline.setOnClickListener(v -> showDateTimePicker());

        // 이미지 선택 리스너 설정
        imagePreview1.setOnClickListener(v -> openImagePicker(1));
        imagePreview2.setOnClickListener(v -> openImagePicker(2));
        imagePreview3.setOnClickListener(v -> openImagePicker(3));
        imagePreview4.setOnClickListener(v -> openImagePicker(4));
        imagePreview5.setOnClickListener(v -> openImagePicker(5));

        // 저장 버튼 리스너 설정
        saveButton.setOnClickListener(v -> savePost());
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth1) -> {
            selectedDeadline = year1 + "-" + (month1 + 1) + "-" + dayOfMonth1;

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute1) -> {
                selectedDeadline += " " + hourOfDay + ":" + minute1;
                buttonSelectDeadline.setText("경매 마감일: " + selectedDeadline);
            }, hour, minute, true);
            timePickerDialog.show();
        }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    private void loadImages(ArrayList<String> imageUrls) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            if (imageUrls.size() > 0) Glide.with(this).load(imageUrls.get(0)).into(imagePreview1);
            if (imageUrls.size() > 1) Glide.with(this).load(imageUrls.get(1)).into(imagePreview2);
            if (imageUrls.size() > 2) Glide.with(this).load(imageUrls.get(2)).into(imagePreview3);
            if (imageUrls.size() > 3) Glide.with(this).load(imageUrls.get(3)).into(imagePreview4);
            if (imageUrls.size() > 4) Glide.with(this).load(imageUrls.get(4)).into(imagePreview5);
        }
    }

    private void openImagePicker(int previewIndex) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, previewIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                switch (requestCode) {
                    case 1:
                        Glide.with(this).load(imageUri).into(imagePreview1);
                        updateImageUrls(imageUri.toString(), 0);
                        break;
                    case 2:
                        Glide.with(this).load(imageUri).into(imagePreview2);
                        updateImageUrls(imageUri.toString(), 1);
                        break;
                    case 3:
                        Glide.with(this).load(imageUri).into(imagePreview3);
                        updateImageUrls(imageUri.toString(), 2);
                        break;
                    case 4:
                        Glide.with(this).load(imageUri).into(imagePreview4);
                        updateImageUrls(imageUri.toString(), 3);
                        break;
                    case 5:
                        Glide.with(this).load(imageUri).into(imagePreview5);
                        updateImageUrls(imageUri.toString(), 4);
                        break;
                }
            }
        }
    }

    private void updateImageUrls(String imageUrl, int index) {
        if (index < imageUrls.size()) {
            imageUrls.set(index, imageUrl);
        } else {
            imageUrls.add(imageUrl);
        }
    }

    private void savePost() {
        String updatedTitle = editTitle.getText().toString();
        String updatedDescription = editDescription.getText().toString();
        String updatedPrice = editPrice.getText().toString();

        if (updatedTitle.isEmpty() || updatedDescription.isEmpty() || selectedDeadline == null || selectedDeadline.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                deleteOldPost(postId);

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build();

                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                builder.addFormDataPart("title", updatedTitle);
                builder.addFormDataPart("description", updatedDescription);
                builder.addFormDataPart("price", updatedPrice);
                builder.addFormDataPart("deadline", selectedDeadline);

                for (int i = 0; i < imageUrls.size(); i++) {
                    String url = imageUrls.get(i);
                    builder.addFormDataPart("image" + i, "image" + i + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), url.getBytes()));
                }

                RequestBody requestBody = builder.build();
                Request request = new Request.Builder()
                        .url(BuildConfig.BASE_URL+"upload")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(EditActivity.this, "게시글이 수정되었습니다.", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(EditActivity.this, "게시글 수정 실패", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("EditError", e.getMessage());
            }
        }).start();
    }

    private void deleteOldPost(int postId) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(BuildConfig.BASE_URL+"/posts/" + postId)
                        .delete()
                        .build();
                client.newCall(request).execute();
            } catch (Exception e) {
                Log.e("DeleteError", e.getMessage());
            }
        }).start();
    }
}

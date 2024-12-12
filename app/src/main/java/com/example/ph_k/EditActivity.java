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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

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
    private Button saveButton;
    private int postId;

    private ImageView imagePreview1, imagePreview2, imagePreview3, imagePreview4, imagePreview5;
    private ArrayList<String> imageUrls = new ArrayList<>(); // 이미지 URL 리스트
    private Button buttonSelectDeadline;  // 날짜와 시간 선택 버튼
    private String selectedDeadline;      // 선택된 데드라인을 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // XML에 정의된 ID와 연결
        editTitle = findViewById(R.id.edit_title);
        editDescription = findViewById(R.id.edit_description);
        buttonSelectDeadline = findViewById(R.id.edit_deadline);  // 날짜 버튼 초기화
        editPrice = findViewById(R.id.edit_price);
        saveButton = findViewById(R.id.button_save);

        // 날짜와 시간 선택 버튼 클릭 리스너
        buttonSelectDeadline.setOnClickListener(v -> showDateTimePicker());

        // 이미지 미리보기 클릭 시 이미지 선택기 열기
        imagePreview1 = findViewById(R.id.image_preview_1);
        imagePreview2 = findViewById(R.id.image_preview_2);
        imagePreview3 = findViewById(R.id.image_preview_3);
        imagePreview4 = findViewById(R.id.image_preview_4);
        imagePreview5 = findViewById(R.id.image_preview_5);

        imagePreview1.setOnClickListener(v -> openImagePicker(1)); // 1번 이미지
        imagePreview2.setOnClickListener(v -> openImagePicker(2)); // 2번 이미지
        imagePreview3.setOnClickListener(v -> openImagePicker(3)); // 3번 이미지
        imagePreview4.setOnClickListener(v -> openImagePicker(4)); // 4번 이미지
        imagePreview5.setOnClickListener(v -> openImagePicker(5)); // 5번 이미지

        // 수정 버튼 클릭 리스너
        saveButton.setOnClickListener(v -> savePost());

        // Intent로 전달된 데이터 가져오기
        Intent intent = getIntent();
        postId = intent.getIntExtra("item_id", -1);
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String price = intent.getStringExtra("price");
        imageUrls = intent.getStringArrayListExtra("image_urls");

        // 가져온 데이터를 UI에 설정
        editTitle.setText(title);
        editDescription.setText(description);
        editPrice.setText(price);

        // 이미지 로드
        loadImages(imageUrls);
    }

    private void showDateTimePicker() {
        // 현재 날짜와 시간으로 DatePickerDialog 및 TimePickerDialog 설정
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 날짜 선택
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth1) -> {
                    // 선택된 날짜 저장
                    selectedDeadline = year1 + "-" + (month1 + 1) + "-" + dayOfMonth1;

                    // 시간 선택
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (view1, hourOfDay, minute1) -> {
                                // 선택된 시간 저장
                                selectedDeadline += " " + hourOfDay + ":" + minute1;
                                buttonSelectDeadline.setText("경매 마감일:  " + selectedDeadline);  // 버튼에 데드라인 표시
                            }, hour, minute, true);
                    timePickerDialog.show();
                }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    private void loadImages(ArrayList<String> imageUrls) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            // Glide로 이미지 로드
            if (imageUrls.size() > 0) {
                Glide.with(this).load(imageUrls.get(0)).into(imagePreview1);
            }
            if (imageUrls.size() > 1) {
                Glide.with(this).load(imageUrls.get(1)).into(imagePreview2);
            }
            if (imageUrls.size() > 2) {
                Glide.with(this).load(imageUrls.get(2)).into(imagePreview3);
            }
            if (imageUrls.size() > 3) {
                Glide.with(this).load(imageUrls.get(3)).into(imagePreview4);
            }
            if (imageUrls.size() > 4) {
                Glide.with(this).load(imageUrls.get(4)).into(imagePreview5);
            }
        }
    }

    private void openImagePicker(int previewIndex) {
        // 이미지 선택을 위한 Intent 생성
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, previewIndex); // 각 프리뷰에 대해 고유한 요청 코드 전달
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // Glide로 이미지 미리보기 업데이트
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
        // imageUrls 리스트를 업데이트
        if (index < imageUrls.size()) {
            imageUrls.set(index, imageUrl); // 기존 URL 업데이트
        } else {
            imageUrls.add(imageUrl); // 새로운 URL 추가
        }
    }

    private void savePost() {
        // 사용자가 입력한 데이터 가져오기
        String updatedTitle = editTitle.getText().toString();
        String updatedDescription = editDescription.getText().toString();
        String updatedPrice = editPrice.getText().toString();
        String updatedDeadLine = selectedDeadline;

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String updatedUserId = sharedPreferences.getString("username", "");

        if (updatedTitle.isEmpty() || updatedDescription.isEmpty()  || selectedDeadline == null || selectedDeadline.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Uri> imageUris = new ArrayList<>();
        for (String url : imageUrls) {
            imageUris.add(Uri.parse(url));
        }

        new Thread(() -> {
            try {
                // 1. 기존 게시글 삭제
                deleteOldPost(postId);

                // 2. 새로운 게시글 생성
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃 설정 (60초)
                        .writeTimeout(60, TimeUnit.SECONDS)  // 요청 쓰기 타임아웃 설정 (60초)
                        .readTimeout(60, TimeUnit.SECONDS)   // 응답 읽기 타임아웃 설정 (60초)
                        .build();

                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                builder.addFormDataPart("title", updatedTitle);
                builder.addFormDataPart("description", updatedDescription);
                builder.addFormDataPart("price", updatedPrice);
                builder.addFormDataPart("user_id", updatedUserId);
                builder.addFormDataPart("deadline", updatedDeadLine);

                for (int i = 0; i < imageUris.size(); i++) {
                    Uri imageUri = imageUris.get(i);
                    if (imageUri.toString().startsWith("http://") || imageUri.toString().startsWith("https://")) {
                        byte[] imageData = downloadImage(imageUri.toString());
                        if (imageData != null) {
                            builder.addFormDataPart("image" + i, "image" + i + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageData));
                        }
                    } else {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                        byte[] imageData = stream.toByteArray();
                        builder.addFormDataPart("image" + i, "image" + i + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageData));
                    }
                }

                RequestBody requestBody = builder.build();
                Request request = new Request.Builder()
                        .url("http://192.168.55.231:7310/upload")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "게시글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "게시글 수정 실패", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }



    // 기존 게시글을 삭제하는 메서드 (DELETE 요청)
    private void deleteOldPost(int postId) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // 게시글 삭제 요청을 위한 URL
                String url = "http://192.168.55.231:7310/posts/" + postId;
                Request request = new Request.Builder()
                        .url(url)
                        .delete()  // DELETE 요청 보내기
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    Log.e("DeleteError", "게시글 삭제 실패: " + response.message());
                }
            } catch (Exception e) {
                Log.e("DeleteError", "게시글 삭제 오류: " + e.getMessage());
            }
        }).start();
    }

    private byte[] downloadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            Log.e("DownloadError", "Error downloading image: " + e.getMessage());
            return null;
        }
    }
}
package com.example.ph_k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private TextView titleTextView, descriptionTextView, priceTextView, useridTextView;
    private ImageView backButton, shareButton, favoriteButton;
    private MaterialButton buyButton;
    private ViewPager2 viewPager;
    private List<String> imageUrls = new ArrayList<>();

    private boolean isLiked = false;  // 좋아요 상태 (기본값은 false)
    private int itemId;  // 게시글 ID
    private String itemUserId;  // 게시글 작성자의 ID
    private String loggedInUserId;  // 로그인한 사용자 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // SharedPreferences에서 로그인된 user_id 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        loggedInUserId = sharedPreferences.getString("username", null); // "username" 키로 로그인된 사용자 ID 가져오기

        // UI 요소들 초기화
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        priceTextView = findViewById(R.id.priceTextView);
        backButton = findViewById(R.id.backButton);
        shareButton = findViewById(R.id.shareButton);
//        favoriteButton = findViewById(R.id.favoriteButton);
        buyButton = findViewById(R.id.buyButton);
        viewPager = findViewById(R.id.viewPager);
        useridTextView = findViewById(R.id.useridTextView);

        // 뒤로가기 버튼 클릭 리스너 설정
        backButton.setOnClickListener(v -> finish());

        // 공유하기 버튼 클릭 리스너 설정
        shareButton.setOnClickListener(v -> sharePost());

        // 게시글 세부 정보 표시
        displayPostDetails();

        // 좋아요 버튼 상태 확인
        checkLikeStatus();

        // 좋아요 버튼 클릭 리스너 설정
        favoriteButton.setOnClickListener(v -> toggleLikeStatus());

        // Buy 버튼 클릭 리스너 설정
        buyButton.setOnClickListener(v -> navigateToChat());
    }

    // 게시글 세부 정보 표시
    private void displayPostDetails() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String price = intent.getStringExtra("price");
        itemUserId = intent.getStringExtra("item_Userid");  // 게시글 작성자의 ID
        ArrayList<String> imageUrlList = intent.getStringArrayListExtra("image_urls");

        itemId = intent.getIntExtra("item_id", -1);  // 게시글의 itemId 받아오기

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        priceTextView.setText(price);
        useridTextView.setText(itemUserId);

        if (imageUrlList != null) {
            imageUrls.addAll(imageUrlList);
        }

        ImageAdapter adapter = new ImageAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);

        // 좋아요 상태에 맞게 아이콘 설정
        favoriteButton.setImageResource(isLiked ? R.drawable.ic_favorite_24 : R.drawable.ic_nofavorite_24);
    }

    // 공유하기 기능
    private void sharePost() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");

        String sharedUrl = "https://example.com/post?title=" + title;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing post: " + sharedUrl);
        startActivity(Intent.createChooser(shareIntent, "Share using"));
    }

    // 좋아요 상태 확인 (서버로 요청하여 좋아요 상태 확인)
    private void checkLikeStatus() {
        String url = "http://192.168.200.114:7310/check_like_status";  // 좋아요 상태 확인을 위한 API URL
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", loggedInUserId);  // 로그인한 사용자 ID
            data.put("item_id", itemId);  // 게시글 ID

            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                    response -> {
                        try {
                            boolean isLikedFromServer = response.getBoolean("isLiked");
                            isLiked = isLikedFromServer;
                            favoriteButton.setImageResource(isLiked ? R.drawable.ic_favorite_24 : R.drawable.ic_nofavorite_24);
                            favoriteButton.setEnabled(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showCustomToast("좋아요 상태 조회 실패");
                        }
                    },
                    error -> showCustomToast("서버 통신 실패")
            );
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 좋아요 상태 토글 (버튼 클릭 시)
    private void toggleLikeStatus() {
        favoriteButton.setEnabled(false);
        String url = isLiked ? "http://192.168.200.114:7310/unlike" : "http://192.168.200.114:7310/like";
        isLiked = !isLiked;
        favoriteButton.setImageResource(isLiked ? R.drawable.ic_favorite_24 : R.drawable.ic_nofavorite_24);

        JSONObject data = new JSONObject();
        try {
            data.put("user_id", loggedInUserId);
            data.put("item_id", itemId);

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                    response -> {
                        String message = isLiked ? "좋아요 추가되었습니다." : "좋아요 취소되었습니다.";
                        showCustomToast(message);
                        favoriteButton.setEnabled(true);
                    },
                    error -> {
                        showCustomToast("좋아요 처리 실패");
                        favoriteButton.setEnabled(true);
                    }
            );
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            favoriteButton.setEnabled(true);
        }
    }

    // Buy 버튼 클릭 시 채팅 화면으로 이동
    private void navigateToChat() {
        Intent chatIntent = new Intent(PostDetailActivity.this, ChatRoomActivity.class);
        chatIntent.putExtra("postTitle", getIntent().getStringExtra("title"));
        chatIntent.putExtra("postPrice", getIntent().getStringExtra("price"));
        chatIntent.putExtra("postUserId", itemUserId);
        startActivity(chatIntent);
    }

    // 커스텀 Toast 메서드
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
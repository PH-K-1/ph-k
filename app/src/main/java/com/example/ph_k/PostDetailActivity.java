package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private TextView titleTextView, descriptionTextView, priceTextView, useridTextView;
    private ImageView backButton, shareButton, favoriteButton;
    private Button buyButton;
    private ViewPager2 viewPager;
    private List<String> imageUrls = new ArrayList<>();

    private boolean isLiked = false;  // 좋아요 상태 (기본값은 false)
    private int userId;  // 로그인한 사용자 ID
    private int itemId;  // 게시글 ID
    private String itemUserId;  // 게시글 작성자의 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // UI 요소들 초기화
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        priceTextView = findViewById(R.id.priceTextView);
        backButton = findViewById(R.id.backButton);
        shareButton = findViewById(R.id.shareButton);
        favoriteButton = findViewById(R.id.favoriteButton);
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
        this.userId = intent.getIntExtra("user_id", -1);  // 로그인한 userId 받아오기
        boolean isLiked = intent.getBooleanExtra("isLiked", false);  // 좋아요 상태 전달받기

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
        if (isLiked) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_24);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_nofavorite_24);
        }
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
        String url = "http://192.168.55.231:7310/check_like_status";  // 좋아요 상태 확인을 위한 API URL
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", itemUserId);  // 로그인한 사용자 ID
            data.put("item_id", itemId);  // 게시글 ID

            // Volley의 RequestQueue 객체 생성
            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                    response -> {
                        try {
                            // 서버에서 좋아요 상태를 받아옴
                            boolean isLikedFromServer = response.getBoolean("isLiked");

                            // 서버에서 받은 좋아요 상태에 따라 버튼 아이콘을 변경
                            if (isLikedFromServer) {
                                favoriteButton.setImageResource(R.drawable.ic_favorite_24);  // 좋아요 있음 아이콘
                                isLiked = true;  // 좋아요 상태 갱신
                            } else {
                                favoriteButton.setImageResource(R.drawable.ic_nofavorite_24);  // 좋아요 없음 아이콘
                                isLiked = false;  // 좋아요 상태 갱신
                            }

                            favoriteButton.setEnabled(true);  // 버튼 활성화
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(PostDetailActivity.this, "좋아요 상태 조회 실패", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        // 에러 처리
                        Toast.makeText(PostDetailActivity.this, "서버 통신 실패", Toast.LENGTH_SHORT).show();
                    }
            );

            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 좋아요 상태 토글 (버튼 클릭 시)
    // 좋아요 상태 토글 (버튼 클릭 시)
    private void toggleLikeStatus() {
        // 버튼을 클릭하자마자 비활성화
        favoriteButton.setEnabled(false);

        // 좋아요 상태에 따라 URL 설정
        String url = isLiked ? "http://192.168.55.231:7310/unlike" : "http://192.168.55.231:7310/like";

        // 좋아요 상태 변경
        isLiked = !isLiked;  // 좋아요 상태 토글

        // 버튼 아이콘 변경
        if (isLiked) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_24);  // 좋아요 있음 아이콘
        } else {
            favoriteButton.setImageResource(R.drawable.ic_nofavorite_24);  // 좋아요 없음 아이콘
        }

        // 서버에 좋아요 상태 전송
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", itemUserId);  // 로그인한 사용자 ID
            data.put("item_id", itemId);  // 게시글 ID

            // Volley를 사용한 요청
            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                    response -> {
                        // 서버 응답 처리
                        String message = isLiked ? "좋아요 추가되었습니다." : "좋아요 취소되었습니다.";
                        Toast.makeText(PostDetailActivity.this, message, Toast.LENGTH_SHORT).show();

                        // 좋아요 상태가 갱신된 후 현재 액티비티 종료
                        finish();  // MylikeActivity를 갱신하려면 현재 액티비티를 종료
                    },
                    error -> {
                        // 서버 오류 처리
                        Toast.makeText(PostDetailActivity.this, "좋아요 처리 실패", Toast.LENGTH_SHORT).show();

                        // 실패 시에도 버튼 활성화
                        favoriteButton.setEnabled(true);
                    }
            );
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            // 예외 처리 시에도 버튼 활성화
            favoriteButton.setEnabled(true);
        }
    }


    // Buy 버튼 클릭 시 채팅 화면으로 이동
    private void navigateToChat() {
        Intent chatIntent = new Intent(PostDetailActivity.this, ChatActivity.class);
        chatIntent.putExtra("postTitle", getIntent().getStringExtra("title"));
        chatIntent.putExtra("postPrice", getIntent().getStringExtra("price"));
        chatIntent.putExtra("postUserId", itemUserId);  // 게시글 작성자의 ID
        startActivity(chatIntent);
    }
}

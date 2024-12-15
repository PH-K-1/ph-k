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

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
// 게시글 세부 정보 표시
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PostDetailActivity extends AppCompatActivity {
    private TextView titleTextView, descriptionTextView, priceTextView, useridTextView, deadlineTextView;
    private ImageView backButton, shareButton, favoriteButton;
    private Button buyButton;
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
        favoriteButton = findViewById(R.id.favoriteButton);
        buyButton = findViewById(R.id.buyButton);
        viewPager = findViewById(R.id.viewPager);
        useridTextView = findViewById(R.id.useridTextView);
        deadlineTextView = findViewById(R.id.deadlineTextView);  // 데드라인 TextView 추가

        // 뒤로가기 버튼 클릭 리스너 설정
        backButton.setOnClickListener(v -> finish());

        // 공유하기 버튼 클릭 리스너 설정
        shareButton.setOnClickListener(v -> sharePost());

        // 게시글 세부 정보 표시
        displayPostDetails();

        // 좋아요 상태 확인
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
        String deadline = intent.getStringExtra("deadline");  // 데드라인 값을 Intent로부터 받아옴

        itemId = intent.getIntExtra("item_id", -1);  // 게시글의 itemId 받아오기

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        priceTextView.setText(price);
        useridTextView.setText(itemUserId);

        // 데드라인 값 표시
        if (deadline != null) {
            deadlineTextView.setText("경매 종료: " + deadline);

            // 경매 종료 날짜 처리
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // 날짜 형식 맞추기
            try {
                Date deadlineDate = dateFormat.parse(deadline);
                Date currentDate = new Date();  // 현재 날짜

                // 경매 종료 시간이 지나면 Buy 버튼 비활성화
                if (deadlineDate != null && currentDate.after(deadlineDate)) {
                    buyButton.setEnabled(false);
                    buyButton.setText("경매 종료");  // 버튼 텍스트 변경

                    // 경매 종료 메시지 표시
                    showCustomToast("경매가 종료되었습니다.");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

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
        String url = BuildConfig.BASE_URL+"/check_like_status";  // 좋아요 상태 확인을 위한 API URL
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", loggedInUserId);  // 로그인한 사용자 ID
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
                            showCustomToast("좋아요 상태 조회 실패");
                        }
                    },
                    error -> {
                        // 에러 처리
                        showCustomToast("서버 통신 실패");
                    }
            );

            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 좋아요 상태 토글 (버튼 클릭 시)
    private void toggleLikeStatus() {
        // 버튼을 클릭하자마자 비활성화
        favoriteButton.setEnabled(false);

        // 좋아요 상태에 따라 URL 설정
        String url = isLiked ? BuildConfig.BASE_URL+"/unlike" : BuildConfig.BASE_URL+"/like";

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
            data.put("user_id", loggedInUserId);  // 로그인한 사용자 ID
            data.put("item_id", itemId);  // 게시글 ID

            // Volley를 사용한 요청
            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                    response -> {
                        // 서버 응답 처리
                        String message = isLiked ? "좋아요 추가되었습니다." : "좋아요 취소되었습니다.";
                        showCustomToast( message);

                        // 서버 응답 후 버튼 활성화
                        favoriteButton.setEnabled(true);
                    },
                    error -> {
                        // 서버 오류 처리
                        showCustomToast("좋아요 처리 실패");

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
        Intent chatIntent = new Intent(PostDetailActivity.this, ChatRoomActivity.class);
        chatIntent.putExtra("auctionId", String.valueOf(itemId));  // itemId를 auctionId로 전달
        chatIntent.putExtra("price", priceTextView.getText().toString());  // 가격 정보를 전달
        startActivity(chatIntent);
    }

    // 커스텀 Toast 메서드
    // 커스텀 Toast 메서드
    public void showCustomToast(String message) {
        // 레이아웃 인플레이터를 사용하여 커스텀 레이아웃을 인플레이트
        LayoutInflater inflater = getLayoutInflater();
        View customView = inflater.inflate(R.layout.custom_toast, null);

        // 커스텀 레이아웃의 TextView와 ImageView를 찾아 메시지와 로고 설정
        TextView toastMessage = customView.findViewById(R.id.toast_message);
        toastMessage.setText(message);

        ImageView logo = customView.findViewById(R.id.toast_logo);
        logo.setImageResource(R.mipmap.sap);

        // Toast 객체 생성 및 표시
        Toast customToast = new Toast(getApplicationContext());
        customToast.setDuration(Toast.LENGTH_SHORT);  // Toast 표시 시간 설정
        customToast.setView(customView);
        customToast.show();
    }

}

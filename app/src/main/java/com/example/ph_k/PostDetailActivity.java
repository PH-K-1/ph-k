package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;  // 이미 Button을 사용하고 있으니 Button 추가는 생략
import com.google.android.material.button.MaterialButton;  // MaterialButton 추가

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private TextView titleTextView, descriptionTextView, priceTextView, useridTextView;
    private ImageView backButton, shareButton;
    private ViewPager2 viewPager;
    private MaterialButton buyButton;  // buyButton을 MaterialButton으로 초기화
    private List<String> imageUrls = new ArrayList<>();

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
        viewPager = findViewById(R.id.viewPager);
        useridTextView = findViewById(R.id.useridTextView);
        buyButton = findViewById(R.id.buyButton);  // buyButton 초기화

        // 뒤로가기 버튼 클릭 리스너 설정
        backButton.setOnClickListener(v -> finish());

        // 공유하기 버튼 클릭 리스너 설정
        shareButton.setOnClickListener(v -> {
            Intent intent = getIntent();
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String price = intent.getStringExtra("price");
            String userId = intent.getStringExtra("item_Userid");

            String sharedUrl = "https://example.com/post?title=" + title;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing post: " + sharedUrl);
            startActivity(Intent.createChooser(shareIntent, "Share using"));
        });

        // 게시글 세부 정보 표시
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String price = intent.getStringExtra("price");
        String userId = intent.getStringExtra("item_Userid");
        ArrayList<String> imageUrlList = intent.getStringArrayListExtra("image_urls");

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        priceTextView.setText(price);
        useridTextView.setText(userId);

        if (imageUrlList != null) {
            imageUrls.addAll(imageUrlList);
        }

        // ViewPager2에 어댑터 설정
        ImageAdapter adapter = new ImageAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);

        // 경매 참여하기 버튼 클릭 리스너 설정
        buyButton.setOnClickListener(v -> {
            // 채팅방으로 이동
            String chatRoomId = "exampleChatRoomId";  // 채팅방 ID는 실제로 API 호출 후 얻어야 함

            Intent chatIntent = new Intent(PostDetailActivity.this, ChatRoomActivity.class);
            chatIntent.putExtra("chatRoomId", chatRoomId);  // 채팅방 ID 전달
            startActivity(chatIntent);
        });
    }
}

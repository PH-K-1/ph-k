package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PostDetailActivity extends AppCompatActivity {
    private TextView titleTextView, descriptionTextView, priceTextView;
    private ImageView itemImageView, backButton, shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // UI 요소들 초기화
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        priceTextView = findViewById(R.id.priceTextView);
        itemImageView = findViewById(R.id.itemImageView);
        backButton = findViewById(R.id.backButton);  // 뒤로가기 버튼
        shareButton = findViewById(R.id.shareButton); // 공유하기 버튼

        // 뒤로가기 버튼 클릭 리스너 설정
        backButton.setOnClickListener(v -> finish());

        // 공유하기 버튼 클릭 리스너 설정
        shareButton.setOnClickListener(v -> {
            // 게시글 세부 정보 가져오기
            Intent intent = getIntent();
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String price = intent.getStringExtra("price");
            String imageUrl = intent.getStringExtra("image_url");

            // 공유할 URL 생성 (여기서는 예시로 게시글 URL을 만듦)
            String sharedUrl = "https://example.com/post?title=" + title;

            // 공유 Intent 생성
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
        String imageUrl = intent.getStringExtra("image_url");

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        priceTextView.setText(price);

        Glide.with(this).load(imageUrl).into(itemImageView);
    }
}

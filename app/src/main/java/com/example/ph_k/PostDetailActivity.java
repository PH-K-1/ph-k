package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private TextView titleTextView, descriptionTextView, priceTextView ,useridTextView;
    private ImageView backButton, shareButton;
    private ViewPager2 viewPager; // ViewPager2 추가
    private List<String> imageUrls = new ArrayList<>(); // 이미지 URL 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // UI 요소들 초기화
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        priceTextView = findViewById(R.id.priceTextView);
        backButton = findViewById(R.id.backButton);  // 뒤로가기 버튼
        shareButton = findViewById(R.id.shareButton); // 공유하기 버튼
        viewPager = findViewById(R.id.viewPager); // ViewPager2 초기화
        useridTextView = findViewById(R.id.useridTextView);

        // 뒤로가기 버튼 클릭 리스너 설정
        backButton.setOnClickListener(v -> finish());

        // 공유하기 버튼 클릭 리스너 설정
        shareButton.setOnClickListener(v -> {
            // 게시글 세부 정보 가져오기
            Intent intent = getIntent();
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String price = intent.getStringExtra("price");
            String Userid = intent.getStringExtra("item_Userid");

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
        String Userid = intent.getStringExtra("item_Userid");
        ArrayList<String> imageUrlList = intent.getStringArrayListExtra("image_urls"); // 여러 이미지 URL 리스트

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        priceTextView.setText(price);
        useridTextView.setText(Userid);

        // 이미지 URL 리스트가 null이 아니면 ViewPager2에 데이터 설정
        if (imageUrlList != null) {
            imageUrls.addAll(imageUrlList);
        }

        // ViewPager2에 어댑터 설정
        ImageAdapter adapter = new ImageAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
    }
}
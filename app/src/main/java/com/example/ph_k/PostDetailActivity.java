package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class PostDetailActivity extends AppCompatActivity {
    private TextView titleTextView, descriptionTextView, priceTextView;
    private ImageView itemImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // UI 요소들 초기화
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        priceTextView = findViewById(R.id.priceTextView);
        itemImageView = findViewById(R.id.itemImageView);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 뒤로가기 버튼 활성화
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    // 뒤로가기 버튼 클릭 처리
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // 뒤로가기 버튼 클릭 시 Activity 종료
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

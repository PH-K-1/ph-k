package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    private TextView titleEditText, descriptionEditText, priceTextView, userIdTextView;
    private ImageView backButton, shareButton;
    private Button saveButton;
    private ViewPager2 viewPager;
    private List<String> imageUrls = new ArrayList<>();
    private int postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        titleEditText = findViewById(R.id.titleTextView);
        descriptionEditText = findViewById(R.id.descriptionTextView);
        priceTextView = findViewById(R.id.priceTextView);
        userIdTextView = findViewById(R.id.useridTextView);
        backButton = findViewById(R.id.backButton);
        shareButton = findViewById(R.id.shareButton);
        saveButton = findViewById(R.id.saveButton);
        viewPager = findViewById(R.id.viewPager);

        backButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> savePost());

        Intent intent = getIntent();
        postId = intent.getIntExtra("item_id", -1);
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String price = intent.getStringExtra("price");
        String userId = intent.getStringExtra("item_Userid");
        ArrayList<String> imageUrlList = intent.getStringArrayListExtra("image_urls");

        titleEditText.setText(title);
        descriptionEditText.setText(description);
        priceTextView.setText(price);

        if (userId != null && !userId.isEmpty()) {
            userIdTextView.setText(userId);
        } else {
            userIdTextView.setText("작성자 정보 없음");
        }

        if (imageUrlList != null) {
            imageUrls.addAll(imageUrlList);
        }

        ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(imageUrls);
        viewPager.setAdapter(imageSliderAdapter);
    }

    private void savePost() {
        String updatedTitle = titleEditText.getText().toString();
        String updatedDescription = descriptionEditText.getText().toString();
        String updatedPrice = priceTextView.getText().toString();
        String updatedUserId = userIdTextView.getText().toString();

        if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {
            Toast.makeText(this, "제목과 내용은 필수 입력 사항입니다.", Toast.LENGTH_SHORT).show();
        } else {
            PostData postData = new PostData(postId, updatedTitle, updatedDescription, updatedPrice, imageUrls, updatedUserId);

            ApiService apiService = RetrofitClient.getApiService();
            Call<PostData> call = apiService.updatePost(postId, postData);

            call.enqueue(new Callback<PostData>() {
                @Override
                public void onResponse(Call<PostData> call, Response<PostData> response) {
                    if (response.isSuccessful()) {
                        // 수정된 데이터를 화면에 즉시 반영
                        titleEditText.setText(updatedTitle);
                        descriptionEditText.setText(updatedDescription);
                        priceTextView.setText(updatedPrice);
                        userIdTextView.setText(updatedUserId);

                        Toast.makeText(EditActivity.this, "게시글이 수정되었습니다.", Toast.LENGTH_SHORT).show();

                        // 수정 완료 후 MyListActivity로 이동
                        Intent intent = new Intent(EditActivity.this, MylistActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // MyListActivity 위의 모든 액티비티 종료
                        startActivity(intent);
                        finish();  // 현재 EditActivity 종료
                    } else {
                        Toast.makeText(EditActivity.this, "게시글 수정 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PostData> call, Throwable t) {
                    Toast.makeText(EditActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {
        private List<String> imageUrls;

        public ImageSliderAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrls.get(position))
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}
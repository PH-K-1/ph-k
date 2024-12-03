package com.example.ph_k;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private Context context;
    private List<Item> itemList;

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_layout.xml을 인플레이트하여 ViewHolder에 전달
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.title.setText(item.getTitle());
        holder.price.setText(item.getPrice() + "원");

        // 서버에서 반환된 이미지 경로들을 전체 URL로 변환
        List<String> imageUrls = item.getImageUrls();  // 여러 이미지 URL을 가져옴

        // 첫 번째 이미지를 기본적으로 표시 (필요에 따라 수정 가능)
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String firstImageUrl = imageUrls.get(0); // 첫 번째 이미지 URL
            Glide.with(context)
                    .load(firstImageUrl) // 이미지를 로드
                    .error(R.drawable.mypage) // 에러 시 기본 이미지 표시
                    .into(holder.image);
        }

        // 아이템 클릭 시 상세보기 화면으로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("item_id", item.getId());  // ID 전달
            intent.putExtra("title", item.getTitle()); // 제목 전달
            intent.putExtra("description", item.getDescription()); // 설명 전달
            intent.putExtra("price", item.getPrice() + "원"); // 가격 전달
            intent.putStringArrayListExtra("image_urls", new ArrayList<>(imageUrls)); // 여러 이미지 URL 전달
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.itemTitle);  // 제목
            price = itemView.findViewById(R.id.itemPrice);  // 가격
            image = itemView.findViewById(R.id.itemImage);  // 이미지
        }
    }
}

package com.example.ph_k;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
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

        // 금액 포맷팅 (쉼표 추가)
        String formattedPrice = formatPrice(item.getPrice());
        holder.price.setText(formattedPrice + "원");

        // 서버에서 반환된 이미지 경로들을 전체 URL로 변환
        List<String> imageUrls = item.getImageUrls();  // 여러 이미지 URL을 가져옴

        // 첫 번째 이미지를 기본적으로 표시 (필요에 따라 수정 가능)
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String firstImageUrl = imageUrls.get(0); // 첫 번째 이미지 URL
            Glide.with(context)
                    .load(firstImageUrl) // 이미지를 로드
                    .apply(RequestOptions.bitmapTransform(new ExifTransformation())) // EXIF 회전 적용
                    .error(R.drawable.mypage) // 에러 시 기본 이미지 표시
                    .into(holder.image);
        }

        // 아이템 클릭 시 상세보기 화면으로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("item_Userid", item.getUserId());  // ID 전달
            intent.putExtra("item_id", item.getId());  // ID 전달
            intent.putExtra("title", item.getTitle()); // 제목 전달
            intent.putExtra("description", item.getDescription()); // 설명 전달
            intent.putExtra("price", formattedPrice + "원"); // 가격 전달
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

    // 금액을 쉼표를 포함하여 포맷팅하는 메서드
    private String formatPrice(String price) {
        try {
            // 금액을 숫자로 변환
            long priceValue = Long.parseLong(price);

            // 금액을 쉼표 포함하여 포맷팅
            DecimalFormat formatter = new DecimalFormat("#,###");
            return formatter.format(priceValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return price;  // 예외 처리 시 원래 값을 그대로 반환
        }
    }

    public class ExifTransformation extends BitmapTransformation {

        @Override
        public Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            int rotation = getExifOrientation(toTransform);
            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);  // 회전 각도 적용
                return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
            }
            return toTransform;  // 회전하지 않음
        }

        private int getExifOrientation(Bitmap bitmap) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                byte[] byteArray = out.toByteArray();
                InputStream inputStream = new ByteArrayInputStream(byteArray);

                ExifInterface exif = new ExifInterface(inputStream);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90: // 세로 사진을 90도 회전
                        return 90;
                    case ExifInterface.ORIENTATION_ROTATE_180: // 180도 회전
                        return 180;
                    case ExifInterface.ORIENTATION_ROTATE_270: // 270도 회전
                        return 270;
                    default:
                        return 0;  // 회전하지 않음
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;  // 회전하지 않음
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update("ExifTransformation".getBytes(CHARSET));
        }
    }


}

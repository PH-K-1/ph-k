package com.example.ph_k;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private Context context;
    private List<Item> itemList;
    private String loggedInUserId;
    private Handler handler = new Handler(); // 카운트다운 갱신용 Handler
    private Runnable runnable;

    public ItemAdapter(Context context, List<Item> itemList, String loggedInUserId) {
        this.context = context;
        this.itemList = itemList;
        this.loggedInUserId = loggedInUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.title.setText(item.getTitle());

        String formattedPrice = formatPrice(item.getPrice());
        holder.price.setText(formattedPrice + "원");

        // 사용자가 입력한 deadline 값 사용
        String deadline = item.getDeadline();
        if ("없음".equals(deadline)) {
            holder.deadline.setText("종료: 없음");
        } else {
            // 카운트다운 시작
            startCountdown(holder, deadline);
        }

        List<String> imageUrls = item.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String firstImageUrl = imageUrls.get(0);
            Glide.with(context)
                    .load(firstImageUrl)
                    .error(R.drawable.mypage)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("item_Userid", item.getUserId());
            intent.putExtra("item_id", item.getId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("price", formattedPrice + "원");
            intent.putStringArrayListExtra("image_urls", new ArrayList<>(imageUrls));
            context.startActivity(intent);
        });

        // 메뉴 버튼 투명 처리 및 비활성화 로직
        if (context instanceof MylikeActivity || context instanceof BoardActivity) {
            holder.menuButton.setAlpha(0f); // 투명 처리
            holder.menuButton.setEnabled(false); // 클릭 비활성화
        } else {
            if (!item.getUserId().equals(loggedInUserId)) {
                holder.menuButton.setAlpha(0f); // 투명 처리
                holder.menuButton.setEnabled(false); // 클릭 비활성화
            } else {
                holder.menuButton.setAlpha(1f); // 보이도록 설정
                holder.menuButton.setEnabled(true); // 클릭 활성화
                holder.menuButton.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(context, holder.menuButton);
                    popupMenu.inflate(R.menu.item_menu);
                    popupMenu.setOnMenuItemClickListener(menuItem -> {
                        int itemId = menuItem.getItemId();
                        if (itemId == R.id.edit) {
                            editItem(item);
                            return true;
                        } else if (itemId == R.id.delete) {
                            deleteItem(item, position);
                            return true;
                        }
                        return false;
                    });
                    popupMenu.show();
                });
            }
        }
    }



    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, deadline; // 마감일을 표시할 TextView 추가
        ImageView image;
        ImageButton menuButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.itemTitle);
            price = itemView.findViewById(R.id.itemPrice);
            deadline = itemView.findViewById(R.id.itemDeadline); // 마감일 TextView 연결
            image = itemView.findViewById(R.id.itemImage);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }

    private String formatPrice(String price) {
        try {
            long priceValue = Long.parseLong(price);
            DecimalFormat formatter = new DecimalFormat("#,###");
            return formatter.format(priceValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return price;
        }
    }

    private void startCountdown(ViewHolder holder, String deadline) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date deadlineDate = sdf.parse(deadline);
            if (deadlineDate == null) {
                holder.deadline.setText("종료: 없음");
                return;
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    long remainingTime = deadlineDate.getTime() - System.currentTimeMillis();
                    if (remainingTime > 0) {
                        String formattedTime = formatTime(remainingTime);
                        holder.deadline.setText("경매 종료: " + formattedTime);
                        handler.postDelayed(this, 1000); // 1초마다 업데이트
                    } else {
                        holder.deadline.setText("종료됨");
                        handler.removeCallbacks(this); // 타이머 종료
                    }
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
            holder.deadline.setText("종료: 없음");
        }
    }

    private String formatTime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        if (days > 0) {
            return days + "일 " + hours + "시간 " + minutes + "분 " + seconds + "초";
        } else if (hours > 0) {
            return hours + "시간 " + minutes + "분 " + seconds + "초";
        } else if (minutes > 0) {
            return minutes + "분 " + seconds + "초";
        } else {
            return seconds + "초";
        }
    }

    private void editItem(Item item) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra("item_id", item.getId());
        intent.putExtra("title", item.getTitle());
        intent.putExtra("description", item.getDescription());
        intent.putExtra("price", item.getPrice());
        intent.putStringArrayListExtra("image_urls", new ArrayList<>(item.getImageUrls()));
        intent.putExtra("item_Userid", item.getUserId());
        intent.putExtra("deadline", item.getDeadline()); // deadline 값 추가
        context.startActivity(intent);
    }

    private void deleteItem(Item item, int position) {
        deleteItemFromServer(String.valueOf(item.getId()), new OnDeleteItemListener() {
            @Override
            public void onDeleteSuccess() {
                itemList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteFailure() {
                Toast.makeText(context, "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteItemFromServer(String itemId, OnDeleteItemListener listener) {
        ApiService apiService = RetrofitClient.getApiService(); // RetrofitClient에서 ApiService 인스턴스를 가져옴
        Call<Void> call = apiService.deletePost(itemId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    listener.onDeleteSuccess();
                } else {
                    listener.onDeleteFailure();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                listener.onDeleteFailure();
            }
        });
    }

    public interface OnDeleteItemListener {
        void onDeleteSuccess();
        void onDeleteFailure();
    }
}
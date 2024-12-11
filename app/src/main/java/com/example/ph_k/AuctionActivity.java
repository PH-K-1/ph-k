package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuctionActivity extends AppCompatActivity {

    private Button buttonPlaceBid;
    private EditText editBidAmount;
    private String auctionId = "경매 아이디";  // 실제 경매 ID로 변경

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction);

        buttonPlaceBid = findViewById(R.id.button_place_bid);
        editBidAmount = findViewById(R.id.edit_bid_amount);

        buttonPlaceBid.setOnClickListener(v -> {
            String bidAmount = editBidAmount.getText().toString();
            if (!bidAmount.isEmpty()) {
                createChatRoom(bidAmount);  // 채팅방 생성 함수 호출
            } else {
                Toast.makeText(AuctionActivity.this, "입찰 금액을 입력해주세요", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 채팅방 생성 요청
    private void createChatRoom(String bidAmount) {
        // Retrofit API를 사용하여 채팅방 생성 요청
        ApiService apiService = RetrofitClient.getApiService();

        // 경매 ID와 입찰 금액을 요청에 포함
        CreateChatRoomRequest request = new CreateChatRoomRequest(auctionId, bidAmount);

        apiService.createChatRoom(request).enqueue(new Callback<CreateChatRoomResponse>() {
            @Override
            public void onResponse(Call<CreateChatRoomResponse> call, Response<CreateChatRoomResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String chatRoomId = response.body().getChatRoomId();
                    // 채팅방 ID가 반환되면 ChatRoomActivity로 이동
                    Intent intent = new Intent(AuctionActivity.this, ChatRoomActivity.class);
                    intent.putExtra("chatRoomId", chatRoomId);  // 채팅방 ID를 전달
                    startActivity(intent);
                } else {
                    Toast.makeText(AuctionActivity.this, "채팅방 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateChatRoomResponse> call, Throwable t) {
                Toast.makeText(AuctionActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

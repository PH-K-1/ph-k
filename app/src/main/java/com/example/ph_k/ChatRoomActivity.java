package com.example.ph_k;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatRoomActivity extends AppCompatActivity {

    private TextView textChatRoomId, textHighestBid, textCurrentPrice;
    private EditText editTextMessage;
    private Button buttonSendMessage;
    private LinearLayout chatMessageArea; // 채팅 메시지 표시 영역
    private Socket mSocket;

    private String auctionId; // 클래스 필드로 선언
    private String username; // 클래스 필드로 선언



    private static final String URL_CHAT_MESSAGES = "http://203.250.133.110:7310/get_chat_messages";
    private static final String SOCKET_URL = "http://203.250.133.110:7310";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // UI 요소 초기화
        textChatRoomId = findViewById(R.id.text_chat_room_id);
        textHighestBid = findViewById(R.id.text_highest_bid);
        textCurrentPrice = findViewById(R.id.pricetextview);
        editTextMessage = findViewById(R.id.edit_text_message);
        buttonSendMessage = findViewById(R.id.btn_send_message);
        chatMessageArea = findViewById(R.id.chat_message_area);

        auctionId = getIntent().getStringExtra("auctionId");
        String price = getIntent().getStringExtra("price");

        // 채팅방 ID와 가격 표시
        textChatRoomId.setText("경매 ID: " + auctionId);
        textCurrentPrice.setText("현재 금액: " + price);

        // 사용자 이름 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        if (auctionId == null || auctionId.isEmpty()) {
            Toast.makeText(this, "경매 ID를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 기존 메시지 불러오기
        loadChatMessages(auctionId);

        // Socket.IO 연결
        setupSocketConnection(username, auctionId);

        // 메시지 전송 버튼 클릭 이벤트
        buttonSendMessage.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message, username, auctionId, price);
            }
        });
    }

    private void loadChatMessages(String auctionId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = URL_CHAT_MESSAGES + "?auction_id=" + auctionId;

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        // 최고 입찰가 및 입찰자 정보 업데이트
                        int highestBid = jsonResponse.getInt("highest_bid");
                        String highestBidder = jsonResponse.optString("highest_bidder", "없음");

                        String formattedHighestBid = String.format("%,d", highestBid);
                        textHighestBid.setText("최고 입찰가: " + formattedHighestBid + "원 (" + highestBidder + ")");

                        // 채팅 메시지 로드
                        JSONArray messages = jsonResponse.getJSONArray("messages");
                        chatMessageArea.removeAllViews(); // 기존 메시지 초기화
                        for (int i = 0; i < messages.length(); i++) {
                            JSONObject messageData = messages.getJSONObject(i);
                            String userId = messageData.getString("user_id");
                            String message = messageData.getString("message");
                            String createdAt = messageData.getString("created_at");

                            // UI에 메시지 추가
                            TextView newMessage = new TextView(this);
                            newMessage.setText(userId + ": " + message + " (" + createdAt + ")");
                            chatMessageArea.addView(newMessage);
                        }

                        // 사용자가 입력한 입찰가와 비교
                        buttonSendMessage.setOnClickListener(v -> {
                            String message = editTextMessage.getText().toString().trim();
                            int bidAmount = 0;

                            try {
                                bidAmount = Integer.parseInt(message); // 입찰가가 숫자인지 확인
                            } catch (NumberFormatException e) {
                                // 숫자가 아니면 일반 메시지로 처리
                                return;
                            }

                            // 입찰가가 최고 입찰가보다 작으면 채팅을 보내지 않음
                            if (bidAmount <= highestBid) {
                                Toast.makeText(ChatRoomActivity.this, "입찰가는 최고 입찰가보다 커야 합니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                sendMessage(message, username, auctionId, String.valueOf(highestBid));
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "채팅 메시지 로드 오류", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // 500 에러 처리
                    if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                        Toast.makeText(this, "서버 오류가 발생했습니다. 나중에 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "채팅 메시지를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                    error.printStackTrace();
                });

        requestQueue.add(stringRequest);
    }





    private void setupSocketConnection(String username, String auctionId) {
        try {
            mSocket = IO.socket(SOCKET_URL);
            mSocket.connect();
            Log.d("ChatRoomActivity", "Joining room with auction_id: " + auctionId + " and username: " + username);

            // 채팅방 참가 데이터 구성
            JSONObject joinData = new JSONObject();
            joinData.put("auction_id", auctionId);  // auction_id 사용
            joinData.put("username", username);

            // 서버로 join 이벤트 전송
            mSocket.emit("join", joinData);

            // 새로운 메시지 수신 처리
            mSocket.on("message", onNewMessage);

            // 입찰가 업데이트 수신 처리
            mSocket.on("update_bid", onBidUpdate); // 입찰가 업데이트 이벤트 처리
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 새로운 메시지 수신 처리
    private final Emitter.Listener onNewMessage = args -> runOnUiThread(() -> {
        try {
            JSONObject data = (JSONObject) args[0];
            String userId = data.getString("user_id");
            String message = data.getString("message");
            String createdAt = data.getString("created_at");

            // 메시지를 UI에 추가
            TextView newMessage = new TextView(this);
            newMessage.setText(userId + ": " + message + " (" + createdAt + ")");
            chatMessageArea.addView(newMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    // 입찰가 업데이트를 UI에 반영
    private final Emitter.Listener onBidUpdate = args -> runOnUiThread(() -> {
        try {
            JSONObject data = (JSONObject) args[0];
            int highestBid = data.getInt("highest_bid");
            String highestBidder = data.getString("highest_bidder");

            // 최고 입찰가 UI 업데이트
            textHighestBid.setText("최고 입찰가: " + highestBid + "원 (" + highestBidder + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    private void sendMessage(String message, String username, String auctionId, String price) {
        try {
            JSONObject messageData = new JSONObject();
            messageData.put("user_id", username);
            messageData.put("auction_id", auctionId);
            messageData.put("message", message);
            messageData.put("price", price);  // 가격 정보 추가

            int bidAmount = 0;
            try {
                bidAmount = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                // 숫자가 아닌 메시지일 경우 그대로 채팅 메시지로 전송
            }

            if (bidAmount > 0) {
                mSocket.emit("new_bid", messageData);

                TextView newMessage = new TextView(this);
                newMessage.setText(username + ": " + bidAmount + "원 (" + username + ")");
                chatMessageArea.addView(newMessage);
            } else {
                mSocket.emit("new_message", messageData);

                TextView newMessage = new TextView(this);
                newMessage.setText(username + ": " + message);
                chatMessageArea.addView(newMessage);
            }

            // 입력 필드 초기화
            editTextMessage.setText("");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            try {
                // leaveData에 정확한 값이 들어갔는지 확인
                JSONObject leaveData = new JSONObject();
                leaveData.put("auction_id", auctionId);  // 채팅방 ID
                leaveData.put("username", username);     // 사용자 이름

                // 서버로 leave 이벤트 전송
                mSocket.emit("leave", leaveData);
                mSocket.disconnect();
                mSocket.off("message", onNewMessage);
                mSocket.off("update_bid", onBidUpdate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
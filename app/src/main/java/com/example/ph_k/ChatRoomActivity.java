package com.example.ph_k;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatRoomActivity extends AppCompatActivity {

    private TextView textChatRoomId, textHighestBid;
    private EditText editTextMessage;
    private Button buttonSendMessage;
    private Button buttonParticipateAuction;

    private Socket mSocket;
    private String chatRoomId;

    // 채팅 메시지 수신 리스너
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String message = data.optString("message");
            String username = data.optString("username");

            runOnUiThread(() -> {
                TextView newMessage = new TextView(ChatRoomActivity.this);
                newMessage.setText(username + ": " + message);
                ((LinearLayout) findViewById(R.id.chat_message_area)).addView(newMessage);
            });
        }
    };

    // 입찰 갱신 리스너
    private Emitter.Listener onBidUpdate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            int highestBid = data.optInt("highest_bid");
            String highestBidder = data.optString("highest_bidder");

            runOnUiThread(() -> {
                textHighestBid.setText("최고 입찰가: " + highestBid + "원 (" + highestBidder + ")");
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        textChatRoomId = findViewById(R.id.text_chat_room_id);
        textHighestBid = findViewById(R.id.text_highest_bid); // 최고 입찰가 텍스트 뷰
        editTextMessage = findViewById(R.id.edit_text_message);
        buttonSendMessage = findViewById(R.id.btn_send_message);
        buttonParticipateAuction = findViewById(R.id.btn_participate_auction);

        String auctionId = getIntent().getStringExtra("auctionId");
        if (auctionId == null || auctionId.isEmpty()) {
            Toast.makeText(this, "경매 ID를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        textChatRoomId.setText("경매 ID: " + auctionId);

        chatRoomId = getIntent().getStringExtra("chatRoomId");
        if (chatRoomId != null) {
            textChatRoomId.setText("채팅방 ID: " + chatRoomId);
        } else {
            createOrGetChatRoom(auctionId);
        }

        try {
            mSocket = IO.socket(BuildConfig.BASE_URL);
            mSocket.connect();

            // 채팅방에 참여
            JSONObject joinData = new JSONObject();
            joinData.put("room", chatRoomId);
            joinData.put("username", "user1");
            mSocket.emit("join", joinData);

            // 리스너 등록
            mSocket.on("message", onNewMessage);
            mSocket.on("update_bid", onBidUpdate);

        } catch (URISyntaxException | JSONException e) {
            e.printStackTrace();
        }

        buttonSendMessage.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });

        buttonParticipateAuction.setOnClickListener(v -> {
            if (chatRoomId == null || chatRoomId.isEmpty()) {
                Toast.makeText(this, "채팅방 ID를 가져오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "이미 채팅방에 참여 중입니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrGetChatRoom(String auctionId) {
        try {
            JSONObject requestData = new JSONObject();
            requestData.put("auction_id", auctionId);

            String url = BuildConfig.BASE_URL + "/create_or_get_chat_room";

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    com.android.volley.Request.Method.POST, url, requestData,
                    response -> {
                        try {
                            chatRoomId = response.getString("chat_room_id");
                            textChatRoomId.setText("채팅방 ID: " + chatRoomId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ChatRoomActivity.this, "채팅방 생성 실패", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(ChatRoomActivity.this, "채팅방 생성 요청 실패", Toast.LENGTH_SHORT).show();
                    }
            );

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "요청 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String message) {
        try {
            JSONObject data = new JSONObject();
            data.put("room", chatRoomId);
            data.put("username", "user1");
            data.put("message", message);

            mSocket.emit("message", data);

            TextView newMessage = new TextView(this);
            newMessage.setText("나: " + message);
            ((LinearLayout) findViewById(R.id.chat_message_area)).addView(newMessage);
            editTextMessage.setText("");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.emit("leave", new JSONObject());
            mSocket.disconnect();
            mSocket.off("message", onNewMessage);
            mSocket.off("update_bid", onBidUpdate);
        }
    }
}

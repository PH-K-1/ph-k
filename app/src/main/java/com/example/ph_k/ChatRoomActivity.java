package com.example.ph_k;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.widget.LinearLayout;  // LinearLayout import 추가

import androidx.appcompat.app.AppCompatActivity;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class ChatRoomActivity extends AppCompatActivity {

    private TextView textChatRoomId;
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

            // 메시지 화면에 표시
            runOnUiThread(() -> {
                // 메시지를 동적으로 추가
                TextView newMessage = new TextView(ChatRoomActivity.this);
                newMessage.setText(username + ": " + message);
                ((LinearLayout) findViewById(R.id.chat_message_area)).addView(newMessage);
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        textChatRoomId = findViewById(R.id.text_chat_room_id);
        editTextMessage = findViewById(R.id.edit_text_message);
        buttonSendMessage = findViewById(R.id.btn_send_message);
        buttonParticipateAuction = findViewById(R.id.btn_participate_auction);

        // 채팅방 ID를 받아옵니다
        chatRoomId = getIntent().getStringExtra("chatRoomId");
        if (chatRoomId != null) {
            // 채팅방 ID를 화면에 표시
            textChatRoomId.setText("채팅방 ID: " + chatRoomId);
        } else {
            textChatRoomId.setText("채팅방 ID를 가져올 수 없습니다.");
        }

        // Socket.IO 클라이언트 설정
        try {
            mSocket = IO.socket("http://192.168.200.114:7310"); // 서버 주소를 입력하세요
            mSocket.connect();

            // 채팅방에 참여
            JSONObject joinData = new JSONObject();
            try {
                joinData.put("room", chatRoomId);
                joinData.put("username", "user1");  // 예시로 "user1"으로 설정, 실제로는 로그인된 사용자 정보로 설정
                mSocket.emit("join", joinData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 메시지 수신
            mSocket.on("message", onNewMessage);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // 메시지 전송 버튼 클릭 시
        buttonSendMessage.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });

        // 경매 참여 버튼 클릭 시
        buttonParticipateAuction.setOnClickListener(v -> {
            // 경매 참여 관련 로직 구현
            Toast.makeText(ChatRoomActivity.this, "경매에 참여하였습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    // 메시지 서버로 전송
    private void sendMessage(String message) {
        try {
            JSONObject data = new JSONObject();
            data.put("room", chatRoomId);
            data.put("username", "user1");  // 예시로 "user1"으로 설정, 실제로는 로그인된 사용자 정보로 설정
            data.put("message", message);

            mSocket.emit("message", data);

            // 전송된 메시지는 화면에 즉시 추가
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
        // 채팅방 나가기
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off("message", onNewMessage);
        }
    }
}
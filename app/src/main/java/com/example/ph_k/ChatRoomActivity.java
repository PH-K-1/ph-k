package com.example.ph_k;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageView sendButton;
    private TextView roomNameTextView;
    private List<String> messages = new ArrayList<>();
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room); // activity_chat_room.xml 레이아웃 사용

        // 채팅방 이름 표시
        roomNameTextView = findViewById(R.id.textView333); // 텍스트 뷰 ID 수정
        Intent intent = getIntent();
        String roomName = intent.getStringExtra("ROOM_NAME");
        roomNameTextView.setText(roomName);

        // RecyclerView 설정
        recyclerView = findViewById(R.id.chattingRecyclerView); // RecyclerView ID 수정
        messageInput = findViewById(R.id.editText); // EditText ID 수정
        sendButton = findViewById(R.id.sendbutton); // 버튼 ID 수정

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messages);
        recyclerView.setAdapter(chatAdapter);

        // 메시지 전송 버튼 클릭 이벤트 처리
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMessage = messageInput.getText().toString();
                if (!newMessage.isEmpty()) {
                    messages.add(newMessage);  // 새 메시지 리스트에 추가
                    chatAdapter.notifyItemInserted(messages.size() - 1); // 새 메시지 추가 알림
                    messageInput.setText(""); // 입력란 초기화
                    recyclerView.scrollToPosition(messages.size() - 1); // 마지막 메시지로 스크롤
                }
            }
        });
    }
}

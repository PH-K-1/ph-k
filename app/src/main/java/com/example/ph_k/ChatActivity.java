package com.example.ph_k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chattingRoomRecyclerView;
    private ChatAdapter chatAdapter;
    private List<String> chatRoomNames = new ArrayList<>(); // 채팅방 이름을 위한 리스트
    private List<String> chatMessages = new ArrayList<>(); // 채팅방 내 메시지
    private EditText messageInput;
    private ImageView sendButton;
    private TextView afterLoginTextView; // 로그인 후 메시지

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); // activity_chat.xml 레이아웃 사용

        // SharedPreferences에서 로그인 상태 확인
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        // 로그인 상태에 따른 메시지 표시
        afterLoginTextView = findViewById(R.id.afterLoginTextView);
        if (isLoggedIn) {
            afterLoginTextView.setVisibility(View.GONE); // 로그인 상태라면 메시지 숨김
        } else {
            afterLoginTextView.setVisibility(View.VISIBLE); // 로그인되지 않았다면 메시지 표시
        }

        // 채팅방 목록 RecyclerView 참조
        chattingRoomRecyclerView = findViewById(R.id.chattingRoomRecyclerView);
        chattingRoomRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 채팅 어댑터 설정
        chatAdapter = new ChatAdapter(chatRoomNames); // 채팅방 이름 리스트를 어댑터에 전달
        chattingRoomRecyclerView.setAdapter(chatAdapter);

        // 샘플 채팅방 이름 추가
        chatRoomNames.add("채팅방 1");
        chatRoomNames.add("채팅방 2");
        chatRoomNames.add("채팅방 3");
        chatAdapter.notifyDataSetChanged(); // RecyclerView 갱신

        // 메시지 입력 EditText와 전송 버튼 참조
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // 메시지 전송 버튼 클릭 이벤트 처리
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMessage = messageInput.getText().toString();
                if (!newMessage.isEmpty()) {
                    chatMessages.add(newMessage);  // 새 메시지 리스트에 추가
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1); // 새 메시지 추가 알림
                    messageInput.setText(""); // 입력란 초기화
                    chattingRoomRecyclerView.scrollToPosition(chatMessages.size() - 1); // 마지막 메시지로 스크롤
                }
            }
        });

        // 채팅방 아이템 클릭 이벤트 처리
        chattingRoomRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                // 터치 이벤트가 발생한 위치를 확인
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    View childView = rv.findChildViewUnder(e.getX(), e.getY());
                    if (childView != null) {
                        int position = rv.getChildAdapterPosition(childView);
                        // 채팅방 클릭 시 ChatRoomActivity로 이동
                        Intent intent = new Intent(ChatActivity.this, ChatRoomActivity.class);

                        // 채팅방 이름 전달
                        String roomName = chatRoomNames.get(position); // 클릭된 채팅방 이름 가져오기
                        intent.putExtra("ROOM_NAME", roomName); // 채팅방 이름 전달

                        startActivity(intent); // ChatRoomActivity로 이동
                    }
                }
                return false; // 이벤트가 처리된 후 계속 전달
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                // 터치 이벤트 처리 (필요시 추가)
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                // 추가적인 처리 (필요시)
            }
        });
    }
}

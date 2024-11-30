package com.example.ph_k;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private WebSocket webSocket;
    private TextView messageDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageDisplay = findViewById(R.id.messageDisplay); // 메시지 표시 TextView

        OkHttpClient client = new OkHttpClient();
        // 서버의 퍼블릭 IP와 포트를 입력
        Request request = new Request.Builder().url("ws://192.168.55.231:7310").build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("WebSocket", "연결됨");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // 메시지를 수신했을 때
                Log.d("WebSocket", "받은 메시지: " + text);

                // UI 스레드에서 메시지 표시
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 메시지를 계속 추가하여 TextView에 설정
                        messageDisplay.append(text + "\n");
                    }
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "연결 실패", t);
            }
        });
    }

    // 버튼 클릭 시 메시지 보내기
    public void sendMessage(View view) {
        EditText messageInput = findViewById(R.id.messageInput);
        String message = messageInput.getText().toString();
        if (!message.isEmpty()) {
            // 서버에 메시지를 전송
            if (webSocket != null) {
                webSocket.send(message);
            }
            messageInput.setText("");  // 입력 필드 초기화
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
        }
    }
}

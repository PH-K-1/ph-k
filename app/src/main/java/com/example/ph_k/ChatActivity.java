package com.example.ph_k;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;
import okio.ByteString;

public class ChatActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button sendButton;
    private TextView chatTextView;
    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        chatTextView = findViewById(R.id.chatTextView);

        // WebSocket 연결
        connectWebSocket();

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (!message.isEmpty()) {
                // 서버로 메시지 전송
                webSocket.send(message);

                // 화면에 즉시 메시지 추가 (보낸 메시지)
                chatTextView.append("나: " + message + "\n");

                // 메시지 전송 후 EditText 비우기
                messageEditText.setText("");
            }
        });
    }

    private void connectWebSocket() {
        OkHttpClient client = new OkHttpClient();

        // WebSocket 서버의 URL (서버가 로컬에서 실행 중이라면 IP 주소로 변경)
        Request request = new Request.Builder().url("ws://192.168.200.114:3000").build();

        // WebSocket 연결
        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "서버와 연결되었습니다", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                // 서버에서 받은 메시지를 UI에 추가
                runOnUiThread(() -> chatTextView.append("상대: " + text + "\n"));
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "서버와의 연결이 종료되었습니다", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
            }
        });

        // WebSocket 연결이 비동기적으로 이루어지므로, 클라이언트는 연결이 완료될 때까지 기다리도록 합니다.
        client.dispatcher().executorService().shutdown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity Destroyed");
        }
    }
}

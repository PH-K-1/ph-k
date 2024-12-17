package com.example.ph_k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatRoomActivity extends AppCompatActivity {

    private TextView textChatRoomId, textHighestBid, textCurrentPrice;
    private EditText editTextMessage;
    private Button buttonSendMessage;
    private LinearLayout chatMessageArea; // 채팅 메시지 표시 영역
    private Socket mSocket;

    private String auctionId; // 클래스 필드로 선언
    private String username; // 클래스 필드로 선언

    private ArrayList<String> imageUrls;
    private ImageView imageView;

    private TextView deadlineTextView;

    private static final String URL_CHAT_MESSAGES = "http://203.250.133.110:7310/get_chat_messages";
    private static final String SOCKET_URL = "http://203.250.133.110:7310";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // itemAdapterView를 가져옴
        LinearLayout itemAdapterView = findViewById(R.id.item_adapter_view);

        // 아이템 제목과 가격 텍스트 뷰를 찾음
        TextView itemTitle = findViewById(R.id.itemTitle);
        TextView itemPrice = findViewById(R.id.itemPrice);

        // UI 요소 초기화
        textChatRoomId = findViewById(R.id.text_chat_room_id);
        textHighestBid = findViewById(R.id.text_highest_bid);
        textCurrentPrice = findViewById(R.id.pricetextview);
        editTextMessage = findViewById(R.id.edit_text_message);
        buttonSendMessage = findViewById(R.id.btn_send_message);
        chatMessageArea = findViewById(R.id.chat_message_area);
        // UI 요소들 초기화
        deadlineTextView = findViewById(R.id.itemDeadline);  // 데드라인을 표시할 TextView

        auctionId = getIntent().getStringExtra("auctionId");
        String price = getIntent().getStringExtra("price");
        String title = getIntent().getStringExtra("title"); // Intent로부터 데이터 수신
        Intent intent = getIntent();
        imageUrls = intent.getStringArrayListExtra("imageUrls");  // 이미지 URL 리스트 받기
        // Intent로 전달된 데이터 받기
        String deadline = intent.getStringExtra("deadline");  // deadline 값 받기


        // 채팅방 ID와 가격 표시
        textChatRoomId.setText("경매 ID: " + auctionId);
        textCurrentPrice.setText("시작가: " + price);

        // UI 요소들 초기화
        imageView = findViewById(R.id.itemImage);

        // 이미지 URL 리스트를 사용하여 이미지 표시 (예시로 첫 번째 이미지를 보여주는 코드)
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String firstImageUrl = imageUrls.get(0);  // 첫 번째 이미지 URL 가져오기
            // 이미지를 로드하는 코드 (예: Picasso 또는 Glide를 사용)
            Glide.with(this).load(firstImageUrl).into(imageView);
        }

        // 아이템 어댑터 메서드 호출 (받은 값 전달)
        addItemAdapter(itemAdapterView, title, price, deadline);

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
                        textHighestBid.setText("최고가: " + formattedHighestBid + "원 (" + highestBidder + ")");

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
                            newMessage.setText(message + " (" + createdAt + ")");
                            chatMessageArea.addView(newMessage);
                        }

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
            newMessage.setText(message + " (" + createdAt + ")");
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
            textHighestBid.setText("최고가: " + highestBid + "원 (" + highestBidder + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });


    private int highestBidAmount = 0;  // 최고가를 관리하는 변수

    private void sendMessage(String message, String username, String auctionId, String price) {
        try {
            int bidAmount = 0;
            int auctionStartPrice = 0;

            // 사용자 입력값인 message가 숫자인지 확인하여 bidAmount에 저장
            try {
                message = message.replaceAll("[^0-9]", "");
                bidAmount = Integer.parseInt(message);  // 입력된 메시지를 숫자로 변환
            } catch (NumberFormatException e) {
                // 숫자가 아닌 메시지일 경우 그대로 채팅 메시지로 전송
            }

            if (!message.matches("[0-9]+")) {
                Toast.makeText(this, "숫자만 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // price에서 쉼표와 "원"을 제거 후 정수로 변환
            if (price != null && !price.isEmpty()) {
                price = price.replaceAll("[^0-9]", "");
                try {
                    auctionStartPrice = Integer.parseInt(price);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "유효한 시작가가 아닙니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(this, "시작가 정보가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 입찰 금액이 시작가보다 작은 경우
            if (bidAmount <= auctionStartPrice) {
                Toast.makeText(this, "시작가보다 높은 금액을 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 입찰 금액이 최고가보다 작은 경우, 서버에서 최고가를 받아와서 확인
            getHighestBidAndCheck(bidAmount, username, auctionId, message);

            // 메시지 입력 필드 초기화
            editTextMessage.setText("");
        } catch (Exception e) {  // 다른 예외를 처리할 경우
            e.printStackTrace();
            Toast.makeText(this, "채팅 메시지 로드 오류", Toast.LENGTH_SHORT).show();
        }
    }

    private void getHighestBidAndCheck(final int bidAmount, final String username, final String auctionId, final String message) {
        // Volley 요청을 통해 서버에서 최고가 정보를 받아옴
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://203.250.133.110:7310/get_chat_messages?auction_id=" + auctionId;  // 서버 URL에 맞춰서 변경

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                response -> {
                    try {
                        // 서버 응답에서 최고가와 최고 입찰자 정보를 추출
                        JSONObject jsonResponse = new JSONObject(response);
                        int highestBid = jsonResponse.getInt("highest_bid");

                        // 최고가가 현재 입찰 금액보다 작은 경우에만 입찰 진행
                        if (bidAmount > highestBid) {
                            // 입찰 금액이 최고가보다 높은 경우
                            highestBidAmount = bidAmount; // 최고가 갱신

                            // 입찰 메시지 생성
                            JSONObject messageData = new JSONObject();
                            messageData.put("user_id", username);
                            messageData.put("auction_id", auctionId);
                            messageData.put("message", message);
                            messageData.put("price", bidAmount);  // 입찰 금액을 가격으로 전달

                            // 입찰 처리
                            mSocket.emit("new_bid", messageData);  // 서버로 입찰 메시지 전송

                            // 입찰되었습니다 토스트 메시지 추가
                            Toast.makeText(this, "입찰되었습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "현재 최고가보다 높은 금액을 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "서버에서 최고가 정보를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // 네트워크 오류 처리
                    Toast.makeText(this, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                });

        // 요청을 큐에 추가
        requestQueue.add(stringRequest);
    }


    private void addItemAdapter(LinearLayout itemAdapterView, String title, String price, String deadline) {
        // 기존에 추가된 모든 아이템을 제거
        itemAdapterView.removeAllViews();

        // 예시로 하나의 아이템을 동적으로 추가
        LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.item_view, null);

        // ID가 제대로 찾혔는지 체크
        TextView itemTitle = itemLayout.findViewById(R.id.itemTitle);
        TextView itemPrice = itemLayout.findViewById(R.id.itemPrice);
        TextView itemDeadline = itemLayout.findViewById(R.id.itemDeadline); // 데드라인 텍스트뷰 추가
        ImageView itemImage = itemLayout.findViewById(R.id.itemImage); // 이미지뷰 추가

        if (itemTitle != null && itemPrice != null && itemImage != null && itemDeadline != null) {
            // 받아온 title과 price를 아이템에 설정
            if (title != null) {
                itemTitle.setText(title); // title을 아이템 제목에 설정
            }

            if (price != null && !price.isEmpty()) {
                itemPrice.setText(price); // price를 아이템 가격에 설정
            } else {
                itemPrice.setText("가격 정보 없음"); // 가격 정보가 없으면 기본값 설정
            }

            // 이미지 URL을 가져와서 Glide로 이미지 로드
            if (imageUrls != null && !imageUrls.isEmpty()) {
                String firstImageUrl = imageUrls.get(0); // 첫 번째 이미지 URL 가져오기
                Glide.with(this).load(firstImageUrl).into(itemImage); // Glide로 이미지 로드
            }

            // 데드라인을 표시 및 카운트다운 시작
            if (deadline != null && !deadline.isEmpty()) {
                itemDeadline.setText("경매 종료일: " + deadline); // 데드라인 값 설정
                startCountdown(itemDeadline, deadline); // 카운트다운 호출
            } else {
                itemDeadline.setText("경매 종료일 없음"); // 데드라인 정보가 없으면 기본값 설정
            }

            // 아이템을 itemAdapterView에 추가
            itemAdapterView.addView(itemLayout);
        } else {
            Log.e("ChatRoomActivity", "itemTitle or itemPrice or itemImage or itemDeadline is null");
        }
    }

    private void startCountdown(TextView deadlineTextView, String deadline) {
        try {
            // deadline을 Date로 변환
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date deadlineDate = format.parse(deadline);

            if (deadlineDate != null) {
                long currentTime = System.currentTimeMillis();
                long countdownTime = deadlineDate.getTime() - currentTime; // 남은 시간 (밀리초 단위)

                // CountDownTimer 생성
                new CountDownTimer(countdownTime, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // 남은 시간 계산
                        int days = (int) (millisUntilFinished / (1000 * 60 * 60 * 24)); // 일 계산
                        int hours = (int) ((millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)); // 시간 계산
                        int minutes = (int) ((millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)); // 분 계산
                        int seconds = (int) ((millisUntilFinished % (1000 * 60)) / 1000); // 초 계산

                        // UI에 남은 시간 표시 (일, 시간, 분, 초)
                        deadlineTextView.setText(String.format("남은 시간: %d일 %02d시간 %02d분 %02d초", days, hours, minutes, seconds));
                    }

                    @Override
                    public void onFinish() {
                        // 시간이 끝나면 "종료"로 변경
                        deadlineTextView.setText("종료");

                        // 최고 입찰 정보를 가져와 사용자에게 알림
                        String highestBidText = "최고 입찰자 정보"; // 실제 최고 입찰 정보 텍스트뷰에서 가져오도록 수정
                        Toast.makeText(ChatRoomActivity.this, highestBidText + "으로 낙찰되었습니다.", Toast.LENGTH_LONG).show();

                        // BoardActivity로 화면 전환
                        Intent intent = new Intent(ChatRoomActivity.this, BoardActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }.start(); // 카운트다운 시작
            }
        } catch (ParseException e) {
            e.printStackTrace();
            deadlineTextView.setText("잘못된 데드라인 형식");
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

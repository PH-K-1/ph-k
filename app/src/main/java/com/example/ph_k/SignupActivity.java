package com.example.ph_k;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.signup_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        String url = "http://192.168.55.231:7310/signup";  // 서버 URL

        // JSON 객체 생성
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("username", username);
            jsonParams.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // 서버에서 반환된 메시지 처리
                            String message = response.getString("message");
                            if (message.equals("아이디가 이미 존재합니다.")) {
                                // 아이디 중복 메시지 처리
                                showCustomToast("아이디가 이미 존재합니다.");
                            } else if (message.equals("회원가입 성공")) {
                                // 회원가입 성공 메시지 처리
                                showCustomToast("회원가입에 성공했습니다.");
                                finish();  // 회원가입 성공 후 로그인 화면으로 돌아가기
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showCustomToast("서버 오류");
                        }
                    }
                },
                error -> {
                    // 400 상태 코드 처리
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        try {
                            // 서버에서 반환한 오류 메시지 처리
                            String errorMessage = new String(error.networkResponse.data, "UTF-8");
                            JSONObject errorJson = new JSONObject(errorMessage);
                            String message = errorJson.getString("message");
                            showCustomToast(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showCustomToast("오류 발생");
                        }
                    } else {
                        // 네트워크 오류 처리
                        showCustomToast("네트워크 오류");
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Volley 큐에 요청 추가
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    // 커스텀 Toast 메서드
    public void showCustomToast(String message) {
        // 레이아웃 인플레이터를 사용하여 커스텀 레이아웃을 인플레이트
        LayoutInflater inflater = getLayoutInflater();
        View customView = inflater.inflate(R.layout.custom_toast, null);

        // 커스텀 레이아웃의 TextView를 찾아 메시지 설정
        TextView toastMessage = customView.findViewById(R.id.toast_message);
        toastMessage.setText(message);

        // Toast 객체 생성 및 표시
        Toast customToast = new Toast(getApplicationContext());
        customToast.setDuration(Toast.LENGTH_SHORT);  // Toast 표시 시간 설정
        customToast.setView(customView);
        customToast.show();
    }
}

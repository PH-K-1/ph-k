package com.example.ph_k;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
    }

    public void onLoginClick(View view) {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            showCustomToast("아이디와 비밀번호를 입력하세요.");
            return;
        }

        // 로그인 요청 객체 생성
        LoginRequest loginRequest = new LoginRequest(username, password);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // 로그인 API 호출
        apiService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    // 로그인 성공 시 SharedPreferences에 사용자 정보 저장
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);  // 사용자 이름 저장
                    editor.apply();

                    showCustomToast("로그인 성공");

                    // 메인 화면으로 이동
                    Intent intent = new Intent(LoginActivity.this, BoardActivity.class);
                    startActivity(intent);
                    finish(); // 로그인 후 현재 액티비티 종료
                } else {
                    try {
                        // 로그인 실패 시 서버에서 반환한 메시지 가져오기
                        String errorMessage = response.errorBody().string(); // 실패 메시지
                        JSONObject jsonObject = new JSONObject(errorMessage);
                        String message = jsonObject.getString("message");

                        // 메시지를 토스트로 표시
                        showCustomToast(message);
                    } catch (Exception e) {
                        // 에러 처리
                        showCustomToast("로그인 실패: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // 네트워크 실패 처리
                Log.e("LoginActivity", "Error: " + t.getMessage());
                showCustomToast("서버 연결 실패");
            }
        });
    }

    // LoginActivity.java 또는 해당 액티비티 클래스에서
    public void onSignUpClick(View view) {
        // 회원가입 화면으로 전환
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
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

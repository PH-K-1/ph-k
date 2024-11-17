package com.example.ph_k;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // 입력값 유효성 검사
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User(username, email, password);

            // Retrofit 클라이언트 설정
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<String> call = apiService.registerUser(user); // 서버에 회원가입 요청

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        Log.d("Response", "Response: " + response.body());  // 응답 내용 로그 출력
                        Toast.makeText(RegisterActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Retrofit", "서버 연결 실패: " + response.message());
                        Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    // 서버 연결 실패
                    Toast.makeText(RegisterActivity.this, "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

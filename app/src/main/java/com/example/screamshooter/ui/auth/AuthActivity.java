package com.example.screamshooter.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.screamshooter.R;
import com.example.screamshooter.ui.home.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword, edtPlayerName;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth); // Đảm bảo layout của bạn khớp tên này

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtPlayerName = findViewById(R.id.edtPlayerName); // Ô nhập tên hiển thị trong game
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Xử lý Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AuthActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Xử lý Đăng ký kèm tên người chơi
        btnRegister.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String playerName = edtPlayerName.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || playerName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin và tên hiển thị!", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Cập nhật tên hiển thị (DisplayName) cho tài khoản Firebase Auth
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(playerName)
                                        .build();

                                user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AuthActivity.this, MainActivity.class));
                                    finish();
                                });
                            }
                        } else {
                            Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
package com.example.screamshooter;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hiển thị Main Menu
        setContentView(R.layout.activity_main);

        // Nút PLAY
        Button btnPlaying = findViewById(R.id.btnPlaying);

        btnPlaying.setOnClickListener(v -> {

            // Chuyển sang màn hình Gameplay
            setContentView(new GameView(this));

        });
    }
}
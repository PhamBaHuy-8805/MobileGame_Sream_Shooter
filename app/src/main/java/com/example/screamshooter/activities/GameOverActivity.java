package com.example.screamshooter.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.screamshooter.R;
import com.example.screamshooter.data.LocalScoreManager;

public class GameOverActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        int finalScore = getIntent().getIntExtra("SCORE", 0);

        LocalScoreManager scoreManager = new LocalScoreManager(this);
        int highScore = scoreManager.getHighScore();

        TextView tvFinalScore = findViewById(R.id.tvFinalScore);
        TextView tvHighScore = findViewById(R.id.tvHighScore);
        View btnRestart = findViewById(R.id.btnRestart);
        View btnHome = findViewById(R.id.btnGameOverHome);

        tvFinalScore.setText("ĐIỂM ĐẠT ĐƯỢC: " + finalScore);
        tvHighScore.setText("ĐIỂM CAO NHẤT: " + highScore);

        btnRestart.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });

        btnHome.setOnClickListener(v -> finish());
    }
}

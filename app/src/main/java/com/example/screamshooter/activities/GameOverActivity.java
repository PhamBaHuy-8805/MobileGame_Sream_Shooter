package com.example.screamshooter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.screamshooter.R;
import com.example.screamshooter.data.LocalScoreManager;

public class GameOverActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        int finalScore = getIntent().getIntExtra("SCORE", 0);
        int starsCollected = getIntent().getIntExtra("STARS", 0);

        LocalScoreManager scoreManager = new LocalScoreManager(this);
        int highScore = scoreManager.getHighScore();

        TextView tvFinalScore = findViewById(R.id.tvFinalScore);
        TextView tvHighScore = findViewById(R.id.tvHighScore);
        TextView tvStarsCollected = findViewById(R.id.tvStarsCollected);
        View btnRestart = findViewById(R.id.btnRestart);
        View btnHome = findViewById(R.id.btnGameOverHome);

        tvFinalScore.setText(getString(R.string.game_over_score, finalScore));
        tvHighScore.setText(getString(R.string.game_over_high_score, highScore));
        tvStarsCollected.setText(getString(R.string.game_over_stars, starsCollected));

        btnRestart.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });

        btnHome.setOnClickListener(v -> finish());
    }
}

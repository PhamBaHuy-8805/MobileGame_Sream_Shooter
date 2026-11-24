package com.example.screamshooter.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.screamshooter.R;
import com.example.screamshooter.game.GameEngine;
import com.example.screamshooter.utils.PermissionUtils;
import com.example.screamshooter.views.GameView;

public class GameActivity extends Activity implements GameEngine.GameEngineListener {

    private GameView gameView;
    private GameEngine gameEngine;
    private View btnPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Xin quyền Record Audio cho tính năng Voice Scream Skill (FR07)
        if (!PermissionUtils.hasRecordAudioPermission(this)) {
            PermissionUtils.requestRecordAudioPermission(this);
        }

        gameView = findViewById(R.id.gameView);
        btnPause = findViewById(R.id.btnPause);

        // Lấy kích thước màn hình
        gameView.post(() -> {
            int width = gameView.getWidth();
            int height = gameView.getHeight();

            gameEngine = new GameEngine(GameActivity.this, width, height, GameActivity.this);
            gameView.setGameEngine(gameEngine);
            gameEngine.startSensors();
        });

        btnPause.setOnClickListener(v -> showPauseDialog());
    }

    private void showPauseDialog() {
        if (gameEngine != null) {
            gameEngine.pauseGame();
        }

        Dialog pauseDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        pauseDialog.setContentView(R.layout.dialog_pause);
        pauseDialog.setCancelable(false);

        View btnResume = pauseDialog.findViewById(R.id.btnResume);
        View btnSettingsPause = pauseDialog.findViewById(R.id.btnSettingsPause);
        View btnHome = pauseDialog.findViewById(R.id.btnHome);

        btnResume.setOnClickListener(v -> {
            pauseDialog.dismiss();
            if (gameEngine != null) {
                gameEngine.resumeGame();
            }
        });

        if (btnSettingsPause != null) {
            btnSettingsPause.setOnClickListener(v ->
                Toast.makeText(GameActivity.this, "Cài đặt âm thanh đang ở mức tối ưu", Toast.LENGTH_SHORT).show()
            );
        }

        btnHome.setOnClickListener(v -> {
            pauseDialog.dismiss();
            finish();
        });

        pauseDialog.show();
    }

    @Override
    public void onGameOver(int finalScore) {
        runOnUiThread(() -> {
            Intent intent = new Intent(GameActivity.this, GameOverActivity.class);
            intent.putExtra("SCORE", finalScore);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onEnergyFull() {
        runOnUiThread(() -> Toast.makeText(GameActivity.this, "⚡ Năng lượng đầy! Hãy lớn tiếng/hét vào Mic để bật Skill!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onSkillActivated() {
        runOnUiThread(() -> Toast.makeText(GameActivity.this, "⚡ KÍCH HOẠT THÀNH CÔNG THẦN CHƯỞNG VŨ TRỤ!", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameEngine != null) {
            gameEngine.stopSensors();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameEngine != null && gameEngine.getGameState() == GameEngine.GameState.RUNNING) {
            gameEngine.startSensors();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameEngine != null) {
            gameEngine.stopSensors();
        }
    }
}

package com.example.screamshooter.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.screamshooter.R;
import com.example.screamshooter.data.SettingsManager;
import com.example.screamshooter.game.GameEngine;
import com.example.screamshooter.utils.PermissionUtils;
import com.example.screamshooter.views.GameView;

public class GameActivity extends BaseActivity implements GameEngine.GameEngineListener {

    private GameView gameView;
    private GameEngine gameEngine;
    private View btnPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (!PermissionUtils.hasRecordAudioPermission(this)) {
            PermissionUtils.requestRecordAudioPermission(this);
        }

        gameView = findViewById(R.id.gameView);
        btnPause = findViewById(R.id.btnPause);

        gameView.post(() -> {
            int width = gameView.getWidth();
            int height = gameView.getHeight();

            gameEngine = new GameEngine(
                    GameActivity.this, width, height, GameActivity.this);
            gameView.setGameEngine(gameEngine);
            gameEngine.startSensors();
        });

        btnPause.setOnClickListener(v -> showPauseDialog());
    }

    private void showPauseDialog() {
        if (gameEngine != null) {
            gameEngine.pauseGame();
        }

        Dialog pauseDialog = new Dialog(
                this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
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
            btnSettingsPause.setOnClickListener(v -> showSoundDialog());
        }

        btnHome.setOnClickListener(v -> {
            pauseDialog.dismiss();
            if (gameEngine != null) {
                // Kết thúc lượt chơi và mở màn tổng kết trước khi về trang chủ
                gameEngine.finishRun();
            } else {
                finish();
            }
        });

        pauseDialog.show();
    }

    private void showSoundDialog() {
        Dialog soundDialog = new Dialog(
                this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        soundDialog.setContentView(R.layout.dialog_sound);

        SettingsManager settingsManager = new SettingsManager(this);
        SeekBar seekBar = soundDialog.findViewById(R.id.dialogSeekVolume);
        TextView valueView = soundDialog.findViewById(R.id.dialogVolumeValue);

        int volume = settingsManager.getVolume();
        seekBar.setProgress(volume);
        if (valueView != null) {
            valueView.setText(getString(R.string.settings_volume_percent, volume));
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                settingsManager.setVolume(progress);
                if (valueView != null) {
                    valueView.setText(
                            getString(R.string.settings_volume_percent, progress)
                    );
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });

        View closeButton = soundDialog.findViewById(R.id.dialogVolumeClose);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> soundDialog.dismiss());
        }

        soundDialog.show();
    }

    @Override
    public void onGameOver(int finalScore) {
        runOnUiThread(() -> {
            int starsCollected = gameEngine != null
                    ? gameEngine.getStarsCollected()
                    : 0;

            Intent intent = new Intent(GameActivity.this, GameOverActivity.class);
            intent.putExtra("SCORE", finalScore);
            intent.putExtra("STARS", starsCollected);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onEnergyFull() {
        runOnUiThread(() -> Toast.makeText(
                GameActivity.this,
                R.string.game_energy_full,
                Toast.LENGTH_SHORT
        ).show());
    }

    @Override
    public void onSkillActivated() {
        runOnUiThread(() -> Toast.makeText(
                GameActivity.this,
                R.string.game_skill_activated,
                Toast.LENGTH_SHORT
        ).show());
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
        if (gameEngine != null
                && gameEngine.getGameState() == GameEngine.GameState.RUNNING) {
            gameEngine.startSensors();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameEngine != null) {
            gameEngine.release();
        }
    }
}

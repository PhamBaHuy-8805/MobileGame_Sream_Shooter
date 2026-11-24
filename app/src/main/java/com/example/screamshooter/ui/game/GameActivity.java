package com.example.screamshooter.ui.game;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.screamshooter.R;
import com.example.screamshooter.data.repository.PlayerRepository;
import com.example.screamshooter.ui.auth.AuthActivity;
import com.example.screamshooter.ui.setting.SettingsActivity;
import com.example.screamshooter.utils.AudioHelper;
import com.example.screamshooter.utils.SensorHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GameActivity extends AppCompatActivity
        implements SensorHelper.OnTiltListener,
        AudioHelper.OnScreamListener {

    private GameView gameView;

    private SensorHelper sensorHelper;

    private AudioHelper audioHelper;

    private boolean activityResumed = false;

    private boolean manualPause = false;

    private boolean audioPermissionRequested = false;

    private Dialog pauseDialog;

    private static final int REQUEST_AUDIO = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        enableImmersiveMode();

        FirebaseUser user =
                FirebaseAuth.getInstance()
                        .getCurrentUser();

        if (user == null) {

            startActivity(
                    new Intent(this, AuthActivity.class)
            );

            finish();

            return;
        }

        final String uid = user.getUid();

        showLoading();

        sensorHelper = new SensorHelper(
                this,
                this
        );

        new PlayerRepository()
                .loadProfile()

                .addOnSuccessListener(profile -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    FirebaseUser currentUser =
                            FirebaseAuth.getInstance()
                                    .getCurrentUser();

                    if (currentUser == null ||
                            !uid.equals(currentUser.getUid())) {

                        finish();

                        return;
                    }

                    gameView = new GameView(
                            this,
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels,
                            getResources()
                                    .getDisplayMetrics()
                                    .heightPixels,
                            profile,
                            uid
                    );

                    setContentView(gameView);

                    if (activityResumed) {

                        startGameplay();
                    }
                })

                .addOnFailureListener(error -> {

                    Toast.makeText(
                            this,
                            R.string.network_error,
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
    }

    private void showLoading() {

        FrameLayout layout =
                new FrameLayout(this);

        layout.setBackgroundColor(
                ContextCompat.getColor(
                        this,
                        R.color.game_background
                )
        );

        ProgressBar progressBar =
                new ProgressBar(this);

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        64,
                        64,
                        Gravity.CENTER
                );

        layout.addView(progressBar, params);

        setContentView(layout);
    }

    private void startGameplay() {

        if (gameView == null ||
                !activityResumed ||
                manualPause) {

            return;
        }

        gameView.resume();

        if (sensorHelper != null) {

            sensorHelper.register();
        }

        startAudioIfAllowed();
    }

    private void startAudioIfAllowed() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED) {

            if (audioHelper == null) {

                audioHelper = new AudioHelper(
                        this,
                        this
                );

                audioHelper.startListening();
            }

        } else if (!audioPermissionRequested) {

            audioPermissionRequested = true;

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    REQUEST_AUDIO
            );
        }
    }

    private void stopGameplay() {

        if (gameView != null) {

            gameView.pause();
        }

        if (sensorHelper != null) {

            sensorHelper.unregister();
        }

        if (audioHelper != null) {

            audioHelper.stopListening();

            audioHelper = null;
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        activityResumed = true;

        startGameplay();
    }

    @Override
    protected void onPause() {

        activityResumed = false;

        stopGameplay();

        super.onPause();
    }

    @Override
    public void onTilt(float tiltX) {

        if (gameView != null) {

            gameView.updatePlayerPosition(tiltX);
        }
    }

    @Override
    public void onScreamDetected(double decibel) {

        if (gameView != null &&
                activityResumed &&
                !manualPause) {

            gameView.triggerSkill();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_AUDIO &&
                grantResults.length > 0 &&
                grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {

            if (activityResumed && !manualPause) {

                startAudioIfAllowed();
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (gameView == null) {

            finish();

            return;
        }

        showPauseDialog();
    }

    private void showPauseDialog() {

        if (pauseDialog != null &&
                pauseDialog.isShowing()) {

            return;
        }

        manualPause = true;

        stopGameplay();

        pauseDialog = new Dialog(this);

        pauseDialog.setContentView(
                R.layout.dialog_pause
        );

        pauseDialog.setCancelable(false);

        Button btnResume =
                pauseDialog.findViewById(
                        R.id.btnResume
                );

        Button btnSettings =
                pauseDialog.findViewById(
                        R.id.btnSettingsInPause
                );

        Button btnQuit =
                pauseDialog.findViewById(
                        R.id.btnQuitToHome
                );

        btnResume.setOnClickListener(v -> {

            pauseDialog.dismiss();

            manualPause = false;

            startGameplay();
        });

        btnSettings.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            this,
                            SettingsActivity.class
                    )
            );
        });

        btnQuit.setOnClickListener(v -> {

            pauseDialog.dismiss();

            finish();
        });

        pauseDialog.show();
    }

    private void enableImmersiveMode() {

        getWindow().addFlags(
                WindowManager.LayoutParams
                        .FLAG_KEEP_SCREEN_ON
        );

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );

        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        controller.hide(
                WindowInsetsCompat.Type.systemBars()
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {

            enableImmersiveMode();
        }
    }
}
package com.example.screamshooter.ui.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.screamshooter.R;
import com.example.screamshooter.data.repository.PlayerRepository;
import com.example.screamshooter.ui.home.MainActivity;

public class GameOverActivity extends AppCompatActivity {

    private TextView tvSaveStatus;

    private Button btnRestart;
    private Button btnHome;
    private Button btnRetrySave;

    private int finalScore;
    private int finalKills;

    private long earnedCoins;

    private String matchId;
    private String matchOwnerUid;

    private boolean saving = false;
    private boolean saved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_over);

        TextView tvFinalScore =
                findViewById(R.id.tvFinalScore);

        TextView tvFinalKills =
                findViewById(R.id.tvFinalKills);

        TextView tvEarnedCoins =
                findViewById(R.id.tvEarnedCoins);

        tvSaveStatus =
                findViewById(R.id.tvSaveStatus);

        btnRestart =
                findViewById(R.id.btnRestart);

        btnHome =
                findViewById(R.id.btnHome);

        btnRetrySave =
                findViewById(R.id.btnRetrySave);

        finalScore = getIntent().getIntExtra(
                "FINAL_SCORE",
                0
        );

        finalKills = getIntent().getIntExtra(
                "FINAL_KILLS",
                0
        );

        earnedCoins = getIntent().getLongExtra(
                "EARNED_COINS",
                0L
        );

        matchId = getIntent().getStringExtra(
                "MATCH_ID"
        );

        matchOwnerUid = getIntent().getStringExtra(
                "MATCH_OWNER_UID"
        );

        tvFinalScore.setText(
                getString(
                        R.string.final_score,
                        finalScore
                )
        );

        tvFinalKills.setText(
                getString(
                        R.string.match_kills,
                        finalKills
                )
        );

        tvEarnedCoins.setText(
                getString(
                        R.string.reward_coins,
                        earnedCoins
                )
        );

        btnRestart.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            this,
                            GameActivity.class
                    )
            );

            finish();
        });

        btnHome.setOnClickListener(v -> {

            Intent intent = new Intent(
                    this,
                    MainActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);

            finish();
        });

        btnRetrySave.setOnClickListener(
                v -> saveResult()
        );

        saveResult();
    }

    private void saveResult() {

        if (saving || saved) {
            return;
        }

        saving = true;

        btnRestart.setEnabled(false);
        btnHome.setEnabled(false);

        btnRetrySave.setVisibility(View.GONE);

        tvSaveStatus.setText(R.string.saving_result);

        new PlayerRepository()
                .saveMatchResult(
                        matchId,
                        matchOwnerUid,
                        finalScore,
                        finalKills,
                        earnedCoins
                )

                .addOnSuccessListener(unused -> {

                    saving = false;
                    saved = true;

                    tvSaveStatus.setText(
                            R.string.result_saved
                    );

                    btnRestart.setEnabled(true);
                    btnHome.setEnabled(true);

                    Log.d(
                            "FIREBASE_SAVE",
                            "Match saved successfully"
                    );
                })

                .addOnFailureListener(error -> {

                    saving = false;

                    String errorMessage =
                            error.getMessage();

                    if (errorMessage == null) {
                        errorMessage =
                                error.getClass().getSimpleName();
                    }

                    Log.e(
                            "FIREBASE_SAVE",
                            "Save match failed",
                            error
                    );

                    tvSaveStatus.setText(
                            getString(R.string.result_save_failed)
                                    + "\n"
                                    + errorMessage
                    );

                    Toast.makeText(
                            GameOverActivity.this,
                            errorMessage,
                            Toast.LENGTH_LONG
                    ).show();

                    btnRetrySave.setVisibility(View.VISIBLE);

                    btnRestart.setEnabled(false);
                    btnHome.setEnabled(false);
                });
    }

    @Override
    public void onBackPressed() {

        if (saved) {

            super.onBackPressed();

        } else if (!saving) {

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setMessage(R.string.leave_unsaved_match)
                    .setNegativeButton(
                            android.R.string.cancel,
                            null
                    )
                    .setPositiveButton(
                            android.R.string.ok,
                            (dialog, which) -> finish()
                    )
                    .show();
        }
    }
}
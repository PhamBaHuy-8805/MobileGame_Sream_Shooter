package com.example.screamshooter.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.screamshooter.R;
import com.example.screamshooter.ui.achievement.AchievementActivity;
import com.example.screamshooter.ui.auth.AuthActivity;
import com.example.screamshooter.ui.game.GameActivity;
import com.example.screamshooter.ui.setting.SettingsActivity;
import com.example.screamshooter.ui.shop.UpgradeActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextView tvUsername;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mặc định sử dụng tiếng Việt
        // nếu người dùng chưa chọn ngôn ngữ.

        if (AppCompatDelegate.getApplicationLocales().isEmpty()) {

            AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags("vi")
            );
        }

        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        tvUsername = findViewById(R.id.tvUsername);

        initListeners();
    }

    private void initListeners() {

        findViewById(R.id.btnStartGame)
                .setOnClickListener(v ->
                        openAuthenticatedActivity(GameActivity.class)
                );

        findViewById(R.id.btnUpgrade)
                .setOnClickListener(v ->
                        openAuthenticatedActivity(UpgradeActivity.class)
                );

        findViewById(R.id.btnAchievement)
                .setOnClickListener(v ->
                        startActivity(new Intent(
                                this,
                                AchievementActivity.class
                        ))
                );

        findViewById(R.id.btnSettings)
                .setOnClickListener(v ->
                        startActivity(new Intent(
                                this,
                                SettingsActivity.class
                        ))
                );

        findViewById(R.id.layoutAccount)
                .setOnClickListener(v ->
                        startActivity(new Intent(
                                this,
                                AuthActivity.class
                        ))
                );
    }

    @Override
    protected void onResume() {

        super.onResume();

        updateAccountInformation();
    }

    private void updateAccountInformation() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {

            tvUsername.setText(R.string.guest);

            return;
        }

        String playerName = user.getDisplayName();

        if (playerName == null || playerName.trim().isEmpty()) {

            playerName = getString(R.string.guest);
        }

        tvUsername.setText(
                getString(R.string.player_name, playerName)
        );
    }

    private void openAuthenticatedActivity(Class<?> activityClass) {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    R.string.login_required,
                    Toast.LENGTH_SHORT
            ).show();

            startActivity(new Intent(
                    this,
                    AuthActivity.class
            ));

            return;
        }

        startActivity(new Intent(
                this,
                activityClass
        ));
    }
}
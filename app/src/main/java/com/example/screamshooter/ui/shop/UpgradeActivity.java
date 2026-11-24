package com.example.screamshooter.ui.shop;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.screamshooter.R;
import com.example.screamshooter.data.model.PlayerProfile;
import com.example.screamshooter.data.repository.PlayerRepository;

import java.text.NumberFormat;

public class UpgradeActivity extends AppCompatActivity {

    private TextView tvCoinBalance;
    private LinearLayout layoutUpgradeItems;
    private ProgressBar progressLoading;

    private PlayerRepository repository;

    private PlayerProfile profile;

    private boolean busy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upgrade);

        tvCoinBalance =
                findViewById(R.id.tvCoinBalance);

        layoutUpgradeItems =
                findViewById(R.id.layoutUpgradeItems);

        progressLoading =
                findViewById(R.id.progressLoading);

        repository = new PlayerRepository();

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());

        loadProfile();
    }

    private void loadProfile() {

        busy = true;

        progressLoading.setVisibility(View.VISIBLE);

        repository.loadProfile()

                .addOnSuccessListener(result -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    profile = result;

                    busy = false;

                    progressLoading.setVisibility(
                            View.GONE
                    );

                    renderShop();
                })

                .addOnFailureListener(error -> {

                    busy = false;

                    progressLoading.setVisibility(
                            View.GONE
                    );

                    Toast.makeText(
                            this,
                            R.string.network_error,
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void renderShop() {

        layoutUpgradeItems.removeAllViews();

        tvCoinBalance.setText(
                getString(
                        R.string.coin_balance,
                        profile.coins
                )
        );

        addUpgradeItem(
                PlayerProfile.HEALTH,
                R.string.upgrade_health,
                R.color.game_danger
        );

        addUpgradeItem(
                PlayerProfile.COIN,
                R.string.upgrade_coin,
                R.color.game_gold
        );

        addUpgradeItem(
                PlayerProfile.FIRE_RATE,
                R.string.upgrade_fire_rate,
                R.color.neon_cyan
        );

        addUpgradeItem(
                PlayerProfile.ENERGY,
                R.string.upgrade_energy,
                R.color.neon_purple
        );

        addUpgradeItem(
                PlayerProfile.SPEED,
                R.string.upgrade_speed,
                R.color.neon_green
        );
    }

    private void addUpgradeItem(
            String type,
            int titleResource,
            int colorResource
    ) {

        int level = profile.getLevel(type);

        long cost =
                PlayerProfile.getUpgradeCost(level);

        int accentColor = ContextCompat.getColor(
                this,
                colorResource
        );

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.HORIZONTAL);

        card.setGravity(Gravity.CENTER_VERTICAL);

        card.setPadding(
                dp(12), dp(10), dp(12), dp(10)
        );

        card.setBackgroundResource(
                R.drawable.bg_panel
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1, -2
                );

        cardParams.bottomMargin = dp(8);

        card.setLayoutParams(cardParams);

        LinearLayout information =
                new LinearLayout(this);

        information.setOrientation(
                LinearLayout.VERTICAL
        );

        information.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0, -2, 1f
                )
        );

        TextView title = new TextView(this);

        title.setText(
                getString(titleResource)
                        + "  "
                        + level + "/5"
        );

        title.setTextSize(16);

        title.setTextColor(accentColor);

        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        information.addView(title);

        LinearLayout levelBar =
                new LinearLayout(this);

        levelBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        LinearLayout.LayoutParams barParams =
                new LinearLayout.LayoutParams(
                        -1, dp(8)
                );

        barParams.topMargin = dp(8);
        barParams.bottomMargin = dp(8);

        levelBar.setLayoutParams(barParams);

        for (int i = 0; i < 5; i++) {

            View segment = new View(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            0, -1, 1f
                    );

            params.rightMargin = dp(4);

            segment.setLayoutParams(params);

            segment.setBackgroundColor(
                    i < level
                            ? accentColor
                            : ContextCompat.getColor(
                            this,
                            R.color.game_border
                    )
            );

            levelBar.addView(segment);
        }

        information.addView(levelBar);

        TextView description = new TextView(this);

        description.setTextSize(13);

        description.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_secondary
                )
        );

        description.setText(
                getUpgradeDescription(type, level)
        );

        information.addView(description);

        card.addView(information);

        AppCompatButton button =
                new AppCompatButton(this);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        dp(132), dp(48)
                );

        buttonParams.leftMargin = dp(12);

        button.setLayoutParams(buttonParams);

        button.setSupportBackgroundTintList(null);

        button.setBackgroundResource(
                R.drawable.bg_primary_button
        );

        button.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.game_background
                )
        );

        button.setTextSize(12);

        if (level >= PlayerProfile.MAX_LEVEL) {

            button.setText(R.string.max_level);

            button.setEnabled(false);

            button.setAlpha(0.5f);

        } else {

            String formattedCost =
                    NumberFormat.getIntegerInstance(
                            getResources()
                                    .getConfiguration()
                                    .getLocales()
                                    .get(0)
                    ).format(cost);

            button.setText(
                    getString(
                            R.string.purchase_cost,
                            formattedCost
                    )
            );

            button.setOnClickListener(
                    v -> purchase(type)
            );

            if (profile.coins < cost) {

                button.setAlpha(0.5f);
            }
        }

        card.addView(button);

        layoutUpgradeItems.addView(card);
    }

    private String getUpgradeDescription(
            String type,
            int level
    ) {

        int nextLevel = Math.min(
                level + 1,
                PlayerProfile.MAX_LEVEL
        );

        switch (type) {

            case PlayerProfile.HEALTH:

                return getString(
                        R.string.upgrade_health_detail,
                        3 + level,
                        3 + nextLevel
                );

            case PlayerProfile.COIN:

                return getString(
                        R.string.upgrade_coin_detail,
                        100 + level * 100,
                        100 + nextLevel * 100
                );

            case PlayerProfile.FIRE_RATE:

                return getString(
                        R.string.upgrade_fire_detail,
                        300 - level * 30,
                        300 - nextLevel * 30
                );

            case PlayerProfile.ENERGY:

                return getString(
                        R.string.upgrade_energy_detail,
                        15 + level * 3,
                        15 + nextLevel * 3
                );

            case PlayerProfile.SPEED:

                return getString(
                        R.string.upgrade_speed_detail,
                        100 + level * 10,
                        100 + nextLevel * 10
                );

            default:
                return "";
        }
    }

    private void purchase(String type) {

        if (busy || profile == null) {
            return;
        }

        int level = profile.getLevel(type);

        long cost =
                PlayerProfile.getUpgradeCost(level);

        if (level >= PlayerProfile.MAX_LEVEL) {
            return;
        }

        if (profile.coins < cost) {

            Toast.makeText(
                    this,
                    R.string.not_enough_coins,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        busy = true;

        progressLoading.setVisibility(View.VISIBLE);

        repository.purchaseUpgrade(type)

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            R.string.upgrade_completed,
                            Toast.LENGTH_SHORT
                    ).show();

                    loadProfile();
                })

                .addOnFailureListener(error -> {

                    Toast.makeText(
                            this,
                            R.string.upgrade_failed,
                            Toast.LENGTH_LONG
                    ).show();

                    loadProfile();
                });
    }

    private int dp(float value) {

        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
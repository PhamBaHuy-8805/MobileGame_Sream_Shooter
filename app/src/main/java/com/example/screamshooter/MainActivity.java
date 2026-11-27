package com.example.screamshooter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.screamshooter.activities.BaseActivity;
import com.example.screamshooter.activities.GameActivity;
import com.example.screamshooter.activities.LeaderboardActivity;
import com.example.screamshooter.data.AchievementManager;
import com.example.screamshooter.data.LocalScoreManager;
import com.example.screamshooter.data.SettingsManager;
import com.example.screamshooter.data.UpgradeManager;

public class MainActivity extends BaseActivity {

    private View btnStart, btnUpgrade, btnAchievement;
    private View btnSettings, btnLeaderboard, btnLogin;

    private View settingsOverlay, upgradeOverlay;
    private View upgradeDetailOverlay, achievementOverlay;

    private TextView starCount;
    private SeekBar volumeSeekBar;
    private TextView volumeValue;
    private Spinner languageSpinner;

    private LocalScoreManager scoreManager;
    private UpgradeManager upgradeManager;
    private AchievementManager achievementManager;
    private SettingsManager settingsManager;

    private int coins = 0;
    private boolean syncingSettings = false;

    private final TextView[] achievementProgressTexts = new TextView[AchievementManager.COUNT];
    private final TextView[] achievementRewardButtons = new TextView[AchievementManager.COUNT];
    private final ProgressBar[] achievementProgressBars = new ProgressBar[AchievementManager.COUNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scoreManager = new LocalScoreManager(this);
        upgradeManager = new UpgradeManager(this);
        achievementManager = new AchievementManager(this);
        settingsManager = new SettingsManager(this);
        coins = scoreManager.getCoins();

        btnStart = findViewById(R.id.btnStart);
        btnUpgrade = findViewById(R.id.btnUpgrade);
        btnAchievement = findViewById(R.id.btnAchievement);
        btnSettings = findViewById(R.id.btnSettings);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnLogin = findViewById(R.id.btnLogin);

        ViewGroup root = findViewById(android.R.id.content);

        settingsOverlay = getLayoutInflater().inflate(
                R.layout.overlay_settings, root, false);
        upgradeOverlay = getLayoutInflater().inflate(
                R.layout.overlay_upgrade, root, false);

        root.addView(settingsOverlay);
        root.addView(upgradeOverlay);

        settingsOverlay.setVisibility(View.GONE);
        upgradeOverlay.setVisibility(View.GONE);

        upgradeDetailOverlay = upgradeOverlay.findViewById(R.id.upgradeDetailOverlay);
        starCount = upgradeOverlay.findViewById(R.id.tvStarCount);

        initAchievementOverlay(root);
        setupSettingsOverlay();
        setupUpgradeSystem();

        btnStart.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(
                        MainActivity.this,
                        R.string.game_cannot_open,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnUpgrade.setOnClickListener(v -> {
            hideOtherOverlays();
            upgradeOverlay.setVisibility(View.VISIBLE);
            upgradeOverlay.bringToFront();
            setOverlayTitle(upgradeOverlay, R.id.upgradeTitle,
                    getString(R.string.upgrade_title));
            updateStarCount();
            updateUpgradeButtons();
        });

        btnSettings.setOnClickListener(v -> {
            hideOtherOverlays();
            settingsOverlay.setVisibility(View.VISIBLE);
            settingsOverlay.bringToFront();
            setOverlayTitle(settingsOverlay, R.id.settingsTitle,
                    getString(R.string.settings_title));
            syncSettingsUi();
        });

        View closeSettings = settingsOverlay.findViewById(R.id.settingsClose);
        if (closeSettings != null) {
            closeSettings.setOnClickListener(v ->
                    settingsOverlay.setVisibility(View.GONE));
        }

        btnAchievement.setOnClickListener(v -> {
            hideOtherOverlays();
            achievementOverlay.setVisibility(View.VISIBLE);
            achievementOverlay.bringToFront();
            updateAchievements();
        });

        btnLeaderboard.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnLogin.setOnClickListener(v -> {
            coins = scoreManager.getCoins();
            Toast.makeText(
                    this,
                    getString(
                            R.string.profile_toast,
                            scoreManager.getHighScore(),
                            coins
                    ),
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    private void hideOtherOverlays() {
        settingsOverlay.setVisibility(View.GONE);
        upgradeOverlay.setVisibility(View.GONE);
        achievementOverlay.setVisibility(View.GONE);
    }

    // =====================================================
    // CÀI ĐẶT ÂM THANH / NGÔN NGỮ
    // =====================================================

    private void setupSettingsOverlay() {
        setOverlayTitle(settingsOverlay, R.id.settingsTitle,
                getString(R.string.settings_title));

        volumeSeekBar = settingsOverlay.findViewById(R.id.seekVolume);
        volumeValue = settingsOverlay.findViewById(R.id.tvVolumeValue);
        languageSpinner = settingsOverlay.findViewById(R.id.spinnerLanguage);

        LanguageSpinnerAdapter adapter = new LanguageSpinnerAdapter();
        languageSpinner.setAdapter(adapter);
        syncSettingsUi();

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (syncingSettings) {
                    return;
                }

                String selected = adapter.getLanguageCode(position);
                if (!selected.equals(settingsManager.getLanguage())) {
                    settingsManager.setLanguage(selected);
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settingsManager.setVolume(progress);
                if (volumeValue != null) {
                    volumeValue.setText(
                            getString(R.string.settings_volume_percent, progress)
                    );
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void syncSettingsUi() {
        if (volumeSeekBar != null) {
            int volume = settingsManager.getVolume();
            volumeSeekBar.setProgress(volume);
            if (volumeValue != null) {
                volumeValue.setText(
                        getString(R.string.settings_volume_percent, volume)
                );
            }
        }

        if (languageSpinner != null) {
            syncingSettings = true;
            int position = SettingsManager.LANGUAGE_EN.equals(
                    settingsManager.getLanguage()) ? 1 : 0;
            languageSpinner.setSelection(position, false);
            languageSpinner.post(() -> syncingSettings = false);
        }
    }

    private class LanguageSpinnerAdapter extends BaseAdapter {

        private final String[] codes = {
                SettingsManager.LANGUAGE_VI,
                SettingsManager.LANGUAGE_EN
        };
        private final String[] labels;

        LanguageSpinnerAdapter() {
            labels = new String[]{
                    getString(R.string.language_vi),
                    getString(R.string.language_en)
            };
        }

        String getLanguageCode(int position) {
            if (position < 0 || position >= codes.length) {
                return SettingsManager.LANGUAGE_VI;
            }
            return codes[position];
        }

        @Override
        public int getCount() {
            return codes.length;
        }

        @Override
        public Object getItem(int position) {
            return getLanguageCode(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_language_selected, parent, false);
            } else {
                textView = (TextView) convertView;
            }
            textView.setText(labels[position]);
            return textView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_language_dropdown, parent, false);
            } else {
                textView = (TextView) convertView;
            }
            textView.setText(labels[position]);
            return textView;
        }
    }

    // =====================================================
    // THÀNH TÍCH
    // =====================================================

    private void initAchievementOverlay(ViewGroup root) {
        achievementOverlay = getLayoutInflater().inflate(
                R.layout.overlay_achievement, root, false);
        root.addView(achievementOverlay);
        achievementOverlay.setVisibility(View.GONE);

        setOverlayTitle(achievementOverlay, R.id.achievementTitle,
                getString(R.string.achievement_title));

        achievementProgressTexts[0] = achievementOverlay.findViewById(R.id.achievementProgressText1);
        achievementProgressTexts[1] = achievementOverlay.findViewById(R.id.achievementProgressText2);
        achievementProgressTexts[2] = achievementOverlay.findViewById(R.id.achievementProgressText3);

        achievementRewardButtons[0] = achievementOverlay.findViewById(R.id.achievementReward1);
        achievementRewardButtons[1] = achievementOverlay.findViewById(R.id.achievementReward2);
        achievementRewardButtons[2] = achievementOverlay.findViewById(R.id.achievementReward3);

        achievementProgressBars[0] = achievementOverlay.findViewById(R.id.achievementProgress1);
        achievementProgressBars[1] = achievementOverlay.findViewById(R.id.achievementProgress2);
        achievementProgressBars[2] = achievementOverlay.findViewById(R.id.achievementProgress3);

        for (int i = 0; i < AchievementManager.COUNT; i++) {
            final int index = i;
            achievementRewardButtons[i].setOnClickListener(v -> claimAchievement(index));
        }

        View closeAchievement = achievementOverlay.findViewById(R.id.achievementClose);
        if (closeAchievement != null) {
            closeAchievement.setOnClickListener(v ->
                    achievementOverlay.setVisibility(View.GONE));
        }
    }

    private void updateAchievements() {
        if (achievementManager == null) {
            return;
        }

        for (int i = 0; i < AchievementManager.COUNT; i++) {
            boolean completed = achievementManager.isCompleted(i);
            int progress = achievementManager.getProgress(i);
            int target = achievementManager.getCurrentTarget(i);
            int reward = achievementManager.getCurrentReward(i);
            boolean canClaim = !completed && progress >= target;

            if (achievementProgressTexts[i] != null) {
                achievementProgressTexts[i].setText(
                        completed
                                ? getString(R.string.achievement_completed)
                                : getString(
                                        R.string.progress_format,
                                        Math.min(progress, target),
                                        target
                                )
                );
            }

            TextView button = achievementRewardButtons[i];
            if (button != null) {
                button.setText(
                        completed
                                ? getString(R.string.achievement_done)
                                : getString(R.string.reward_format, reward)
                );
                button.setEnabled(canClaim);
                button.setBackground(makeBackground(
                        canClaim ? Color.rgb(255, 210, 0) : Color.rgb(119, 112, 128),
                        Color.BLACK, 2, 6
                ));
                button.setTextColor(canClaim ? Color.rgb(25, 20, 30) : Color.WHITE);
            }

            if (achievementProgressBars[i] != null) {
                achievementProgressBars[i].setMax(Math.max(1, target));
                achievementProgressBars[i].setProgress(Math.min(progress, target));
            }
        }
    }

    private void claimAchievement(int index) {
        int result = achievementManager.claim(index);

        if (result == AchievementManager.CLAIM_ALREADY_COMPLETE) {
            Toast.makeText(this, R.string.achievement_already_complete,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (result == AchievementManager.CLAIM_NOT_REACHED) {
            Toast.makeText(
                    this,
                    getString(
                            R.string.achievement_not_reached,
                            achievementManager.getCurrentTarget(index)
                    ),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        coins = scoreManager.getCoins();
        updateAchievements();
        updateStarCount();
        updateUpgradeButtons();

        Toast.makeText(
                this,
                getString(R.string.achievement_claim_success, result),
                Toast.LENGTH_SHORT
        ).show();
    }

    // =====================================================
    // NÂNG CẤP
    // =====================================================

    private View upgradeView(int id) {
        return upgradeOverlay.findViewById(id);
    }

    private void setupUpgradeSystem() {
        setOverlayTitle(upgradeOverlay, R.id.upgradeTitle,
                getString(R.string.upgrade_title));

        upgradeView(R.id.buyHealth).setOnClickListener(v ->
                buyUpgrade(UpgradeManager.TYPE_HEALTH));
        upgradeView(R.id.buySpeed).setOnClickListener(v ->
                buyUpgrade(UpgradeManager.TYPE_BULLET_SPEED));
        upgradeView(R.id.buyTime).setOnClickListener(v ->
                buyUpgrade(UpgradeManager.TYPE_SKILL_TIME));
        upgradeView(R.id.buyMagnet).setOnClickListener(v ->
                buyUpgrade(UpgradeManager.TYPE_MAGNET));

        upgradeView(R.id.iconHealth).setOnClickListener(v ->
                showUpgradeDetail(UpgradeManager.TYPE_HEALTH));
        upgradeView(R.id.iconSpeed).setOnClickListener(v ->
                showUpgradeDetail(UpgradeManager.TYPE_BULLET_SPEED));
        upgradeView(R.id.iconTime).setOnClickListener(v ->
                showUpgradeDetail(UpgradeManager.TYPE_SKILL_TIME));
        upgradeView(R.id.iconMagnet).setOnClickListener(v ->
                showUpgradeDetail(UpgradeManager.TYPE_MAGNET));

        View closeUpgrade = upgradeView(R.id.upgradeClose);
        if (closeUpgrade != null) {
            closeUpgrade.setOnClickListener(v -> {
                if (upgradeDetailOverlay != null) {
                    upgradeDetailOverlay.setVisibility(View.GONE);
                }
                upgradeOverlay.setVisibility(View.GONE);
            });
        }

        View closeDetail = upgradeView(R.id.upgradeDetailClose);
        if (closeDetail != null) {
            closeDetail.setOnClickListener(v ->
                    upgradeDetailOverlay.setVisibility(View.GONE));
        }

        updateStarCount();
        updateUpgradeButtons();
    }

    private void buyUpgrade(int type) {
        int result = upgradeManager.buy(type);

        if (result == UpgradeManager.RESULT_MAX) {
            Toast.makeText(this, R.string.upgrade_max_reached,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (result == UpgradeManager.RESULT_NOT_ENOUGH) {
            Toast.makeText(
                    this,
                    getString(R.string.upgrade_not_enough,
                            upgradeManager.getPrice(type)),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        coins = scoreManager.getCoins();
        updateStarCount();
        updateUpgradeButtons();

        Toast.makeText(
                this,
                getString(R.string.upgrade_success, coins),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void updateStarCount() {
        if (starCount != null) {
            starCount.setText(getString(R.string.reward_format, coins));
        }
    }

    private void updateUpgradeButtons() {
        updatePriceButton(R.id.buyHealth, UpgradeManager.TYPE_HEALTH);
        updatePriceButton(R.id.buySpeed, UpgradeManager.TYPE_BULLET_SPEED);
        updatePriceButton(R.id.buyTime, UpgradeManager.TYPE_SKILL_TIME);
        updatePriceButton(R.id.buyMagnet, UpgradeManager.TYPE_MAGNET);

        updateLevelCells(new int[]{
                R.id.health1, R.id.health2, R.id.health3,
                R.id.health4, R.id.health5
        }, upgradeManager.getLevel(UpgradeManager.TYPE_HEALTH));

        updateLevelCells(new int[]{
                R.id.speed1, R.id.speed2, R.id.speed3
        }, upgradeManager.getLevel(UpgradeManager.TYPE_BULLET_SPEED));

        updateLevelCells(new int[]{
                R.id.time1, R.id.time2, R.id.time3
        }, upgradeManager.getLevel(UpgradeManager.TYPE_SKILL_TIME));

        updateLevelCells(new int[]{
                R.id.magnet1, R.id.magnet2
        }, upgradeManager.getLevel(UpgradeManager.TYPE_MAGNET));
    }

    private void updatePriceButton(int buttonId, int type) {
        TextView button = upgradeOverlay.findViewById(buttonId);
        if (button == null) {
            return;
        }

        int level = upgradeManager.getLevel(type);
        boolean isMax = level >= upgradeManager.getMaxLevel(type);
        int price = upgradeManager.getPrice(type);
        boolean canBuy = !isMax && coins >= price;

        button.setText(isMax
                ? getString(R.string.upgrade_max)
                : String.valueOf(price));
        button.setTextColor(canBuy && !isMax
                ? Color.rgb(25, 20, 30) : Color.WHITE);

        button.setBackground(makeBackground(
                isMax || !canBuy
                        ? Color.rgb(119, 112, 128)
                        : Color.rgb(255, 210, 0),
                Color.BLACK, 2, 14));
    }

    private void updateLevelCells(int[] cellIds, int currentLevel) {
        for (int i = 0; i < cellIds.length; i++) {
            View cell = upgradeOverlay.findViewById(cellIds[i]);
            if (cell == null) {
                continue;
            }

            cell.setBackgroundResource(i < currentLevel
                    ? R.drawable.bg_upgrade_level_on
                    : R.drawable.bg_upgrade_level_off);
        }
    }

    private void showUpgradeDetail(int type) {
        if (upgradeDetailOverlay == null) {
            return;
        }

        TextView titleView = upgradeDetailOverlay.findViewById(R.id.upgradeDetailTitle);
        TextView descriptionView = upgradeDetailOverlay.findViewById(R.id.upgradeDetailDescription);
        TextView levelView = upgradeDetailOverlay.findViewById(R.id.upgradeDetailLevel);
        TextView nextView = upgradeDetailOverlay.findViewById(R.id.upgradeDetailNext);

        int level = upgradeManager.getLevel(type);
        int maxLevel = upgradeManager.getMaxLevel(type);

        int titleRes;
        int descriptionRes;
        int nextRes;

        switch (type) {
            case UpgradeManager.TYPE_HEALTH:
                titleRes = R.string.upgrade_health;
                descriptionRes = R.string.upgrade_health_desc;
                nextRes = R.string.upgrade_health_next;
                break;
            case UpgradeManager.TYPE_BULLET_SPEED:
                titleRes = R.string.upgrade_bullet_speed;
                descriptionRes = R.string.upgrade_speed_desc;
                nextRes = R.string.upgrade_speed_next;
                break;
            case UpgradeManager.TYPE_SKILL_TIME:
                titleRes = R.string.upgrade_skill_time;
                descriptionRes = R.string.upgrade_time_desc;
                nextRes = R.string.upgrade_time_next;
                break;
            case UpgradeManager.TYPE_MAGNET:
                titleRes = R.string.upgrade_magnet;
                descriptionRes = R.string.upgrade_magnet_desc;
                nextRes = R.string.upgrade_magnet_next;
                break;
            default:
                return;
        }

        if (titleView != null) {
            titleView.setText(titleRes);
        }
        if (descriptionView != null) {
            descriptionView.setText(descriptionRes);
        }
        if (levelView != null) {
            levelView.setText(getString(
                    R.string.upgrade_level_format, level, maxLevel));
        }
        if (nextView != null) {
            nextView.setText(level >= maxLevel
                    ? getString(R.string.upgrade_max_reached)
                    : getString(nextRes));
        }

        upgradeDetailOverlay.setVisibility(View.VISIBLE);
        upgradeDetailOverlay.bringToFront();

        if (starCount != null) {
            starCount.bringToFront();
        }
    }

    // =====================================================
    // TIỆN ÍCH GIAO DIỆN
    // =====================================================

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable makeBackground(
            int fill, int stroke, int strokeDp, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(strokeDp), stroke);
        return drawable;
    }

    private void setOverlayTitle(
            View overlay, int includeId, String title) {
        View titleInclude = overlay.findViewById(includeId);

        if (titleInclude != null) {
            TextView titleText =
                    titleInclude.findViewById(R.id.overlayTitleText);

            if (titleText != null) {
                titleText.setText(title);
            } else if (titleInclude instanceof TextView) {
                ((TextView) titleInclude).setText(title);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        coins = scoreManager.getCoins();
        updateStarCount();
        updateUpgradeButtons();
        updateAchievements();
        syncSettingsUi();
    }
}

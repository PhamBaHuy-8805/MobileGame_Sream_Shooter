
package com.example.screamshooter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;

public class MainActivity extends Activity {

    private View btnStart, btnUpgrade, btnAchievement;
    private View btnSettings, btnLeaderboard, btnLogin;

    private View settingsOverlay, upgradeOverlay;
    private View upgradeDetailOverlay, achievementOverlay;

    private TextView starCount;

    private int coins = 500;

    private int healthLevel = 0;
    private int speedLevel = 0;
    private int timeLevel = 0;
    private int magnetLevel = 0;

    private final int[] healthPrices = {100, 1000, 5000, 10000, 20000};
    private final int[] speedPrices = {500, 1000, 5000};
    private final int[] timePrices = {500, 1000, 5000};
    private final int[] magnetPrices = {10000, 20000};


    // Thành tích mẫu
    private final String[] achievementNames = {
            "CHIẾN BINH VŨ TRỤ",
            "BẤT KHẢ CHIẾN BẠI",
            "TAY SĂN BÁU VẬT"
    };

    private final String[] achievementDescriptions = {
            "Đạt điểm số trong một lần chơi",
            "Hoàn thành các trận chơi",
            "Thu thập sao trong game"
    };

    private final String[] achievementIcons = {"🏆", "💗", "🧲"};

    // Các mốc thành tích tăng dần
    private final int[][] achievementMilestones = {
            {100, 500, 1000, 2000, 5000},
            {5, 10, 20, 50, 100},
            {200, 500, 1000, 2000, 5000}
    };

    // Số sao thưởng tương ứng với từng mốc
    private final int[][] achievementRewards = {
            {100, 200, 500, 1000, 2000},
            {200, 500, 1000, 2000, 5000},
            {200, 500, 1000, 2000, 5000}
    };

    // Tiến trình mẫu của từng thành tích.
    // Sau này nối các giá trị này với dữ liệu thật của GameView.
    private final int[] achievementProgress = {0, 0, 0};

    // Mỗi thành tích có mốc hiện tại và trạng thái nhận thưởng riêng.
    private final int[] achievementStage = {0, 0, 0};
    private final boolean[] achievementClaimed = {false, false, false};

    private final TextView[] achievementProgressTexts = new TextView[3];
    private final TextView[] achievementRewardButtons = new TextView[3];
    private final TextView[] achievementBars = new TextView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        upgradeDetailOverlay =
                upgradeOverlay.findViewById(R.id.upgradeDetailOverlay);

        starCount = upgradeOverlay.findViewById(R.id.tvStarCount);

        // Tạo overlay thành tích bằng code
        createAchievementOverlay(root);

        // BẮT ĐẦU GAME
        btnStart.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(
                        MainActivity.this, GameView.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(
                        MainActivity.this,
                        "Không thể mở màn chơi!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // MỞ NÂNG CẤP
        btnUpgrade.setOnClickListener(v -> {
            hideOtherOverlays();
            upgradeOverlay.setVisibility(View.VISIBLE);
            upgradeOverlay.bringToFront();

            setOverlayTitle(upgradeOverlay, R.id.upgradeTitle, "NÂNG CẤP");
            updateStarCount();
            updateUpgradeButtons();
        });

        // MỞ CÀI ĐẶT
        btnSettings.setOnClickListener(v -> {
            hideOtherOverlays();
            settingsOverlay.setVisibility(View.VISIBLE);
            settingsOverlay.bringToFront();

            setOverlayTitle(settingsOverlay, R.id.settingsTitle, "CÀI ĐẶT");
        });

        View closeSettings = settingsOverlay.findViewById(R.id.settingsClose);
        if (closeSettings != null) {
            closeSettings.setOnClickListener(v ->
                    settingsOverlay.setVisibility(View.GONE));
        }

        // MỞ THÀNH TÍCH
        btnAchievement.setOnClickListener(v -> {
            hideOtherOverlays();
            achievementOverlay.setVisibility(View.VISIBLE);
            achievementOverlay.bringToFront();
            updateAchievements();
        });

        // ĐÓNG THÀNH TÍCH
        // Nút đóng đã được thiết lập trong createAchievementOverlay().

        setupUpgradeSystem();

        btnLeaderboard.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Chức năng bảng xếp hạng đang được phát triển",
                        Toast.LENGTH_SHORT
                ).show());

        btnLogin.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Chức năng đăng nhập đang được phát triển",
                        Toast.LENGTH_SHORT
                ).show());
    }

    private void hideOtherOverlays() {
        settingsOverlay.setVisibility(View.GONE);
        upgradeOverlay.setVisibility(View.GONE);
        achievementOverlay.setVisibility(View.GONE);
    }

    // =====================================================
    // TẠO GIAO DIỆN THÀNH TÍCH
    // =====================================================

    private void createAchievementOverlay(ViewGroup root) {

        FrameLayout overlay = new FrameLayout(this);
        overlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlay.setBackgroundColor(Color.parseColor("#CC000000"));
        overlay.setVisibility(View.GONE);
        overlay.setClickable(true);
        overlay.setFocusable(true);

        FrameLayout panel = new FrameLayout(this);
        FrameLayout.LayoutParams panelParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

        panelParams.setMargins(
                dp(24),
                dp(18),
                dp(24),
                dp(18)
        );

        panelParams.gravity = Gravity.CENTER;
        panel.setLayoutParams(panelParams);
        panel.setLayoutParams(panelParams);
        panel.setBackground(makeBackground("#625B70", "#17141F", 5, 12));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(42), dp(18), dp(8));

        FrameLayout.LayoutParams contentParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
        content.setLayoutParams(contentParams);

        for (int i = 0; i < achievementNames.length; i++) {
            final int index = i;

            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                            1
                    );
            rowParams.bottomMargin = dp(2);
            row.setLayoutParams(rowParams);

            TextView icon = new TextView(this);
            icon.setText(achievementIcons[i]);
            icon.setTextSize(27);
            icon.setGravity(Gravity.CENTER);
            icon.setBackground(makeBackground("#484456", "#17141F", 3, 8));

            LinearLayout.LayoutParams iconParams =
                    new LinearLayout.LayoutParams(dp(52), dp(58));
            iconParams.rightMargin = dp(10);
            row.addView(icon, iconParams);

            LinearLayout middle = new LinearLayout(this);
            middle.setOrientation(LinearLayout.VERTICAL);
            middle.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams middleParams =
                    new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.MATCH_PARENT, 1);
            row.addView(middle, middleParams);

            TextView title = new TextView(this);
            title.setText(achievementNames[i]);
            title.setTextColor(Color.WHITE);
            title.setTextSize(14);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            middle.addView(title);

            TextView description = new TextView(this);
            description.setText(achievementDescriptions[i]);
            description.setTextColor(Color.LTGRAY);
            description.setTextSize(10);
            middle.addView(description);

            TextView progress = new TextView(this);
            progress.setTextColor(Color.WHITE);
            progress.setTextSize(11);
            progress.setGravity(Gravity.CENTER);
            achievementProgressTexts[i] = progress;
            middle.addView(progress);

            TextView bar = new TextView(this);
            bar.setBackground(makeBackground("#302D3B", "#17141F", 2, 5));
            achievementBars[i] = bar;

            LinearLayout.LayoutParams barParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, dp(12));
            barParams.topMargin = dp(3);
            middle.addView(bar, barParams);

            TextView reward = new TextView(this);
            reward.setText("100 ★");
            reward.setTextColor(Color.WHITE);
            reward.setTextSize(12);
            reward.setGravity(Gravity.CENTER);
            reward.setBackground(makeBackground("#777080", "#17141F", 2, 6));
            reward.setClickable(true);
            reward.setFocusable(true);

            LinearLayout.LayoutParams rewardParams =
                    new LinearLayout.LayoutParams(dp(68), dp(34));
            rewardParams.leftMargin = dp(10);
            row.addView(reward, rewardParams);

            achievementRewardButtons[i] = reward;

            reward.setOnClickListener(v -> claimAchievement(index));

            content.addView(row);
        }

        panel.addView(content);

        TextView title = new TextView(this);
        title.setText("THÀNH TÍCH");
        title.setTextColor(Color.parseColor("#FFF0A0"));
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setBackground(makeBackground("#A73830", "#17141F", 3, 8));

        FrameLayout.LayoutParams titleParams =
                new FrameLayout.LayoutParams(dp(220), dp(42));
        titleParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        panel.addView(title, titleParams);

        TextView close = new TextView(this);
        close.setText("×");
        close.setTextColor(Color.WHITE);
        close.setTextSize(34);
        close.setGravity(Gravity.CENTER);
        close.setBackground(makeBackground("#8F3047", "#17141F", 3, 8));
        close.setClickable(true);
        close.setFocusable(true);

        FrameLayout.LayoutParams closeParams =
                new FrameLayout.LayoutParams(dp(48), dp(48));
        closeParams.gravity = Gravity.TOP | Gravity.END;
        closeParams.rightMargin = dp(-3);
        closeParams.topMargin = dp(-3);
        panel.addView(close, closeParams);

        close.setOnClickListener(v ->
                achievementOverlay.setVisibility(View.GONE));

        overlay.addView(panel);
        root.addView(overlay);
        achievementOverlay = overlay;
    }

// =====================================================
// CẬP NHẬT GIAO DIỆN THÀNH TÍCH
// =====================================================

    private void updateAchievements() {
        for (int i = 0; i < achievementNames.length; i++) {

            int stage = achievementStage[i];
            int[] milestones = achievementMilestones[i];
            int[] rewards = achievementRewards[i];

            boolean completed = stage >= milestones.length;

            int target = completed
                    ? milestones[milestones.length - 1]
                    : milestones[stage];

            int rewardAmount = completed
                    ? 0
                    : rewards[stage];

            boolean canClaim = !completed
                    && achievementProgress[i] >= target;

            // Hiển thị tiến trình
            if (achievementProgressTexts[i] != null) {
                achievementProgressTexts[i].setText(
                        completed
                                ? "Đã hoàn thành tất cả mốc"
                                : achievementProgress[i] + "/" + target
                );
            }

            // Nút nhận thưởng
            TextView button = achievementRewardButtons[i];

            if (button != null) {
                button.setText(
                        completed ? "XONG" : rewardAmount + " ★"
                );

                button.setEnabled(canClaim);

                button.setBackground(makeBackground(
                        canClaim
                                ? Color.rgb(255, 210, 0)
                                : Color.rgb(119, 112, 128),
                        Color.BLACK,
                        2,
                        6
                ));

                button.setTextColor(
                        canClaim
                                ? Color.rgb(25, 20, 30)
                                : Color.WHITE
                );
            }

            // Thanh tiến trình
            if (achievementBars[i] != null) {
                float ratio = completed
                        ? 1f
                        : Math.min(
                        1f,
                        achievementProgress[i] / (float) target
                );

                achievementBars[i].setBackground(
                        makeProgressBackground(ratio)
                );
            }
        }
    }


// =====================================================
// NHẬN THƯỞNG THÀNH TÍCH
// =====================================================

    private void claimAchievement(int index) {

        // Kiểm tra chỉ số thành tích
        if (index < 0 || index >= achievementNames.length) {
            return;
        }

        // Lấy mốc và phần thưởng của thành tích được chọn
        int[] milestones = achievementMilestones[index];
        int[] rewards = achievementRewards[index];

        int stage = achievementStage[index];

        // Đã hoàn thành tất cả các mốc
        if (stage >= milestones.length) {
            Toast.makeText(
                    this,
                    "Bạn đã hoàn thành thành tích này!",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Mốc cần đạt hiện tại và số sao được thưởng
        int target = milestones[stage];
        int reward = rewards[stage];

        // Chưa đủ điều kiện nhận thưởng
        if (achievementProgress[index] < target) {
            Toast.makeText(
                    this,
                    "Chưa đạt mốc " + target + "!",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Cộng sao thưởng
        coins += reward;

        // Chuyển sang mốc tiếp theo để không nhận lại mốc cũ
        achievementStage[index]++;

        // Cập nhật giao diện thành tích và số sao
        updateAchievements();
        updateStarCount();
        updateUpgradeButtons();

        // Thông báo nhận thưởng thành công
        Toast.makeText(
                this,
                "Nhận thành công " + reward + " ★!",
                Toast.LENGTH_SHORT
        ).show();
    }

    // Hàm để gameplay gọi khi cập nhật tiến trình thành tích.
    // achievementIndex: 0 = điểm, 1 = số trận, 2 = sao thu thập.
    public void addAchievementProgress(int achievementIndex, int amount) {
        if (achievementIndex < 0
                || achievementIndex >= achievementProgress.length
                || amount <= 0) {
            return;
        }

        runOnUiThread(() -> {
            achievementProgress[achievementIndex] += amount;
            updateAchievements();
        });
    }

    // =====================================================
    // TIỆN ÍCH GIAO DIỆN
    // =====================================================

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable makeBackground(
            String fill, String stroke, int strokeDp, int radiusDp) {
        return makeBackground(
                Color.parseColor(fill),
                Color.parseColor(stroke),
                strokeDp,
                radiusDp
        );
    }

    private GradientDrawable makeBackground(
            int fill, int stroke, int strokeDp, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(strokeDp), stroke);
        return drawable;
    }

    private LayerDrawable makeProgressBackground(float ratio) {

        // Nền thanh tiến trình màu xám đậm
        GradientDrawable track = makeBackground(
                "#302D3B",
                "#17141F",
                1,
                5
        );

        // Phần tiến trình màu vàng
        ClipDrawable progress = new ClipDrawable(
                new ColorDrawable(Color.rgb(255, 210, 0)),
                Gravity.LEFT,
                ClipDrawable.HORIZONTAL
        );

        // Level từ 0 đến 10000 tương ứng 0% đến 100%
        progress.setLevel((int) (Math.max(0f, Math.min(1f, ratio)) * 10000));

        // Ghép nền và phần tiến trình thành một thanh
        LayerDrawable layers = new LayerDrawable(
                new android.graphics.drawable.Drawable[]{
                        track,
                        progress
                }
        );

        return layers;
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

    // =====================================================
    // HỆ THỐNG NÂNG CẤP
    // =====================================================

    private View upgradeView(int id) {
        return upgradeOverlay.findViewById(id);
    }

    private void setupUpgradeSystem() {
        setOverlayTitle(upgradeOverlay, R.id.upgradeTitle, "NÂNG CẤP");

        upgradeView(R.id.buyHealth).setOnClickListener(v -> buyUpgrade(1));
        upgradeView(R.id.buySpeed).setOnClickListener(v -> buyUpgrade(2));
        upgradeView(R.id.buyTime).setOnClickListener(v -> buyUpgrade(3));
        upgradeView(R.id.buyMagnet).setOnClickListener(v -> buyUpgrade(4));

        upgradeView(R.id.iconHealth).setOnClickListener(
                v -> showUpgradeDetail("MÁU"));
        upgradeView(R.id.iconSpeed).setOnClickListener(
                v -> showUpgradeDetail("TỐC ĐỘ ĐẠN"));
        upgradeView(R.id.iconTime).setOnClickListener(
                v -> showUpgradeDetail("THỜI GIAN KỸ NĂNG"));
        upgradeView(R.id.iconMagnet).setOnClickListener(
                v -> showUpgradeDetail("NAM CHÂM"));

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

    private int getUpgradeLevel(int type) {
        switch (type) {
            case 1: return healthLevel;
            case 2: return speedLevel;
            case 3: return timeLevel;
            case 4: return magnetLevel;
            default: return 0;
        }
    }

    private int getMaxLevel(int type) {
        switch (type) {
            case 1: return 5;
            case 2:
            case 3: return 3;
            case 4: return 2;
            default: return 0;
        }
    }

    private int getUpgradePrice(int type) {
        int level = getUpgradeLevel(type);
        int[] prices;

        switch (type) {
            case 1: prices = healthPrices; break;
            case 2: prices = speedPrices; break;
            case 3: prices = timePrices; break;
            case 4: prices = magnetPrices; break;
            default: return Integer.MAX_VALUE;
        }

        return level >= prices.length
                ? Integer.MAX_VALUE : prices[level];
    }

    private void buyUpgrade(int type) {
        int level = getUpgradeLevel(type);

        if (level >= getMaxLevel(type)) {
            Toast.makeText(this, "Đã đạt cấp tối đa!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int price = getUpgradePrice(type);

        if (coins < price) {
            Toast.makeText(this, "Không đủ sao! Cần " + price + " ★",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        coins -= price;

        switch (type) {
            case 1: healthLevel++; break;
            case 2: speedLevel++; break;
            case 3: timeLevel++; break;
            case 4: magnetLevel++; break;
        }

        updateStarCount();
        updateUpgradeButtons();

        Toast.makeText(this,
                "Nâng cấp thành công! Còn " + coins + " ★",
                Toast.LENGTH_SHORT).show();
    }

    private void updateStarCount() {
        if (starCount != null) {
            starCount.setText(coins + " ★");
        }
    }

    private void updateUpgradeButtons() {
        updatePriceButton(R.id.buyHealth, 1);
        updatePriceButton(R.id.buySpeed, 2);
        updatePriceButton(R.id.buyTime, 3);
        updatePriceButton(R.id.buyMagnet, 4);

        updateLevelCells(new int[]{
                R.id.health1, R.id.health2, R.id.health3,
                R.id.health4, R.id.health5
        }, healthLevel);

        updateLevelCells(new int[]{
                R.id.speed1, R.id.speed2, R.id.speed3
        }, speedLevel);

        updateLevelCells(new int[]{
                R.id.time1, R.id.time2, R.id.time3
        }, timeLevel);

        updateLevelCells(new int[]{
                R.id.magnet1, R.id.magnet2
        }, magnetLevel);
    }

    private void updatePriceButton(int buttonId, int type) {
        TextView button = upgradeOverlay.findViewById(buttonId);
        if (button == null) return;

        int level = getUpgradeLevel(type);
        boolean isMax = level >= getMaxLevel(type);
        int price = getUpgradePrice(type);
        boolean canBuy = !isMax && coins >= price;

        button.setText(isMax ? "MAX" : String.valueOf(price));
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
            if (cell == null) continue;

            cell.setBackgroundResource(i < currentLevel
                    ? R.drawable.bg_upgrade_level_on
                    : R.drawable.bg_upgrade_level_off);
        }
    }

    // =====================================================
    // CHI TIẾT NÂNG CẤP
    // =====================================================

    private void showUpgradeDetail(String title) {
        if (upgradeDetailOverlay == null) return;

        TextView titleView = upgradeDetailOverlay.findViewById(
                R.id.upgradeDetailTitle);
        TextView descriptionView = upgradeDetailOverlay.findViewById(
                R.id.upgradeDetailDescription);
        TextView levelView = upgradeDetailOverlay.findViewById(
                R.id.upgradeDetailLevel);
        TextView nextView = upgradeDetailOverlay.findViewById(
                R.id.upgradeDetailNext);

        int level;
        int maxLevel;
        String description;
        String nextEffect;

        switch (title) {
            case "MÁU":
                level = healthLevel;
                maxLevel = 5;
                description = "Tăng khả năng chịu sát thương của tàu.";
                nextEffect = "Cấp tiếp theo: Tăng khả năng sống sót.";
                break;

            case "TỐC ĐỘ ĐẠN":
                level = speedLevel;
                maxLevel = 3;
                description = "Tăng tốc độ tấn công để tiêu diệt quái vật.";
                nextEffect = "Cấp tiếp theo: Tăng tốc độ tấn công.";
                break;

            case "THỜI GIAN KỸ NĂNG":
                level = timeLevel;
                maxLevel = 3;
                description = "Kéo dài thời gian hiệu lực của kỹ năng.";
                nextEffect = "Cấp tiếp theo: Kéo dài thời gian kỹ năng.";
                break;

            case "NAM CHÂM":
                level = magnetLevel;
                maxLevel = 2;
                description = "Hút vật phẩm hoặc sao ở gần tàu.";
                nextEffect = "Cấp tiếp theo: Tăng khả năng hút vật phẩm.";
                break;

            default:
                return;
        }

        if (titleView != null) titleView.setText(title);
        if (descriptionView != null) descriptionView.setText(description);

        if (levelView != null) {
            levelView.setText("Cấp độ hiện tại: " + level + "/" + maxLevel);
        }

        if (nextView != null) {
            nextView.setText(level >= maxLevel
                    ? "Đã đạt cấp độ tối đa!" : nextEffect);
        }

        upgradeDetailOverlay.setVisibility(View.VISIBLE);
        upgradeDetailOverlay.bringToFront();

        if (starCount != null) starCount.bringToFront();
    }
}

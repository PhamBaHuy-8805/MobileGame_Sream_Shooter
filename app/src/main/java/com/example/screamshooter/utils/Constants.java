package com.example.screamshooter.utils;

public class Constants {
    // Cấu hình âm thanh kích hoạt Skill (FR07, NFR02)
    public static final float AUDIO_TRIGGER_THRESHOLD_DB = 70.0f; // Ngưỡng mặc định > 70 dB
    public static final long SKILL_DURATION_MS = 5000; // Thời gian hiệu lực Skill đặc biệt: 5 giây

    // Cấu hình Game Loop (NFR01, NFR03)
    public static final int TARGET_FPS = 60;
    public static final long FRAME_TIME_MS = 1000 / TARGET_FPS;

    // Cấu hình Auto-fire
    public static final long AUTO_FIRE_INTERVAL_MS = 180; // Bắn đạn tự động mỗi 180ms
    public static final int STAR_SCORE_VALUE = 10; // Điểm cộng khi nhặt được sao

    // Cấu hình Năng lượng (FR06)
    public static final int MAX_ENERGY = 100;
    public static final int ENERGY_PER_KILL = 10; // +10% mỗi lần diệt quái

    // SharedPreferences Keys
    public static final String PREFS_NAME = "scream_shooter_prefs";
    public static final String KEY_HIGH_SCORE = "key_high_score";
    public static final String KEY_LEADERBOARD = "key_leaderboard_json";
    public static final String KEY_COINS = "key_coins";
}

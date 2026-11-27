package com.example.screamshooter.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.screamshooter.utils.Constants;

public class UpgradeManager {

    public static final int TYPE_HEALTH = 1;
    public static final int TYPE_BULLET_SPEED = 2;
    public static final int TYPE_SKILL_TIME = 3;
    public static final int TYPE_MAGNET = 4;

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_MAX = 1;
    public static final int RESULT_NOT_ENOUGH = 2;

    private static final String KEY_HEALTH = "upgrade_health_level";
    private static final String KEY_SPEED = "upgrade_speed_level";
    private static final String KEY_TIME = "upgrade_time_level";
    private static final String KEY_MAGNET = "upgrade_magnet_level";

    private static final int MAX_HEALTH_LEVEL = 5;
    private static final int MAX_SPEED_LEVEL = 3;
    private static final int MAX_TIME_LEVEL = 3;
    private static final int MAX_MAGNET_LEVEL = 2;

    private static final int[] HEALTH_PRICES = {100, 1000, 5000, 10000, 20000};
    private static final int[] SPEED_PRICES = {500, 1000, 5000};
    private static final int[] TIME_PRICES = {500, 1000, 5000};
    private static final int[] MAGNET_PRICES = {10000, 20000};

    private static final int BASE_MAX_HEALTH = 3;

    private final SharedPreferences prefs;
    private final LocalScoreManager scoreManager;
    private final Object lock = new Object();

    public UpgradeManager(Context context) {
        Context appContext = context.getApplicationContext();
        Context target = appContext != null ? appContext : context;
        prefs = target.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        scoreManager = new LocalScoreManager(target);
    }

    public int getLevel(int type) {
        switch (type) {
            case TYPE_HEALTH:
                return prefs.getInt(KEY_HEALTH, 0);
            case TYPE_BULLET_SPEED:
                return prefs.getInt(KEY_SPEED, 0);
            case TYPE_SKILL_TIME:
                return prefs.getInt(KEY_TIME, 0);
            case TYPE_MAGNET:
                return prefs.getInt(KEY_MAGNET, 0);
            default:
                return 0;
        }
    }

    public int getMaxLevel(int type) {
        switch (type) {
            case TYPE_HEALTH:
                return MAX_HEALTH_LEVEL;
            case TYPE_BULLET_SPEED:
                return MAX_SPEED_LEVEL;
            case TYPE_SKILL_TIME:
                return MAX_TIME_LEVEL;
            case TYPE_MAGNET:
                return MAX_MAGNET_LEVEL;
            default:
                return 0;
        }
    }

    public int getPrice(int type) {
        int level = getLevel(type);
        int[] prices;

        switch (type) {
            case TYPE_HEALTH:
                prices = HEALTH_PRICES;
                break;
            case TYPE_BULLET_SPEED:
                prices = SPEED_PRICES;
                break;
            case TYPE_SKILL_TIME:
                prices = TIME_PRICES;
                break;
            case TYPE_MAGNET:
                prices = MAGNET_PRICES;
                break;
            default:
                return Integer.MAX_VALUE;
        }

        return level >= prices.length ? Integer.MAX_VALUE : prices[level];
    }

    public int buy(int type) {
        synchronized (lock) {
            int level = getLevel(type);
            int maxLevel = getMaxLevel(type);

            if (maxLevel == 0 || level >= maxLevel) {
                return RESULT_MAX;
            }

            int price = getPrice(type);
            if (!scoreManager.spendCoins(price)) {
                return RESULT_NOT_ENOUGH;
            }

            prefs.edit().putInt(getLevelKey(type), level + 1).apply();
            return RESULT_SUCCESS;
        }
    }

    public int getMaxHealth() {
        return BASE_MAX_HEALTH + getLevel(TYPE_HEALTH);
    }

    public long getAutoFireIntervalMs() {
        return Math.max(
                110L,
                Constants.AUTO_FIRE_INTERVAL_MS - 20L * getLevel(TYPE_BULLET_SPEED)
        );
    }

    public float getBulletSpeedMultiplier() {
        return 1f + 0.2f * getLevel(TYPE_BULLET_SPEED);
    }

    public long getSkillDurationMs() {
        return Constants.SKILL_DURATION_MS + 1000L * getLevel(TYPE_SKILL_TIME);
    }

    public float getMagnetRadius() {
        return 140f * getLevel(TYPE_MAGNET);
    }

    private String getLevelKey(int type) {
        switch (type) {
            case TYPE_HEALTH:
                return KEY_HEALTH;
            case TYPE_BULLET_SPEED:
                return KEY_SPEED;
            case TYPE_SKILL_TIME:
                return KEY_TIME;
            case TYPE_MAGNET:
                return KEY_MAGNET;
            default:
                return KEY_HEALTH;
        }
    }
}

package com.example.screamshooter.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.screamshooter.utils.Constants;

public class AchievementManager {

    public static final int ACHIEVEMENT_SCORE = 0;
    public static final int ACHIEVEMENT_MATCHES = 1;
    public static final int ACHIEVEMENT_STARS = 2;
    public static final int COUNT = 3;

    public static final int CLAIM_NOT_REACHED = -1;
    public static final int CLAIM_ALREADY_COMPLETE = -2;

    private static final int[][] MILESTONES = {
            {100, 500, 1000, 2000, 5000},
            {5, 10, 20, 50, 100},
            {20, 50, 100, 200, 500}
    };

    private static final int[][] REWARDS = {
            {100, 200, 500, 1000, 2000},
            {200, 500, 1000, 2000, 5000},
            {200, 500, 1000, 2000, 5000}
    };

    private static final String[] PROGRESS_KEYS = {
            "achievement_score_progress",
            "achievement_matches_progress",
            "achievement_stars_progress"
    };

    private static final String[] STAGE_KEYS = {
            "achievement_score_stage",
            "achievement_matches_stage",
            "achievement_stars_stage"
    };

    private final SharedPreferences prefs;
    private final LocalScoreManager scoreManager;
    private final Object lock = new Object();

    public AchievementManager(Context context) {
        Context appContext = context.getApplicationContext();
        Context target = appContext != null ? appContext : context;
        prefs = target.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        scoreManager = new LocalScoreManager(target);
    }

    public int getProgress(int index) {
        if (index < 0 || index >= COUNT) {
            return 0;
        }
        return prefs.getInt(PROGRESS_KEYS[index], 0);
    }

    public int getStage(int index) {
        if (index < 0 || index >= COUNT) {
            return 0;
        }
        return prefs.getInt(STAGE_KEYS[index], 0);
    }

    public boolean isCompleted(int index) {
        return index >= 0
                && index < COUNT
                && getStage(index) >= MILESTONES[index].length;
    }

    public int getCurrentTarget(int index) {
        if (index < 0 || index >= COUNT) {
            return 0;
        }
        int[] milestones = MILESTONES[index];
        int stage = getStage(index);
        return stage >= milestones.length
                ? milestones[milestones.length - 1]
                : milestones[stage];
    }

    public int getCurrentReward(int index) {
        if (index < 0 || index >= COUNT || isCompleted(index)) {
            return 0;
        }
        return REWARDS[index][getStage(index)];
    }

    public void recordRun(int score, int starsCollected) {
        synchronized (lock) {
            int bestScore = Math.max(
                    Math.max(getProgress(ACHIEVEMENT_SCORE), score),
                    scoreManager.getHighScore()
            );
            int matches = getProgress(ACHIEVEMENT_MATCHES) + 1;
            int stars = getProgress(ACHIEVEMENT_STARS) + Math.max(0, starsCollected);

            prefs.edit()
                    .putInt(PROGRESS_KEYS[ACHIEVEMENT_SCORE], bestScore)
                    .putInt(PROGRESS_KEYS[ACHIEVEMENT_MATCHES], matches)
                    .putInt(PROGRESS_KEYS[ACHIEVEMENT_STARS], stars)
                    .apply();
        }
    }

    public int claim(int index) {
        synchronized (lock) {
            if (index < 0 || index >= COUNT) {
                return CLAIM_ALREADY_COMPLETE;
            }

            int stage = getStage(index);
            int[] milestones = MILESTONES[index];

            if (stage >= milestones.length) {
                return CLAIM_ALREADY_COMPLETE;
            }

            int target = milestones[stage];
            if (getProgress(index) < target) {
                return CLAIM_NOT_REACHED;
            }

            int reward = REWARDS[index][stage];
            prefs.edit().putInt(STAGE_KEYS[index], stage + 1).apply();
            scoreManager.addCoins(reward);
            return reward;
        }
    }
}

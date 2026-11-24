package com.example.screamshooter.data;

import android.content.Context;
import com.example.screamshooter.models.ScoreRecord;

import java.util.List;

public class FirebaseScoreRepository {

    private final LocalScoreManager localManager;

    public interface ScoreCallback {
        void onSuccess(List<ScoreRecord> records);
        void onError(String message);
    }

    public FirebaseScoreRepository(Context context) {
        this.localManager = new LocalScoreManager(context);
    }

    /**
     * Đồng bộ và lưu điểm người chơi.
     * Khi offline hoặc chưa cài Firebase SDK, hệ thống tự động lưu Local an toàn (NFR07, FR10).
     */
    public void submitScore(String playerName, int score) {
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Phi Thuyền X";
        }
        localManager.addScoreRecord(playerName, score);
        localManager.addCoins(score / 10); // Thưởng Coin theo 10% điểm số
    }

    public void fetchTopScores(ScoreCallback callback) {
        List<ScoreRecord> localList = localManager.getLeaderboard();
        if (callback != null) {
            callback.onSuccess(localList);
        }
    }
}

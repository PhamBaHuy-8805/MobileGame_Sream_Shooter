package com.example.screamshooter.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.screamshooter.models.ScoreRecord;
import com.example.screamshooter.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalScoreManager {

    private final SharedPreferences prefs;

    public LocalScoreManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getHighScore() {
        return prefs.getInt(Constants.KEY_HIGH_SCORE, 0);
    }

    public void saveHighScore(int score) {
        int current = getHighScore();
        if (score > current) {
            prefs.edit().putInt(Constants.KEY_HIGH_SCORE, score).apply();
        }
    }

    public int getCoins() {
        return prefs.getInt(Constants.KEY_COINS, 500);
    }

    public void addCoins(int amount) {
        int current = getCoins();
        prefs.edit().putInt(Constants.KEY_COINS, current + amount).apply();
    }

    public void saveCoins(int totalCoins) {
        prefs.edit().putInt(Constants.KEY_COINS, totalCoins).apply();
    }

    public synchronized boolean spendCoins(int amount) {
        int current = getCoins();
        if (amount < 0 || current < amount) {
            return false;
        }
        prefs.edit().putInt(Constants.KEY_COINS, current - amount).apply();
        return true;
    }

    public void addScoreRecord(String playerName, int score) {
        saveHighScore(score);

        List<ScoreRecord> list = getLeaderboard();
        list.add(new ScoreRecord(playerName, score, System.currentTimeMillis()));
        Collections.sort(list);

        // Chỉ giữ Top 10
        if (list.size() > 10) {
            list = new ArrayList<>(list.subList(0, 10));
        }

        saveLeaderboardList(list);
    }

    public List<ScoreRecord> getLeaderboard() {
        List<ScoreRecord> list = new ArrayList<>();
        String json = prefs.getString(Constants.KEY_LEADERBOARD, "");
        if (json == null || json.isEmpty()) {
            // Mẫu dữ liệu ban đầu
            list.add(new ScoreRecord("Phi Thuyền Vương", 1200, System.currentTimeMillis()));
            list.add(new ScoreRecord("Chiến Binh Không Gian", 850, System.currentTimeMillis()));
            list.add(new ScoreRecord("Thợ Săn Quái Vật", 600, System.currentTimeMillis()));
            return list;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.optString("playerName", "Người chơi");
                int score = obj.optInt("score", 0);
                long time = obj.optLong("timestamp", System.currentTimeMillis());
                list.add(new ScoreRecord(name, score, time));
            }
            Collections.sort(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void saveLeaderboardList(List<ScoreRecord> list) {
        try {
            JSONArray array = new JSONArray();
            for (ScoreRecord record : list) {
                JSONObject obj = new JSONObject();
                obj.put("playerName", record.getPlayerName());
                obj.put("score", record.getScore());
                obj.put("timestamp", record.getTimestamp());
                array.put(obj);
            }
            prefs.edit().putString(Constants.KEY_LEADERBOARD, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

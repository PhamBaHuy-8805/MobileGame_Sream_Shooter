package com.example.screamshooter.models;

import java.io.Serializable;

public class ScoreRecord implements Serializable, Comparable<ScoreRecord> {
    private String playerName;
    private int score;
    private long timestamp;

    public ScoreRecord() {
    }

    public ScoreRecord(String playerName, int score, long timestamp) {
        this.playerName = playerName;
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(ScoreRecord o) {
        // Sắp xếp điểm số giảm dần
        return Integer.compare(o.score, this.score);
    }
}

package com.example.screamshooter.data.model;

public class Score {
    private String username;
    private int score;
    private long timestamp;

    public Score() {
        // Constructor rỗng bắt buộc cho Firestore deserialization
    }

    public Score(String username, int score, long timestamp) {
        this.username = username;
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}
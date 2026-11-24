package com.example.screamshooter.ui.achievement;

public class ScoreModel {
    private String playerName;
    private int score;

    public ScoreModel(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }
}
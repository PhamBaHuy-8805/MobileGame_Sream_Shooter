package com.example.screamshooter.data.model;

public class PlayerProfile {

    public static final int MAX_LEVEL = 5;

    public static final String HEALTH = "healthLevel";
    public static final String COIN = "coinLevel";
    public static final String FIRE_RATE = "fireRateLevel";
    public static final String ENERGY = "energyLevel";
    public static final String SPEED = "speedLevel";

    private static final long[] UPGRADE_COSTS = {
            1000,
            2500,
            5000,
            10000,
            20000
    };

    public long coins = 0;
    public long highScore = 0;
    public long totalKills = 0;
    public long totalGames = 0;

    public int healthLevel = 0;
    public int coinLevel = 0;
    public int fireRateLevel = 0;
    public int energyLevel = 0;
    public int speedLevel = 0;

    public PlayerProfile() {
    }

    public int getStartingLives() {
        return 3 + healthLevel;
    }

    public int getCoinsPerKill() {
        return 100 + coinLevel * 100;
    }

    public int getFireInterval() {
        return 300 - fireRateLevel * 30;
    }

    public int getEnergyPerKill() {
        return 15 + energyLevel * 3;
    }

    public float getMovementMultiplier() {
        return 1.0f + speedLevel * 0.1f;
    }

    public int getLevel(String type) {

        switch (type) {

            case HEALTH:
                return healthLevel;

            case COIN:
                return coinLevel;

            case FIRE_RATE:
                return fireRateLevel;

            case ENERGY:
                return energyLevel;

            case SPEED:
                return speedLevel;

            default:
                throw new IllegalArgumentException(
                        "Unknown upgrade type"
                );
        }
    }

    public static long getUpgradeCost(int currentLevel) {

        if (currentLevel < 0 ||
                currentLevel >= MAX_LEVEL) {

            return -1;
        }

        return UPGRADE_COSTS[currentLevel];
    }
}
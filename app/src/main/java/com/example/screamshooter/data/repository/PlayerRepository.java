package com.example.screamshooter.data.repository;

import com.example.screamshooter.data.model.PlayerProfile;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class PlayerRepository {

    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    private DocumentReference getPlayerRef() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            throw new IllegalStateException(
                    "AUTH_REQUIRED"
            );
        }

        return db.collection("users")
                .document(user.getUid());
    }

    private Map<String, Object> createDefaultProfile() {

        Map<String, Object> data = new HashMap<>();

        data.put("coins", 0L);
        data.put("highScore", 0L);
        data.put("totalKills", 0L);
        data.put("totalGames", 0L);

        data.put("healthLevel", 0);
        data.put("coinLevel", 0);
        data.put("fireRateLevel", 0);
        data.put("energyLevel", 0);
        data.put("speedLevel", 0);

        data.put("createdAt",
                FieldValue.serverTimestamp());

        return data;
    }

    private long readLong(
            DocumentSnapshot snapshot,
            String field
    ) {

        Long value = snapshot.getLong(field);

        return value == null ? 0L : value;
    }

    private int readLevel(
            DocumentSnapshot snapshot,
            String field
    ) {

        long value = readLong(snapshot, field);

        return (int) Math.max(
                0,
                Math.min(PlayerProfile.MAX_LEVEL, value)
        );
    }

    private PlayerProfile parseProfile(
            DocumentSnapshot snapshot
    ) {

        PlayerProfile profile = new PlayerProfile();

        profile.coins = readLong(snapshot, "coins");

        profile.highScore =
                readLong(snapshot, "highScore");

        profile.totalKills =
                readLong(snapshot, "totalKills");

        profile.totalGames =
                readLong(snapshot, "totalGames");

        profile.healthLevel =
                readLevel(snapshot, PlayerProfile.HEALTH);

        profile.coinLevel =
                readLevel(snapshot, PlayerProfile.COIN);

        profile.fireRateLevel =
                readLevel(snapshot, PlayerProfile.FIRE_RATE);

        profile.energyLevel =
                readLevel(snapshot, PlayerProfile.ENERGY);

        profile.speedLevel =
                readLevel(snapshot, PlayerProfile.SPEED);

        return profile;
    }

    public Task<PlayerProfile> loadProfile() {

        if (auth.getCurrentUser() == null) {

            return Tasks.forException(
                    new IllegalStateException(
                            "AUTH_REQUIRED"
                    )
            );
        }

        DocumentReference ref = getPlayerRef();

        DocumentReference leaderboardRef =
                db.collection("leaderboard")
                        .document(ref.getId());

        return db.runTransaction(transaction -> {

            DocumentSnapshot snapshot =
                    transaction.get(ref);

            if (!snapshot.exists()) {

                DocumentSnapshot oldScore =
                        transaction.get(leaderboardRef);

                Map<String, Object> data =
                        createDefaultProfile();

                long previousScore =
                        readLong(oldScore, "score");

                data.put("highScore", previousScore);

                transaction.set(ref, data);

                PlayerProfile profile =
                        new PlayerProfile();

                profile.highScore = previousScore;

                return profile;
            }

            return parseProfile(snapshot);
        });
    }

    public Task<Void> purchaseUpgrade(String type) {

        if (auth.getCurrentUser() == null) {

            return Tasks.forException(
                    new IllegalStateException(
                            "AUTH_REQUIRED"
                    )
            );
        }

        DocumentReference ref = getPlayerRef();

        return db.runTransaction(transaction -> {

            DocumentSnapshot snapshot =
                    transaction.get(ref);

            if (!snapshot.exists()) {

                throw new IllegalStateException(
                        "PROFILE_NOT_FOUND"
                );
            }

            PlayerProfile profile =
                    parseProfile(snapshot);

            int currentLevel = profile.getLevel(type);

            if (currentLevel >= PlayerProfile.MAX_LEVEL) {

                throw new IllegalStateException(
                        "MAX_LEVEL"
                );
            }

            long cost =
                    PlayerProfile.getUpgradeCost(
                            currentLevel
                    );

            if (profile.coins < cost) {

                throw new IllegalStateException(
                        "NOT_ENOUGH_COINS"
                );
            }

            Map<String, Object> updates =
                    new HashMap<>();

            updates.put(
                    "coins",
                    profile.coins - cost
            );

            updates.put(
                    type,
                    currentLevel + 1
            );

            updates.put(
                    "updatedAt",
                    FieldValue.serverTimestamp()
            );

            transaction.update(ref, updates);

            return null;
        });
    }

    public Task<Void> saveMatchResult(
            String matchId,
            String matchOwnerUid,
            int score,
            int kills,
            long earnedCoins
    ) {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null ||
                !user.getUid().equals(matchOwnerUid)) {

            return Tasks.forException(
                    new IllegalStateException(
                            "AUTH_REQUIRED"
                    )
            );
        }

        if (matchId == null ||
                matchId.isEmpty() ||
                score < 0 ||
                kills < 0 ||
                earnedCoins < 0 ||
                earnedCoins > kills * 600L) {

            return Tasks.forException(
                    new IllegalArgumentException(
                            "INVALID_MATCH"
                    )
            );
        }

        String uid = user.getUid();

        String name = user.getDisplayName();

        if (name == null || name.trim().isEmpty()) {

            name = "Gamer";
        }

        final String playerName = name;

        DocumentReference playerRef =
                db.collection("users").document(uid);

        DocumentReference scoreRef =
                db.collection("leaderboard").document(uid);

        DocumentReference matchRef =
                playerRef.collection("matches")
                        .document(matchId);

        return db.runTransaction(transaction -> {

            DocumentSnapshot matchSnapshot =
                    transaction.get(matchRef);

            // Không cộng thưởng lại nếu trận đã lưu.

            if (matchSnapshot.exists()) {
                return null;
            }

            DocumentSnapshot playerSnapshot =
                    transaction.get(playerRef);

            DocumentSnapshot scoreSnapshot =
                    transaction.get(scoreRef);

            if (!playerSnapshot.exists()) {

                throw new IllegalStateException(
                        "PROFILE_NOT_FOUND"
                );
            }

            PlayerProfile profile =
                    parseProfile(playerSnapshot);

            long oldHighScore = Math.max(
                    profile.highScore,
                    readLong(scoreSnapshot, "score")
            );

            long newHighScore =
                    Math.max(oldHighScore, score);

            Map<String, Object> playerUpdates =
                    new HashMap<>();

            playerUpdates.put(
                    "coins",
                    profile.coins + earnedCoins
            );

            playerUpdates.put(
                    "highScore",
                    newHighScore
            );

            playerUpdates.put(
                    "totalKills",
                    profile.totalKills + kills
            );

            playerUpdates.put(
                    "totalGames",
                    profile.totalGames + 1
            );

            playerUpdates.put(
                    "updatedAt",
                    FieldValue.serverTimestamp()
            );

            transaction.update(
                    playerRef,
                    playerUpdates
            );

            Map<String, Object> leaderboardData =
                    new HashMap<>();

            leaderboardData.put("userId", uid);

            leaderboardData.put(
                    "playerName",
                    playerName
            );

            leaderboardData.put(
                    "score",
                    newHighScore
            );

            leaderboardData.put(
                    "timestamp",
                    System.currentTimeMillis()
            );

            transaction.set(
                    scoreRef,
                    leaderboardData,
                    SetOptions.merge()
            );

            Map<String, Object> matchData =
                    new HashMap<>();

            matchData.put("score", score);
            matchData.put("kills", kills);
            matchData.put("earnedCoins", earnedCoins);

            matchData.put(
                    "playedAt",
                    FieldValue.serverTimestamp()
            );

            transaction.set(matchRef, matchData);

            return null;
        });
    }
}
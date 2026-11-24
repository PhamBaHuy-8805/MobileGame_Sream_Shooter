package com.example.screamshooter.data.repository;

import androidx.annotation.NonNull;

import com.example.screamshooter.data.model.Score;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ScoreRepository {
    private FirebaseFirestore db;
    private static final String COLLECTION_NAME = "leaderboard";

    public interface OnLeaderboardCallback {
        void onSuccess(List<Score> scores);
        void onError(Exception e);
    }

    public ScoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // Lưu điểm số lên Cloud Firestore khi kết thúc ván chơi (FR10)
    public void saveScore(String username, int score, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Score scoreObj = new Score(username, score, System.currentTimeMillis());
        db.collection(COLLECTION_NAME)
                .add(scoreObj)
                .addOnSuccessListener(documentReference -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    // Lấy Top 10 điểm số cao nhất cho Bảng xếp hạng (Luật 8)
    public void getTopScores(final OnLeaderboardCallback callback) {
        db.collection(COLLECTION_NAME)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Score> scoreList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Score score = document.toObject(Score.class);
                            scoreList.add(score);
                        }
                        callback.onSuccess(scoreList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }
}
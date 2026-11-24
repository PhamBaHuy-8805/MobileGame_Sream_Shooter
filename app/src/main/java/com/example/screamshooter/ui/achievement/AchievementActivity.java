package com.example.screamshooter.ui.achievement;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.screamshooter.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AchievementActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<ScoreModel> scoreList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        recyclerView = findViewById(R.id.recyclerViewLeaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        scoreList = new ArrayList<>();
        // Lưu ý: Đảm bảo bạn đã có sẵn LeaderboardAdapter để hiển thị danh sách
        adapter = new LeaderboardAdapter(scoreList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        // Truy vấn lấy Top 10 điểm cao nhất sắp xếp giảm dần (DESCENDING)
        db.collection("leaderboard")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scoreList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String playerName = document.getString("playerName");
                        Long score = document.getLong("score");

                        if (playerName != null && score != null) {
                            scoreList.add(new ScoreModel(playerName, score.intValue()));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải bảng xếp hạng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
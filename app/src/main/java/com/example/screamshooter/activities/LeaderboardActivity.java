package com.example.screamshooter.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.screamshooter.R;
import com.example.screamshooter.data.LocalScoreManager;
import com.example.screamshooter.models.ScoreRecord;

import java.util.List;

public class LeaderboardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        View btnBack = findViewById(R.id.btnBackLeaderboard);
        ListView lvLeaderboard = findViewById(R.id.lvLeaderboard);

        btnBack.setOnClickListener(v -> finish());

        LocalScoreManager scoreManager = new LocalScoreManager(this);
        List<ScoreRecord> records = scoreManager.getLeaderboard();

        LeaderboardAdapter adapter = new LeaderboardAdapter(records);
        lvLeaderboard.setAdapter(adapter);
    }

    private class LeaderboardAdapter extends BaseAdapter {

        private final List<ScoreRecord> list;

        LeaderboardAdapter(List<ScoreRecord> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_leaderboard, parent, false);
            }

            ScoreRecord record = list.get(position);

            TextView tvRank = convertView.findViewById(R.id.tvRank);
            TextView tvPlayerName = convertView.findViewById(R.id.tvPlayerName);
            TextView tvScore = convertView.findViewById(R.id.tvScore);

            tvRank.setText(String.valueOf(position + 1));
            tvPlayerName.setText(record.getPlayerName());
            tvScore.setText(record.getScore() + " Pts");

            return convertView;
        }
    }
}

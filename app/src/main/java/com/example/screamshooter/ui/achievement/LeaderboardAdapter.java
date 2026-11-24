package com.example.screamshooter.ui.achievement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.screamshooter.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter
        extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<ScoreModel> scoreList;

    public LeaderboardAdapter(List<ScoreModel> scoreList) {
        this.scoreList = scoreList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_score, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        ScoreModel score = scoreList.get(position);

        Context context = holder.itemView.getContext();

        Locale locale = context.getResources()
                .getConfiguration()
                .getLocales()
                .get(0);

        NumberFormat numberFormat =
                NumberFormat.getIntegerInstance(locale);

        holder.tvName.setText(
                (position + 1) + ". " + score.getPlayerName()
        );

        holder.tvScore.setText(
                numberFormat.format(score.getScore())
        );

        int colorResource;

        switch (position) {

            case 0:
                colorResource = R.color.game_gold;
                break;

            case 1:
                colorResource = R.color.game_silver;
                break;

            case 2:
                colorResource = R.color.game_bronze;
                break;

            default:
                colorResource = R.color.text_primary;
                break;
        }

        holder.tvName.setTextColor(
                ContextCompat.getColor(context, colorResource)
        );

        holder.tvScore.setTextColor(
                ContextCompat.getColor(
                        context,
                        position < 3
                                ? colorResource
                                : R.color.neon_cyan
                )
        );
    }

    @Override
    public int getItemCount() {
        return scoreList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvScore;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            tvName = itemView.findViewById(
                    R.id.tvPlayerName
            );

            tvScore = itemView.findViewById(
                    R.id.tvPlayerScore
            );
        }
    }
}
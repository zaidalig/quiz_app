// app/src/main/java/com/example/quiz/HighScoreAdapter.java
package com.example.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HighScoreAdapter extends RecyclerView.Adapter<HighScoreAdapter.HighScoreViewHolder> {

    private List<HighScore> highScoreList;

    public HighScoreAdapter(List<HighScore> highScoreList) {
        this.highScoreList = highScoreList;
    }

    public void setHighScoreList(List<HighScore> highScoreList) {
        this.highScoreList = highScoreList;
    }

    @NonNull
    @Override
    public HighScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_high_score, parent, false);
        return new HighScoreViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HighScoreViewHolder holder, int position) {
        HighScore hs = highScoreList.get(position);
        holder.textHighScoreDetails.setText("Score: " + hs.score + "/" + hs.total);
        holder.textHighScoreDate.setText(hs.date);
    }

    @Override
    public int getItemCount() {
        return highScoreList.size();
    }

    public class HighScoreViewHolder extends RecyclerView.ViewHolder {
        TextView textHighScoreDetails, textHighScoreDate;

        public HighScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            textHighScoreDetails = itemView.findViewById(R.id.textHighScoreDetails);
            textHighScoreDate = itemView.findViewById(R.id.textHighScoreDate);
        }
    }
}

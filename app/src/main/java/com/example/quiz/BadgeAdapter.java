package com.example.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<Badge> badgeList;

    public BadgeAdapter(List<Badge> badgeList) {
        this.badgeList = badgeList;
    }

    public void setBadgeList(List<Badge> badgeList) {
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badgeList.get(position);
        holder.textBadgeName.setText(badge.name);
        holder.textBadgeDescription.setText(badge.description);
        holder.textBadgeDate.setText(badge.dateEarned);
    }

    @Override
    public int getItemCount() {
        return badgeList.size();
    }

    public class BadgeViewHolder extends RecyclerView.ViewHolder {
        TextView textBadgeName, textBadgeDescription, textBadgeDate;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            textBadgeName = itemView.findViewById(R.id.textBadgeName);
            textBadgeDescription = itemView.findViewById(R.id.textBadgeDescription);
            textBadgeDate = itemView.findViewById(R.id.textBadgeDate);
        }
    }
}

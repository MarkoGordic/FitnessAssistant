package com.example.fitnessassistant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.data.ActivityRecycler;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder>{
    private final Context context;
    private final List<ActivityRecycler> activities;

    public ActivityAdapter(Context context, List<ActivityRecycler> activityRecyclerList){
        this.context = context;
        this.activities = activityRecyclerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.activityDate.setText(String.valueOf(activities.get(position).getDate()));
        holder.activityTime.setText(activities.get(position).getDuration());
        holder.activityDistance.setText(String.valueOf(activities.get(position).getDistance()));
        holder.activityCalories.setText(String.valueOf(activities.get(position).getCaloriesBurnt()));
        holder.activityAverageSpeed.setText(String.valueOf(activities.get(position).getAverageSpeed()));
        holder.activityImage.setImageBitmap(activities.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView activityType, activityDate, activityDistance, activityCalories, activityAverageSpeed, activityTime;
        ImageView activityTypeImage, activityImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            activityType = itemView.findViewById(R.id.activityTypeText);
            activityDate = itemView.findViewById(R.id.activityDate);
            activityTime = itemView.findViewById(R.id.activityTime);
            activityDistance = itemView.findViewById(R.id.activityDistance);
            activityCalories = itemView.findViewById(R.id.activityCalories);
            activityAverageSpeed = itemView.findViewById(R.id.activityAverageSpeed);

            activityTypeImage = itemView.findViewById(R.id.activityTypeIcon);
            activityImage = itemView.findViewById(R.id.activityImage);
        }
    }
}

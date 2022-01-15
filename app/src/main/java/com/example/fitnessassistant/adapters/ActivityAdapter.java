package com.example.fitnessassistant.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.data.ActivityRecycler;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.util.TimeFunctional;

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

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int date = Integer.parseInt((String) DateFormat.format("yyyyMMdd", activities.get(position).getDate()));
        int day = date % 100;
        date /= 100;
        int month = date % 100;
        date /= 100;
        int year = date;

        holder.activityDate.setText(String.format("%d %s %d", day, TimeFunctional.getMonthShort(context, month), year));

        holder.activityTime.setText(activities.get(position).getDuration());
        holder.activityImage.setImageBitmap(activities.get(position).getImage());

        switch(activities.get(position).getActivityType()){
            case ActivityRecycler.ACTIVITY_RUNNING:
                Drawable run = AppCompatResources.getDrawable(context, R.drawable.run);
                if (run != null)
                    run.setTint(context.getColor(R.color.backgroundColor));
                holder.activityTypeImage.setImageDrawable(run);
                holder.activityType.setText(context.getString(R.string.running));
                break;
            case ActivityRecycler.ACTIVITY_CYCLING:
                Drawable cycle = AppCompatResources.getDrawable(context, R.drawable.bike);
                if (cycle != null)
                    cycle.setTint(context.getColor(R.color.backgroundColor));
                holder.activityTypeImage.setImageDrawable(cycle);
                holder.activityType.setText(context.getString(R.string.cycling));
                break;
            case ActivityRecycler.ACTIVITY_WALKING:
                Drawable walk = AppCompatResources.getDrawable(context, R.drawable.walk);
                if (walk != null)
                    walk.setTint(context.getColor(R.color.backgroundColor));
                holder.activityTypeImage.setImageDrawable(walk);
                holder.activityType.setText(context.getString(R.string.walking));
                break;
        }

        if(UnitPreferenceFragment.getDistanceUnit(context).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE)) {
            holder.activityDistance.setText(String.format("%.1f%s", activities.get(position).getDistance(), context.getString(R.string.mi)));
            holder.activityAverageSpeed.setText(String.format("%.1f%s",activities.get(position).getAverageSpeed(), context.getString(R.string.mi_h)));
        } else {
            holder.activityDistance.setText(String.format("%.1f%s", activities.get(position).getDistance(), context.getString(R.string.km)));
            holder.activityAverageSpeed.setText(String.format("%.1f%s",activities.get(position).getAverageSpeed(), context.getString(R.string.km_h)));
        }

        if(UnitPreferenceFragment.getEnergyUnit(context).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
            holder.activityCalories.setText(String.format("%d%s", activities.get(position).getCaloriesBurnt(), context.getString(R.string.kj)));
        else
            holder.activityCalories.setText(String.format("%d%s", activities.get(position).getCaloriesBurnt(), context.getString(R.string.cal)));
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

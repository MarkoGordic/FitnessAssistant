package com.example.fitnessassistant.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.data.ActivityRecycler;
import com.example.fitnessassistant.database.mdbh.MDBHActivityTracker;
import com.example.fitnessassistant.map.ActivityRecyclerFragment;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.util.TimeFunctional;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder>{
    public static final int MAIN_RECYCLER = 1;
    public static final int SUPP_RECYCLER = 2;
    private final Context context;
    public List<ActivityRecycler> activities;
    private final int activity;

    public ActivityAdapter(Context context, List<ActivityRecycler> activityRecyclerList, int activity){
        this.context = context;
        this.activities = activityRecyclerList;
        this.activity = activity;
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
            holder.activityDistance.setText(String.format("%.1f%s", activities.get(position).getDistance() * 0.621371f, context.getString(R.string.mi)));
            holder.activityAverageSpeed.setText(String.format("%.1f%s",activities.get(position).getAverageSpeed() * 0.621371f, context.getString(R.string.mi_h)));
        } else {
            holder.activityDistance.setText(String.format("%.1f%s", activities.get(position).getDistance(), context.getString(R.string.km)));
            holder.activityAverageSpeed.setText(String.format("%.1f%s",activities.get(position).getAverageSpeed(), context.getString(R.string.km_h)));
        }

        if(UnitPreferenceFragment.getEnergyUnit(context).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
            holder.activityCalories.setText(String.format("%.1f%s", activities.get(position).getCaloriesBurnt() * 4.184f, context.getString(R.string.kj)));
        else
            holder.activityCalories.setText(String.format("%.1f%s", activities.get(position).getCaloriesBurnt(), context.getString(R.string.cal)));

        if(activity == MAIN_RECYCLER) {
            holder.trashButton.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(R.layout.custom_two_button_alert_dialog);
                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Drawable trash = AppCompatResources.getDrawable(context, R.drawable.trash);
                if (trash != null)
                    trash.setTint(context.getColor(R.color.SpaceCadet));
                ((AppCompatImageView) dialog.findViewById(R.id.dialog_drawable)).setImageDrawable(trash);

                ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.delete_activity);
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.delete_activity_message);
                dialog.findViewById(R.id.dialog_input).setVisibility(View.GONE);

                dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());

                ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.delete);
                dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                    MDBHActivityTracker.getInstance(context).removeActivityFromDB(activities.get(position).getId()); // TODO: Na ovoj liniji se kres dok sam brisao activity
                    remove(position);
                    dialog.dismiss();
                });
            });
        } else
            holder.trashButton.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if(activity == MAIN_RECYCLER)
            return activities.size();
        else if(activity == SUPP_RECYCLER)
            return Math.min(activities.size(), 3);
        else
            return -1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView activityType, activityDate, activityDistance, activityCalories, activityAverageSpeed, activityTime;
        ImageView activityTypeImage, activityImage;
        AppCompatImageButton trashButton;

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

            trashButton = itemView.findViewById(R.id.trashActivity);
        }
    }

    public void remove(int position){
        if(position >= 0) {
            Handler handler = new Handler(context.getMainLooper());
            handler.post(() -> {
                activities.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());

                ActivityRecyclerFragment.updateLists(activities);

                if(activities.size() == 0)
                    ((InAppActivity) context).updateActivityRecyclerUI(true);
            });
        }
    }

    public void add(ActivityRecycler activityRecycler, int position){
        if(position >= 0) {
            Handler handler = new Handler(context.getMainLooper());
            handler.post(() -> {
                InAppActivity.activities.add(position, activityRecycler);
                ((InAppActivity) context).smallActivityAdapter.notifyItemInserted(position);
                ((InAppActivity) context).updateActivityRecyclerUI(false);
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<ActivityRecycler> list){
        Handler handler = new Handler(context.getMainLooper());
        handler.post(() -> {
            activities = list;
            notifyDataSetChanged();
        });
    }
}

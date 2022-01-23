package com.example.fitnessassistant.adapters;

import static com.example.fitnessassistant.sleeptracker.SleepDateFragment.QUALITY_AWFUL;
import static com.example.fitnessassistant.sleeptracker.SleepDateFragment.QUALITY_BAD;
import static com.example.fitnessassistant.sleeptracker.SleepDateFragment.QUALITY_EXCELLENT;
import static com.example.fitnessassistant.sleeptracker.SleepDateFragment.QUALITY_GOOD;
import static com.example.fitnessassistant.sleeptracker.SleepDateFragment.QUALITY_NEUTRAL;
import static com.example.fitnessassistant.sleeptracker.SleepDateFragment.QUALITY_NONE;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.sleeptracker.SleepFragment;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final ArrayList<String> daysOfMonth;
    private final ArrayList<Integer> qualitiesOfMonth;
    private final OnItemListener listener;
    private final int backgroundColor;
    private final int invertedBackgroundColor;
    private final int awful;
    private final int bad;
    private final int neutral;
    private final int good;
    private final int excellent;
    private final int gray;
    private Drawable circle;

    public CalendarAdapter(Context context, ArrayList<String> daysOfMonth, ArrayList<Integer> qualitiesOfMonth, OnItemListener listener){
        this.backgroundColor = ContextCompat.getColor(context, R.color.backgroundColor);
        this.invertedBackgroundColor = ContextCompat.getColor(context, R.color.InvertedBackgroundColor);
        this.awful = ContextCompat.getColor(context, R.color.Awful);
        this.bad = ContextCompat.getColor(context, R.color.Bad);
        this.neutral = ContextCompat.getColor(context, R.color.Neutral);
        this.good = ContextCompat.getColor(context, R.color.Good);
        this.excellent = ContextCompat.getColor(context, R.color.Excellent);
        this.gray = ContextCompat.getColor(context, R.color.LightGrayColor);
        this.circle = ContextCompat.getDrawable(context, R.drawable.calendar_circle);
        this.daysOfMonth = daysOfMonth;
        this.qualitiesOfMonth = qualitiesOfMonth;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        view.post(() -> {
            int side = view.getMeasuredWidth();

            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = side;
            lp.height = side;
            view.setLayoutParams(lp);
        });
        return new CalendarViewHolder(listener, view);
    }

    public void setAddedQuality(int position, int quality){
        qualitiesOfMonth.set(position, quality);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.dayOfMonth.setText(daysOfMonth.get(position));
        if(qualitiesOfMonth.get(position) != null) {
            circle = circle.getConstantState().newDrawable();
            switch(qualitiesOfMonth.get(position)){
                case QUALITY_AWFUL:
                    circle.setTint(awful);
                    break;
                case QUALITY_BAD:
                    circle.setTint(bad);
                    break;
                case QUALITY_NEUTRAL:
                    circle.setTint(neutral);
                    break;
                case QUALITY_GOOD:
                    circle.setTint(good);
                    break;
                case QUALITY_EXCELLENT:
                    circle.setTint(excellent);
                    break;
                default:
                    circle.setTint(gray);
            }
            holder.dayOfMonth.setBackground(circle);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position, @NonNull List<Object> payloads) {
        if(!payloads.isEmpty() && payloads.get(0) instanceof Integer) {
            if((Integer) payloads.get(0) > 0) {
                holder.dayOfMonth.setBackgroundResource(R.color.BlueYonder);
                holder.dayOfMonth.setTextColor(backgroundColor);
                SleepFragment.previousPositionSelected = holder.getAdapterPosition();
            } else if((Integer) payloads.get(0) < 0){
                if(qualitiesOfMonth.get(position) != null) {
                    circle = circle.getConstantState().newDrawable();
                    switch(qualitiesOfMonth.get(position)){
                        case QUALITY_AWFUL:
                            circle.setTint(awful);
                            break;
                        case QUALITY_BAD:
                            circle.setTint(bad);
                            break;
                        case QUALITY_NEUTRAL:
                            circle.setTint(neutral);
                            break;
                        case QUALITY_GOOD:
                            circle.setTint(good);
                            break;
                        case QUALITY_EXCELLENT:
                            circle.setTint(excellent);
                            break;
                        default:
                            circle.setTint(gray);
                    }
                    holder.dayOfMonth.setBackground(circle);
                } else
                    holder.dayOfMonth.setBackgroundResource(R.color.backgroundColor);

                holder.dayOfMonth.setTextColor(invertedBackgroundColor);
            } else { // DEFAULT
                holder.dayOfMonth.setBackgroundResource(R.color.backgroundColor);
                holder.dayOfMonth.setTextColor(invertedBackgroundColor);
            }
        } else
            super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final TextView dayOfMonth;
        private final CalendarAdapter.OnItemListener onItemListener;

        public CalendarViewHolder(OnItemListener onItemListener, @NonNull View itemView){
            super(itemView);
            dayOfMonth = itemView.findViewById(R.id.cellDayText);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
        }
    }
}

package com.example.fitnessassistant.database.data;

import android.content.Context;

import com.example.fitnessassistant.pedometer.StepGoalFragment;
import com.example.fitnessassistant.questions.WeightFragment;

import java.util.ArrayList;
import java.util.List;

public class GoalsData {
    private float firstWeight;
    private float targetWeight;
    private int firstDate;

    private List<Integer> weeklySteps = new ArrayList<>();

    public GoalsData(){}

    public void setData(Context context){
        firstWeight = WeightFragment.getFirstWeight(context);
        targetWeight = WeightFragment.getGoalWeight(context);

        firstDate = Integer.parseInt(WeightFragment.getFirstWeightDate(context));

        weeklySteps.add(StepGoalFragment.getMondayStepGoal(context));
        weeklySteps.add(StepGoalFragment.getTuesdayStepGoal(context));
        weeklySteps.add(StepGoalFragment.getWednesdayStepGoal(context));
        weeklySteps.add(StepGoalFragment.getThursdayStepGoal(context));
        weeklySteps.add(StepGoalFragment.getFridayStepGoal(context));
        weeklySteps.add(StepGoalFragment.getSaturdayStepGoal(context));
        weeklySteps.add(StepGoalFragment.getSundayStepGoal(context));
    }

    public void setFirstDate(int firstDate) {
        this.firstDate = firstDate;
    }

    public void setFirstWeight(float firstWeight) {
        this.firstWeight = firstWeight;
    }

    public void setTargetWeight(float targetWeight) {
        this.targetWeight = targetWeight;
    }

    public void setWeeklySteps(List<Integer> weeklySteps) {
        this.weeklySteps = weeklySteps;
    }

    public int getFirstDate() {
        return firstDate;
    }

    public float getFirstWeight() {
        return firstWeight;
    }

    public float getTargetWeight() {
        return targetWeight;
    }

    public List<Integer> getWeeklySteps() {
        return weeklySteps;
    }
}

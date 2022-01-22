package com.example.fitnessassistant.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.fitnessassistant.R;

public class GraphView extends View {

    private int[] graphValues;
    private int graphMaxY;
    private float bottomScaleCount;
    private float sideScaleCount;
    private float maxCount;
    private Path path;
    private Paint pathPaint;
    private Paint backgroundPaint;
    private int[] arr1;
    private int[] arr2;


    private void init(Context context){
        pathPaint = new Paint();
        pathPaint.setStrokeWidth(4f);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setColor(ContextCompat.getColor(context, R.color.SpaceCadet));
        pathPaint.setPathEffect(new CornerPathEffect(10f));
        pathPaint.setDither(true);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setStrokeWidth(1.5f);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.NonBackgroundColor));

        resetPath();
    }

    public GraphView(Context context) {
        super(context);
        init(context);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setArrayValues(int[] array1, int[] array2){
        arr1 = array1;
        arr2 = array2;
    }

    private int calculateGraphMax(int maxY){
        for(int i : arr1)
            for(int j : arr2)     // those too are skipped because they are not divisible by 4 (and they are too small for that precision)
                if(maxY < i * j && i * j % 4 == 0)
                    return i * j;

        return arr1[arr1.length - 1] * arr2[arr2.length - 1];
    }

    public void setGraphValues(int[] yValues,  int maxY) {
        graphValues = yValues;
        graphMaxY = calculateGraphMax(maxY);
    }

    public void setGraphValues(int[] yValues) {
        int maxY = 0;
        for (int y : yValues) {
            if (y > maxY)
                maxY = y;
        }
        setGraphValues(yValues, maxY);
    }

    public void setBottomScaleCount(float count){
        this.bottomScaleCount = count;
    }

    public void setSideScaleCount(float count){
        this.sideScaleCount = count;
    }

    private void drawBackground(Canvas canvas, float factorX, float factorY){
        for(float i : getYValuesForGraph())
            canvas.drawLine(0, getHeight() - (i * factorY), getWidth() * factorX, getHeight() - (i * factorY), backgroundPaint);
    }

    public void resetPath(){
        path = new Path();
    }

    public void setMaxCount(float maxCount){
        this.maxCount = maxCount;
    }

    private void drawGraphPath(Canvas canvas){
        resetPath();
        drawBackground(canvas, getWidth() / (float) maxCount, getHeight() / (float)graphMaxY);

        if(graphValues.length > 1) {
            float stepX = getWidth() / bottomScaleCount / 2;
            float stepY = getHeight() / sideScaleCount / 2;

            float drawingWidth = getWidth() - stepX * 2;
            float drawingHeight = getHeight() - stepY * 2;
            float factorX = drawingWidth / (float) (maxCount - 1);
            float factorY = drawingHeight / (float) graphMaxY;

            path.moveTo(stepX, getHeight() - (graphValues[0] * factorY + stepY));

            for (int i = 1; i < graphValues.length; i++)
                path.lineTo(i * factorX + stepX, getHeight() - (graphValues[i] * factorY + stepY));

            canvas.drawPath(path, pathPaint);
        }
    }

    private float[] getYValuesForGraph(){
        float step = (float) graphMaxY / 10;
        float val = (float) graphMaxY / 5;
        return new float[]{ step, val + step, 2 * val + step, 3 * val + step, 4 * val + step};
    }

    public float[] getYValues(){
        return new float[]{ 0, (float) graphMaxY / 4, (float) graphMaxY / 2, 3 * (float) graphMaxY / 4, graphMaxY };
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(graphValues == null)
            return;

        drawGraphPath(canvas);
    }
}
package com.example.fitnessassistant.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class PieView extends View {
    private Paint p;
    private Paint cenPaint;
    private RectF rectF;

    int[] data;
    int partNum;
    private int[] colors;
    private int bgColor;

    private void init(int num, int[] data, int[] colors, int bgColor){
        this.p = new Paint();
        this.cenPaint = new Paint();
        this.rectF = new RectF();

        setFocusable(true);

        this.partNum =num;
        this.data=data;
        this.colors =colors;
        this.bgColor = bgColor;
    }

    public PieView(Context context) {
        super(context);
        init(0, new int[0], new int[0], 0);
    }

    public PieView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(0, new int[0], new int[0], 0);
    }

    public PieView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(0, new int[0], new int[0], 0);
    }

    public void setData(int num, int[] data, int[] colors, int bgColor){
        init(num, data, colors, bgColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);

        float[] scaledValues = scale();

        float totalMarginX = getWidth() / 3.5f;
        float totalMarginY = getHeight() / 3.5f;

        float left = totalMarginX / 2f;
        float right = getWidth() - (totalMarginX / 2f);
        float top = totalMarginY / 2f;
        float bottom = getHeight() - (totalMarginY / 2f);

        rectF.set(left, top, right, bottom);

        float start = 0;
        for(int i = 0; i < partNum; i++){
            p.setColor(colors[i]);

            canvas.drawArc(rectF, start, scaledValues[i],true, p);
            start += scaledValues[i];
        }

        cenPaint.setAntiAlias(true);
        cenPaint.setStyle(Paint.Style.FILL);
        cenPaint.setColor(bgColor);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float pieWidth = right - left;
        float pieHeight = bottom - top;

        float fullCircleRadius = Math.min(pieWidth / 2f, pieHeight / 2f);

        canvas.drawCircle(cx, cy, fullCircleRadius * 0.875f, cenPaint);
    }

    private float[] scale() {
        float[] scaledValues = new float[this.data.length];

        float total = 0;
        for (float val : this.data)
            total += val;

        for (int i = 0; i < this.data.length; i++)
            scaledValues[i] = (this.data[i] / total) * 360;

        return scaledValues;
    }
}

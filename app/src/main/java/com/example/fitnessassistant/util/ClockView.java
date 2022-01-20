package com.example.fitnessassistant.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.fitnessassistant.R;

public class ClockView extends View {

    private int backgroundColor;
    private int invertedBackgroundColor;
    private int blueYonder;

    private Paint paint;
    private RectF rectF;
    private final Rect rect = new Rect();

    private float startAngle;
    private float sweepAngle;
    private float fontSize;
    private float innerRadius;
    private float numPaddingLevel;

    private final int[] numbers = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };

    private void init(Context context){
        paint = new Paint();
        rectF = new RectF();

        backgroundColor = context.getColor(R.color.backgroundColor);
        invertedBackgroundColor = context.getColor(R.color.InvertedBackgroundColor);
        blueYonder = context.getColor(R.color.BlueYonder);
    }

    public ClockView(Context context) {
        super(context);
        init(context);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setStartHours(float startHours){
        startAngle = startHours * 30 - 90;
    }

    public void setHoursSlept(float hoursSlept){
        sweepAngle = hoursSlept * 30;
    }

    private void drawNumeral(Canvas canvas){
        paint.setTextSize(fontSize);
        paint.setFakeBoldText(true);
        paint.setColor(invertedBackgroundColor);

        for(int i : numbers){
            String tmp = String.valueOf(i);
            paint.getTextBounds(tmp, 0, tmp.length(), rect);
            double angle = Math.PI / 6  * (i - 3);
            double x = getWidth() / 2f + Math.cos(angle) * (innerRadius - (innerRadius / numPaddingLevel)) - rect.width() / 2f;
            double y = getHeight() / 2f + Math.sin(angle) * (innerRadius - (innerRadius / numPaddingLevel)) + rect.height() / 2f;
            canvas.drawText(tmp, (float) x,(float) y, paint);
        }
    }

    private void drawCenter(Canvas canvas){
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, 12, paint);
    }

    public void setFontSize(float fontSizeInSP){
        fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSizeInSP, getResources().getDisplayMetrics());
    }

    public void setNumPadding(float numPaddingLevel){
        this.numPaddingLevel = numPaddingLevel;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float centerX = getWidth() / 2f;
        final float centerY = getHeight() / 2f;

        float radius = Math.min(centerX, centerY);

        paint.setColor(invertedBackgroundColor);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radius, paint);

        drawCenter(canvas);

        innerRadius = radius - (radius / 60f);

        paint.setColor(backgroundColor);
        canvas.drawCircle(centerX, centerY, innerRadius, paint);

        rectF.set(centerX - innerRadius, centerY - innerRadius, centerX + innerRadius, centerY + innerRadius);

        paint.setColor(blueYonder);
        canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

        drawNumeral(canvas);
    }
}

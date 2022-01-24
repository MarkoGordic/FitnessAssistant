package com.example.fitnessassistant.util;

import static com.example.fitnessassistant.sleeptracker.SleepDateFragment.VALUE_BLOCK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.core.content.res.ResourcesCompat;

import com.example.fitnessassistant.R;

public class RangeSeekBar extends androidx.appcompat.widget.AppCompatImageView {

    public interface OnRangeSeekBarChangeListener {
        void onRangeSeekBarValuesChanged(RangeSeekBar bar, int minValue, int maxValue);
    }

    private enum Thumb { MIN, MAX }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mRect;

    private final Bitmap thumbImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal);
    private final Bitmap thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed);
    private final Bitmap thumbDisabledImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_disabled);

    private final int grayColor = ResourcesCompat.getColor(getResources(), R.color.LightGrayColor, null);
    private final int spaceCadet = ResourcesCompat.getColor(getResources(), R.color.LightGrayColor, null);

    private final float thumbWidth = thumbImage.getWidth();
    private final float thumbHalfWidth = 0.5f * thumbWidth;
    private final float thumbHalfHeight = 0.5f * thumbImage.getHeight();

    private int textOffset;
    private float sideDistance;
    private float padding;

    private int absoluteMinValue;
    private int absoluteMaxValue;

    private double normalizedMinValue = 0d;
    private double normalizedMaxValue = 1d;
    private Thumb pressedThumb = null;
    private boolean notifyWhileDragging = false;
    private OnRangeSeekBarChangeListener listener;

    public static final int INVALID_POINTER_ID = 255;
    public static final int POINTER_INDEX_MASK = 0x0000ff00, POINTER_INDEX_SHIFT = 8;
    private int pointerId = INVALID_POINTER_ID;

    private float downX;
    private int scaledTouchSlop;
    private boolean isDragging;

    public RangeSeekBar(Context context) {
        super(context);
        init(context);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RangeSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private void init(Context context) {
        // calculating distance (padding)
        sideDistance = dpToPx(context, 8);
        int topDistance = dpToPx(context, 8);

        textOffset = dpToPx(context, 8) + topDistance;

        float lineHeight = dpToPx(context, 1);
        mRect = new RectF(padding,
                textOffset + thumbHalfHeight - lineHeight / 2,
                getWidth() - padding,
                textOffset + thumbHalfHeight + lineHeight / 2);

        // make RangeSeekBar focusable
        setFocusable(true);
        setFocusableInTouchMode(true);
        scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void setRangeValues(int minValue, int maxValue) {
        absoluteMinValue = minValue;
        absoluteMaxValue = maxValue;
    }

    public void setNotifyWhileDragging(boolean notify) {
        notifyWhileDragging = notify;
    }

    public int getSelectedMinValue() {
        return normalizedToValue(normalizedMinValue);
    }

    public void setSelectedMinValue(int value) {
        if(absoluteMinValue == absoluteMaxValue)
            setNormalizedMaxValue(0);
        else
            setNormalizedMinValue(valueToNormalized(value));
    }

    public int getSelectedMaxValue() {
        return normalizedToValue(normalizedMaxValue);
    }

    public void setSelectedMaxValue(int value) {
        if(absoluteMinValue == absoluteMaxValue)
            setNormalizedMaxValue(1);
        else
            setNormalizedMaxValue(valueToNormalized(value));
    }

    public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener listener) {
        this.listener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled())
            return false;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // first finger touches
                pointerId = event.getPointerId(event.getPointerCount() - 1);
                downX = event.getX(event.findPointerIndex(pointerId));

                pressedThumb = getPressedThumb(downX);

                if (pressedThumb == null)
                    return super.onTouchEvent(event); // don't handle if it's not a thumb press

                setPressed(true);
                invalidate();

                isDragging = true;
                trackTouchEvent(event);

                if (getParent() != null)
                    getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE: // when finger is on screen and moving
                if (pressedThumb != null) {
                    if (isDragging)
                        trackTouchEvent(event);
                    else {
                        // scroll and follow event
                        if (Math.abs(event.getX(event.findPointerIndex(pointerId)) - downX) > scaledTouchSlop) {
                            setPressed(true);
                            invalidate();

                            isDragging = true;
                            trackTouchEvent(event);

                            if (getParent() != null)
                                getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }

                    if (notifyWhileDragging && listener != null)
                        listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                }
                break;
            case MotionEvent.ACTION_UP: // last finger off screen
                if (isDragging) {
                    trackTouchEvent(event);

                    isDragging = false;
                    setPressed(false);
                } else {
                    // tap-seek to the location touched.
                    isDragging = true;
                    trackTouchEvent(event);
                    isDragging = false;
                }

                pressedThumb = null;
                invalidate();

                if (listener != null)
                    listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // second or further finger on screen
                downX = event.getX(event.getPointerCount() - 1);
                pointerId = event.getPointerId(event.getPointerCount() - 1);
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP: // some finger leaves the screen, at least one is left
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL: // entire gesture was aborted
                if (isDragging) {
                    isDragging = false;
                    setPressed(false);
                }
                invalidate();
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & POINTER_INDEX_MASK) >> POINTER_INDEX_SHIFT;
        int eventPointerId = ev.getPointerId(pointerIndex);

        if (eventPointerId == pointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            downX = ev.getX(newPointerIndex);
            pointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        float x = event.getX(event.findPointerIndex(pointerId));

        double normX = screenToNormalized(x);

        if (pressedThumb.equals(Thumb.MIN))
            setNormalizedMinValue(normX);
        else if (pressedThumb.equals(Thumb.MAX) && normX * (absoluteMaxValue - absoluteMinValue) >= VALUE_BLOCK)
            setNormalizedMaxValue(normX);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED)
            width = MeasureSpec.getSize(widthMeasureSpec);

        int height = thumbImage.getHeight() + dpToPx(getContext(), 32);
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED)
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));

        setMeasuredDimension(width, height);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // set paint
        paint.setStyle(Style.FILL);
        paint.setColor(grayColor);
        paint.setAntiAlias(true);

        // calculate padding
        padding = sideDistance + thumbHalfWidth;

        // draw seek bar background line
        mRect.left = padding;
        mRect.right = getWidth() - padding;
        canvas.drawRect(mRect, paint);

        boolean selectedValuesAreDefault = getSelectedMinValue() == absoluteMinValue &&
                getSelectedMaxValue() == absoluteMaxValue;

        // draw seek bar active range line
        mRect.left = normalizedToScreen(normalizedMinValue);
        mRect.right = normalizedToScreen(normalizedMaxValue);

        paint.setColor(selectedValuesAreDefault ? grayColor : spaceCadet);
        canvas.drawRect(mRect, paint);

        drawThumb(normalizedToScreen(normalizedMinValue), Thumb.MIN.equals(pressedThumb), canvas, selectedValuesAreDefault);
        drawThumb(normalizedToScreen(normalizedMaxValue), Thumb.MAX.equals(pressedThumb), canvas, selectedValuesAreDefault);
    }

    private void drawThumb(float screenCoord, boolean pressed, Canvas canvas, boolean areSelectedValuesDefault) {
        Bitmap buttonToDraw;
        if (areSelectedValuesDefault)
            buttonToDraw = thumbDisabledImage;
        else
            buttonToDraw = pressed ? thumbPressedImage : thumbImage;

        canvas.drawBitmap(buttonToDraw, screenCoord - thumbHalfWidth, textOffset, paint);
    }

    private Thumb getPressedThumb(float touchX) {
        Thumb result = null;

        boolean minThumbPressed = isThumbPressed(touchX, normalizedMinValue);
        boolean maxThumbPressed = isThumbPressed(touchX, normalizedMaxValue);

        if (minThumbPressed && maxThumbPressed)
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX; // choose the one with more room
        else if (minThumbPressed)
            result = Thumb.MIN;
        else if (maxThumbPressed)
            result = Thumb.MAX;
        return result;
    }

    private boolean isThumbPressed(float touchX, double normalizedThumbValue) {
        // if the press is in thumb range
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth;
    }

    private void setNormalizedMinValue(double value) {
        normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValue)));
        invalidate();
    }

    private void setNormalizedMaxValue(double value) {
        normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValue)));
        invalidate();
    }

    private int normalizedToValue(double normalized) {
        double v = absoluteMinValue + normalized * (absoluteMaxValue - absoluteMinValue);
        return (int) (Math.round(v * 100) / 100d);
    }

    private double valueToNormalized(int value) {
        if(absoluteMinValue == absoluteMaxValue)
            return 0;
        return ((double)(value - absoluteMinValue)) / (absoluteMaxValue - absoluteMinValue);
    }

    private float normalizedToScreen(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }

    private double screenToNormalized(float screenCoord) {
        if (getWidth() <= 2 * padding)
            return 0;
        return Math.min(1d, Math.max(0d, (screenCoord - padding) / (getWidth() - 2 * padding)));
    }
}
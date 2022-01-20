package com.example.fitnessassistant.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.example.fitnessassistant.R;

public class RangeSeekBar extends androidx.appcompat.widget.AppCompatImageView {

    public static final int HEIGHT_IN_DP = 60;
    public static final int TEXT_LATERAL_PADDING_IN_DP = 3;
    private static final int INITIAL_PADDING_IN_DP = 8;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Bitmap thumbImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal);
    private final Bitmap thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed);
    private final Bitmap thumbDisabledImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_disabled);
    private final float thumbWidth = thumbImage.getWidth();
    private final float thumbHalfWidth = 0.5f * thumbWidth;
    private final float thumbHalfHeight = 0.5f * thumbImage.getHeight();
    private float INITIAL_PADDING;
    private float padding;
    private int absoluteMinValue;
    private int absoluteMaxValue;
    private double absoluteMinValuePrim, absoluteMaxValuePrim;
    private double normalizedMinValue = 0d;
    private double normalizedMaxValue = 1d;
    private Thumb pressedThumb = null;
    private boolean notifyWhileDragging = false;
    private OnRangeSeekBarChangeListener listener;
    /**
     * Default color of a {@link RangeSeekBar}, #FF33B5E5. This is also known as "Ice Cream Sandwich" blue.
     */
    public static final int DEFAULT_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5);
    /**
     * An invalid pointer id.
     */
    public static final int INVALID_POINTER_ID = 255;

    // Localized constants from MotionEvent for compatibility
    // with API < 8 "Froyo".
    public static final int ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;

    private float mDownMotionX;

    private int mActivePointerId = INVALID_POINTER_ID;

    private int mScaledTouchSlop;

    private boolean mIsDragging;

    private int mTextOffset;
    private int mDistanceToTop;
    private RectF mRect;

    private static final int DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP = 8;
    private static final int DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP = 8;

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

    private void init(Context context) {
        setRangeValues(0, 100);

        INITIAL_PADDING = PixelUtil.dpToPx(context, INITIAL_PADDING_IN_DP);

        mDistanceToTop = PixelUtil.dpToPx(context, DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP);
        mTextOffset = PixelUtil.dpToPx(context,
                DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP) + this.mDistanceToTop;

        int LINE_HEIGHT_IN_DP = 1;
        float lineHeight = PixelUtil.dpToPx(context, LINE_HEIGHT_IN_DP);
        mRect = new RectF(padding,
                mTextOffset + thumbHalfHeight - lineHeight / 2,
                getWidth() - padding,
                mTextOffset + thumbHalfHeight + lineHeight / 2);

        // make RangeSeekBar focusable. This solves focus handling issues in case EditText widgets are being used along with the RangeSeekBar within ScollViews.
        setFocusable(true);
        setFocusableInTouchMode(true);
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void setRangeValues(int minValue, int maxValue) {
        this.absoluteMinValue = minValue;
        this.absoluteMaxValue = maxValue;
        absoluteMinValuePrim = (double) absoluteMinValue;
        absoluteMaxValuePrim = (double) absoluteMaxValue;
    }

    public boolean isNotifyWhileDragging() {
        return notifyWhileDragging;
    }

    /**
     * Should the widget notify the listener callback while the user is still dragging a thumb? Default is false.
     */
    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }

    /**
     * Returns the absolute minimum value of the range that has been set at construction time.
     *
     * @return The absolute minimum value of the range.
     */
    public int getAbsoluteMinValue() {
        return absoluteMinValue;
    }

    /**
     * Returns the absolute maximum value of the range that has been set at construction time.
     *
     * @return The absolute maximum value of the range.
     */
    public int getAbsoluteMaxValue() {
        return absoluteMaxValue;
    }

    /**
     * Returns the currently selected min value.
     *
     * @return The currently selected min value.
     */
    public int getSelectedMinValue() {
        return normalizedToValue(normalizedMinValue);
    }

    /**
     * Sets the currently selected minimum value. The widget will be invalidated and redrawn.
     *
     * @param value The Number value to set the minimum value to. Will be clamped to given absolute minimum/maximum range.
     */
    public void setSelectedMinValue(int value) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
        if (absoluteMaxValuePrim == absoluteMinValuePrim) {
            setNormalizedMinValue(0d);
        } else {
            setNormalizedMinValue(valueToNormalized(value));
        }
    }

    /**
     * Returns the currently selected max value.
     *
     * @return The currently selected max value.
     */
    public int getSelectedMaxValue() {
        return normalizedToValue(normalizedMaxValue);
    }

    /**
     * Sets the currently selected maximum value. The widget will be invalidated and redrawn.
     *
     * @param value The Number value to set the maximum value to. Will be clamped to given absolute minimum/maximum range.
     */
    public void setSelectedMaxValue(int value) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
        if (absoluteMaxValuePrim == absoluteMinValuePrim) {
            setNormalizedMaxValue(1d);
        } else {
            setNormalizedMaxValue(valueToNormalized(value));
        }
    }

    public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Handles thumb selection and movement. Notifies listener callback on certain events.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled()) {
            return false;
        }

        int pointerIndex;

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);

                pressedThumb = evalPressedThumb(mDownMotionX);

                // Only handle thumb presses.
                if (pressedThumb == null) {
                    return super.onTouchEvent(event);
                }

                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();

                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedThumb != null) {

                    if (mIsDragging) {
                        trackTouchEvent(event);
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);

                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }

                    if (notifyWhileDragging && listener != null) {
                        listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                pressedThumb = null;
                invalidate();
                if (listener != null) {
                    listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = event.getPointerCount() - 1;
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);

        if (Thumb.MIN.equals(pressedThumb)) {
            setNormalizedMinValue(screenToNormalized(x));
        } else if (Thumb.MAX.equals(pressedThumb)) {
            setNormalizedMaxValue(screenToNormalized(x));
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch() { mIsDragging = true; }

    /**
     * This is called when the user either releases his touch or the touch is canceled.
     */
    void onStopTrackingTouch() { mIsDragging = false; }

    /**
     * Ensures correct size of the widget.
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }

        int height = thumbImage.getHeight() + PixelUtil.dpToPx(getContext(), HEIGHT_IN_DP);
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    /**
     * Draws the widget on the given canvas.
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Style.FILL);
        paint.setColor(Color.GRAY);
        paint.setAntiAlias(true);

        // draw min and max labels
        float minMaxLabelSize = Math.max(paint.measureText("Min"), paint.measureText("Max"));
        padding = INITIAL_PADDING + minMaxLabelSize + thumbHalfWidth;

        // draw seek bar background line
        mRect.left = padding;
        mRect.right = getWidth() - padding;
        canvas.drawRect(mRect, paint);

        boolean selectedValuesAreDefault = (getSelectedMinValue() == getAbsoluteMinValue()) &&
                (getSelectedMaxValue() == getAbsoluteMaxValue());

        int colorToUseForButtonsAndHighlightedLine = selectedValuesAreDefault ? Color.GRAY : DEFAULT_COLOR;

        // draw seek bar active range line
        mRect.left = normalizedToScreen(normalizedMinValue);
        mRect.right = normalizedToScreen(normalizedMaxValue);

        paint.setColor(colorToUseForButtonsAndHighlightedLine);
        canvas.drawRect(mRect, paint);

        drawThumb(normalizedToScreen(normalizedMinValue), Thumb.MIN.equals(pressedThumb), canvas, selectedValuesAreDefault);
        drawThumb(normalizedToScreen(normalizedMaxValue), Thumb.MAX.equals(pressedThumb), canvas, selectedValuesAreDefault);

        // draw the text if sliders have moved from default edges
        if (!selectedValuesAreDefault) {
            paint.setColor(Color.WHITE);
            // give text a bit more space here so it doesn't get cut off
            int offset = PixelUtil.dpToPx(getContext(), TEXT_LATERAL_PADDING_IN_DP);

            String minText = String.valueOf(getSelectedMinValue());
            String maxText = String.valueOf(getSelectedMaxValue());
            float minTextWidth = paint.measureText(minText) + offset;
            float maxTextWidth = paint.measureText(maxText) + offset;

            canvas.drawText(minText,
                    normalizedToScreen(normalizedMinValue) - minTextWidth * 0.5f, mDistanceToTop, paint);

            canvas.drawText(maxText,
                    normalizedToScreen(normalizedMaxValue) - maxTextWidth * 0.5f, mDistanceToTop, paint);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", super.onSaveInstanceState());
        bundle.putDouble("MIN", normalizedMinValue);
        bundle.putDouble("MAX", normalizedMaxValue);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        final Bundle bundle = (Bundle) parcel;
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
        normalizedMinValue = bundle.getDouble("MIN");
        normalizedMaxValue = bundle.getDouble("MAX");
    }

    /**
     * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
     *
     * @param screenCoord The x-coordinate in screen space where to draw the image.
     * @param pressed     Is the thumb currently in "pressed" state?
     * @param canvas      The canvas to draw upon.
     */
    private void drawThumb(float screenCoord, boolean pressed, Canvas canvas, boolean areSelectedValuesDefault) {
        Bitmap buttonToDraw;
        if (areSelectedValuesDefault) {
            buttonToDraw = thumbDisabledImage;
        } else {
            buttonToDraw = pressed ? thumbPressedImage : thumbImage;
        }

        canvas.drawBitmap(buttonToDraw, screenCoord - thumbHalfWidth, mTextOffset, paint);
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private Thumb evalPressedThumb(float touchX) {
        Thumb result = null;
        boolean minThumbPressed = isInThumbRange(touchX, normalizedMinValue);
        boolean maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue);
        if (minThumbPressed && maxThumbPressed) {
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a corner, not being able to drag them apart anymore.
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
        } else if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        }
        return result;
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
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
        double v = absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim);
        // TODO parameterize this rounding to allow variable decimal points
        return (int) (Math.round(v * 100) / 100d);
    }

    private double valueToNormalized(int value) {
        if (absoluteMaxValuePrim == absoluteMinValuePrim)
            return 0;
        return (value - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim);
    }

    private float normalizedToScreen(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }

    private double screenToNormalized(float screenCoord) {
        if (getWidth() <= 2 * padding)
            return 0;
        return Math.min(1d, Math.max(0d, (screenCoord - padding) / (getWidth() - 2 * padding)));
    }

    public interface OnRangeSeekBarChangeListener {
        void onRangeSeekBarValuesChanged(RangeSeekBar bar, int minValue, int maxValue);
    }

    private enum Thumb { MIN, MAX }
}
/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.filepicker.view;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Checkable;

import com.qiscus.sdk.R;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class SmoothCheckBox extends View implements Checkable {
    private static final String KEY_INSTANCE_STATE = "InstanceState";
    private static final int COLOR_TICK = Color.WHITE;
    private static final int COLOR_UNCHECKED = Color.WHITE;
    private static final int COLOR_CHECKED = Color.parseColor("#FB4846");
    private static final int COLOR_FLOOR_UNCHECKED = Color.parseColor("#DFDFDF");
    private static final int DEF_DRAW_SIZE = 25;
    private static final int DEF_ANIM_DURATION = 300;

    private Paint paint, tickPaint, floorPaint;
    private Point[] tickPoints;
    private Point centerPoint;
    private Path tickPath;
    private float leftLineDistance, rightLineDistance, drewDistance;
    private float scaleVal = 1.0f, floorScale = 1.0f;
    private int width, animDuration, strokeWidth;
    private int checkedColor, unCheckedColor, floorColor, floorUnCheckedColor;
    private boolean checked;
    private boolean tickDrawing;
    private OnCheckedChangeListener listener;

    public SmoothCheckBox(Context context) {
        this(context, null);
    }

    public SmoothCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SmoothCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.QiscusSmoothCheckBox);
        int tickColor = ta.getColor(R.styleable.QiscusSmoothCheckBox_color_tick, COLOR_TICK);
        animDuration = ta.getInt(R.styleable.QiscusSmoothCheckBox_duration, DEF_ANIM_DURATION);
        floorColor = ta.getColor(R.styleable.QiscusSmoothCheckBox_color_unchecked_stroke, COLOR_FLOOR_UNCHECKED);
        checkedColor = ta.getColor(R.styleable.QiscusSmoothCheckBox_color_checked, COLOR_CHECKED);
        unCheckedColor = ta.getColor(R.styleable.QiscusSmoothCheckBox_color_unchecked, COLOR_UNCHECKED);
        strokeWidth = ta.getDimensionPixelSize(R.styleable.QiscusSmoothCheckBox_stroke_width,
                dp2px(getContext(), 0));
        ta.recycle();

        floorUnCheckedColor = floorColor;
        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);
        tickPaint.setColor(tickColor);

        floorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        floorPaint.setStyle(Paint.Style.FILL);
        floorPaint.setColor(floorColor);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(checkedColor);

        tickPath = new Path();
        centerPoint = new Point();
        tickPoints = new Point[3];
        tickPoints[0] = new Point();
        tickPoints[1] = new Point();
        tickPoints[2] = new Point();

        setOnClickListener(v -> {
            toggle();
            tickDrawing = false;
            drewDistance = 0;
            if (isChecked()) {
                startCheckedAnimation();
            } else {
                startUnCheckedAnimation();
            }
        });
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putBoolean(KEY_INSTANCE_STATE, isChecked());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            boolean isChecked = bundle.getBoolean(KEY_INSTANCE_STATE);
            setChecked(isChecked);
            super.onRestoreInstanceState(bundle.getParcelable(KEY_INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        this.setChecked(!isChecked());
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        reset();
        invalidate();
        if (listener != null) {
            listener.onCheckedChanged(SmoothCheckBox.this, this.checked);
        }
    }

    /**
     * <p>checked with animation</p>
     *
     * @param checked checked
     * @param animate change with animation
     */
    public void setChecked(boolean checked, boolean animate) {
        if (animate) {
            tickDrawing = false;
            this.checked = checked;
            drewDistance = 0f;
            if (checked) {
                startCheckedAnimation();
            } else {
                startUnCheckedAnimation();
            }
            if (listener != null) {
                listener.onCheckedChanged(SmoothCheckBox.this, this.checked);
            }

        } else {
            this.setChecked(checked);
        }
    }

    private void reset() {
        tickDrawing = true;
        floorScale = 1.0f;
        scaleVal = isChecked() ? 0f : 1.0f;
        floorColor = isChecked() ? checkedColor : floorUnCheckedColor;
        drewDistance = isChecked() ? (leftLineDistance + rightLineDistance) : 0;
    }

    private int measureSize(int measureSpec) {
        int defSize = dp2px(getContext(), DEF_DRAW_SIZE);
        int specSize = MeasureSpec.getSize(measureSpec);
        int specMode = MeasureSpec.getMode(measureSpec);

        int result = 0;
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = Math.min(defSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        width = getMeasuredWidth();
        strokeWidth = (strokeWidth == 0 ? getMeasuredWidth() / 10 : strokeWidth);
        strokeWidth = strokeWidth > getMeasuredWidth() / 5 ? getMeasuredWidth() / 5 : strokeWidth;
        strokeWidth = (strokeWidth < 3) ? 3 : strokeWidth;
        centerPoint.x = width / 2;
        centerPoint.y = getMeasuredHeight() / 2;

        tickPoints[0].x = Math.round((float) getMeasuredWidth() / 30 * 7);
        tickPoints[0].y = Math.round((float) getMeasuredHeight() / 30 * 14);
        tickPoints[1].x = Math.round((float) getMeasuredWidth() / 30 * 13);
        tickPoints[1].y = Math.round((float) getMeasuredHeight() / 30 * 20);
        tickPoints[2].x = Math.round((float) getMeasuredWidth() / 30 * 22);
        tickPoints[2].y = Math.round((float) getMeasuredHeight() / 30 * 10);

        leftLineDistance = (float) Math.sqrt(Math.pow(tickPoints[1].x - tickPoints[0].x, 2) +
                Math.pow(tickPoints[1].y - tickPoints[0].y, 2));
        rightLineDistance = (float) Math.sqrt(Math.pow(tickPoints[2].x - tickPoints[1].x, 2) +
                Math.pow(tickPoints[2].y - tickPoints[1].y, 2));
        tickPaint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBorder(canvas);
        drawCenter(canvas);
        drawTick(canvas);
    }

    private void drawCenter(Canvas canvas) {
        paint.setColor(unCheckedColor);
        float radius = (centerPoint.x - strokeWidth) * scaleVal;
        canvas.drawCircle(centerPoint.x, centerPoint.y, radius, paint);
    }

    private void drawBorder(Canvas canvas) {
        floorPaint.setColor(floorColor);
        int radius = centerPoint.x;
        canvas.drawCircle(centerPoint.x, centerPoint.y, radius * floorScale, floorPaint);
    }

    private void drawTick(Canvas canvas) {
        if (tickDrawing && isChecked()) {
            drawTickPath(canvas);
        }
    }

    private void drawTickPath(Canvas canvas) {
        tickPath.reset();
        // draw left of the tick
        if (drewDistance < leftLineDistance) {
            float step = (width / 20.0f) < 3 ? 3 : (width / 20.0f);
            drewDistance += step;
            float stopX = tickPoints[0].x + (tickPoints[1].x - tickPoints[0].x) * drewDistance / leftLineDistance;
            float stopY = tickPoints[0].y + (tickPoints[1].y - tickPoints[0].y) * drewDistance / leftLineDistance;

            tickPath.moveTo(tickPoints[0].x, tickPoints[0].y);
            tickPath.lineTo(stopX, stopY);
            canvas.drawPath(tickPath, tickPaint);

            if (drewDistance > leftLineDistance) {
                drewDistance = leftLineDistance;
            }
        } else {

            tickPath.moveTo(tickPoints[0].x, tickPoints[0].y);
            tickPath.lineTo(tickPoints[1].x, tickPoints[1].y);
            canvas.drawPath(tickPath, tickPaint);

            // draw right of the tick
            if (drewDistance < leftLineDistance + rightLineDistance) {
                float stopX = tickPoints[1].x + (tickPoints[2].x - tickPoints[1].x)
                        * (drewDistance - leftLineDistance) / rightLineDistance;
                float stopY = tickPoints[1].y - (tickPoints[1].y - tickPoints[2].y)
                        * (drewDistance - leftLineDistance) / rightLineDistance;

                tickPath.reset();
                tickPath.moveTo(tickPoints[1].x, tickPoints[1].y);
                tickPath.lineTo(stopX, stopY);
                canvas.drawPath(tickPath, tickPaint);

                float step = (width / 20) < 3 ? 3 : (width / 20);
                drewDistance += step;
            } else {
                tickPath.reset();
                tickPath.moveTo(tickPoints[1].x, tickPoints[1].y);
                tickPath.lineTo(tickPoints[2].x, tickPoints[2].y);
                canvas.drawPath(tickPath, tickPaint);
            }
        }

        // invalidate
        if (drewDistance < leftLineDistance + rightLineDistance) {
            postDelayed(this::postInvalidate, 10);
        }
    }

    private void startCheckedAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0f);
        animator.setDuration(animDuration / 3 * 2);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            scaleVal = (float) animation.getAnimatedValue();
            floorColor = getGradientColor(unCheckedColor, checkedColor, 1 - scaleVal);
            postInvalidate();
        });
        animator.start();

        ValueAnimator floorAnimator = ValueAnimator.ofFloat(1.0f, 0.8f, 1.0f);
        floorAnimator.setDuration(animDuration);
        floorAnimator.setInterpolator(new LinearInterpolator());
        floorAnimator.addUpdateListener(animation -> {
            floorScale = (float) animation.getAnimatedValue();
            postInvalidate();
        });
        floorAnimator.start();

        drawTickDelayed();
    }

    private void startUnCheckedAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1.0f);
        animator.setDuration(animDuration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            scaleVal = (float) animation.getAnimatedValue();
            floorColor = getGradientColor(checkedColor, COLOR_FLOOR_UNCHECKED, scaleVal);
            postInvalidate();
        });
        animator.start();

        ValueAnimator floorAnimator = ValueAnimator.ofFloat(1.0f, 0.8f, 1.0f);
        floorAnimator.setDuration(animDuration);
        floorAnimator.setInterpolator(new LinearInterpolator());
        floorAnimator.addUpdateListener(animation -> {
            floorScale = (float) animation.getAnimatedValue();
            postInvalidate();
        });
        floorAnimator.start();
    }

    private void drawTickDelayed() {
        postDelayed(() -> {
            tickDrawing = true;
            postInvalidate();
        }, animDuration);
    }

    private static int getGradientColor(int startColor, int endColor, float percent) {
        int sr = (startColor & 0xff0000) >> 0x10;
        int sg = (startColor & 0xff00) >> 0x8;
        int sb = (startColor & 0xff);

        int er = (endColor & 0xff0000) >> 0x10;
        int eg = (endColor & 0xff00) >> 0x8;
        int eb = (endColor & 0xff);

        int cr = (int) (sr * (1 - percent) + er * percent);
        int cg = (int) (sg * (1 - percent) + eg * percent);
        int cb = (int) (sb * (1 - percent) + eb * percent);
        return Color.argb(0xff, cr, cg, cb);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
        this.listener = l;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked);
    }

    public int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
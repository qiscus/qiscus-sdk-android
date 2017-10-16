package com.qiscus.sdk.chat.presentation.uikit.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;

import com.qiscus.sdk.chat.presentation.uikit.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int LAYOUT_DIRECTION_LTR = 0;
    public static final int LAYOUT_DIRECTION_RTL = 1;

    private final LayoutConfiguration config;
    List<LineDefinition> lines = new ArrayList<>();

    public FlowLayout(Context context) {
        super(context);
        config = new LayoutConfiguration(context, null);
    }

    public FlowLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        config = new LayoutConfiguration(context, attributeSet);
    }

    public FlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        config = new LayoutConfiguration(context, attributeSet);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingRight() - getPaddingLeft();
        final int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        final int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        final int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        final int controlMaxLength = config.getOrientation() == HORIZONTAL ? sizeWidth : sizeHeight;
        final int controlMaxThickness = config.getOrientation() == HORIZONTAL ? sizeHeight : sizeWidth;
        final int modeLength = config.getOrientation() == HORIZONTAL ? modeWidth : modeHeight;
        final int modeThickness = config.getOrientation() == HORIZONTAL ? modeHeight : modeWidth;

        lines.clear();
        LineDefinition currentLine = new LineDefinition(controlMaxLength);
        lines.add(currentLine);

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(), lp.width),
                    getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), lp.height)
            );

            lp.orientation = config.getOrientation();
            if (config.getOrientation() == FlowLayout.HORIZONTAL) {
                lp.setLength(child.getMeasuredWidth());
                lp.setThickness(child.getMeasuredHeight());
            } else {
                lp.setLength(child.getMeasuredHeight());
                lp.setThickness(child.getMeasuredWidth());
            }

            boolean newLine = lp.isNewLine() || (modeLength != MeasureSpec.UNSPECIFIED && !currentLine.canFit(child));
            if (newLine) {
                currentLine = new LineDefinition(controlMaxLength);
                if (config.getOrientation() == VERTICAL && config.getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                    lines.add(0, currentLine);
                } else {
                    lines.add(currentLine);
                }
            }

            if (config.getOrientation() == HORIZONTAL && config.getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                currentLine.addView(0, child);
            } else {
                currentLine.addView(child);
            }
        }

        calculateLinesAndChildPosition(lines);

        int contentLength = 0;
        final int linesCount = lines.size();
        for (int i = 0; i < linesCount; i++) {
            LineDefinition l = lines.get(i);
            contentLength = Math.max(contentLength, l.getLineLength());
        }
        int contentThickness = currentLine.getLineStartThickness() + currentLine.getLineThickness();

        int realControlLength = findSize(modeLength, controlMaxLength, contentLength);
        int realControlThickness = findSize(modeHeight, controlMaxThickness, contentThickness);

        applyGravityToLines(lines, realControlLength, realControlThickness);

        for (int i = 0; i < linesCount; i++) {
            LineDefinition line = lines.get(i);
            applyGravityToLine(line);
            applyPositionsToViews(line);
        }

        /* need to take padding into account */
        int totalControlWidth = getPaddingLeft() + getPaddingRight();
        int totalControlHeight = getPaddingBottom() + getPaddingTop();
        if (config.getOrientation() == HORIZONTAL) {
            totalControlWidth += contentLength;
            totalControlHeight += contentThickness;
        } else {
            totalControlWidth += contentThickness;
            totalControlHeight += contentLength;
        }
        setMeasuredDimension(resolveSize(totalControlWidth, widthMeasureSpec), resolveSize(totalControlHeight, heightMeasureSpec));
    }

    private int findSize(int modeSize, int controlMaxSize, int contentSize) {
        int realControlLength;
        switch (modeSize) {
            case MeasureSpec.UNSPECIFIED:
                realControlLength = contentSize;
                break;
            case MeasureSpec.AT_MOST:
                realControlLength = Math.min(contentSize, controlMaxSize);
                break;
            case MeasureSpec.EXACTLY:
                realControlLength = controlMaxSize;
                break;
            default:
                realControlLength = contentSize;
                break;
        }
        return realControlLength;
    }

    private void calculateLinesAndChildPosition(List<LineDefinition> lines) {
        int prevLinesThickness = 0;
        final int linesCount = lines.size();
        for (int i = 0; i < linesCount; i++) {
            final LineDefinition line = lines.get(i);
            line.setLineStartThickness(prevLinesThickness);
            prevLinesThickness += line.getLineThickness();
            int prevChildThickness = 0;
            final List<View> childViews = line.getViews();
            final int childCount = childViews.size();
            for (int j = 0; j < childCount; j++) {
                View child = childViews.get(j);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                lp.setInlineStartLength(prevChildThickness);
                prevChildThickness += lp.getLength() + lp.getSpacingLength();
            }
        }
    }

    private void applyPositionsToViews(LineDefinition line) {
        final List<View> childViews = line.getViews();
        final int childCount = childViews.size();
        for (int i = 0; i < childCount; i++) {
            final View child = childViews.get(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if (config.getOrientation() == HORIZONTAL) {
                layoutParams.setPosition(
                        getPaddingLeft() + line.getLineStartLength() + layoutParams.getInlineStartLength(),
                        getPaddingTop() + line.getLineStartThickness() + layoutParams.getInlineStartThickness());
                child.measure(
                        MeasureSpec.makeMeasureSpec(layoutParams.getLength(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(layoutParams.getThickness(), MeasureSpec.EXACTLY)
                );
            } else {
                layoutParams.setPosition(
                        getPaddingLeft() + line.getLineStartThickness() + layoutParams.getInlineStartThickness(),
                        getPaddingTop() + line.getLineStartLength() + layoutParams.getInlineStartLength());
                child.measure(
                        MeasureSpec.makeMeasureSpec(layoutParams.getThickness(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(layoutParams.getLength(), MeasureSpec.EXACTLY)
                );
            }
        }
    }

    private void applyGravityToLines(List<LineDefinition> lines, int realControlLength, int realControlThickness) {
        final int linesCount = lines.size();
        if (linesCount <= 0) {
            return;
        }

        LineDefinition lastLine = lines.get(linesCount - 1);
        int excessThickness = realControlThickness - (lastLine.getLineThickness() + lastLine.getLineStartThickness());

        if (excessThickness < 0) {
            excessThickness = 0;
        }

        int excessOffset = 0;
        for (int i = 0; i < linesCount; i++) {
            final LineDefinition child = lines.get(i);
            int weight = 1;
            int gravity = getGravity(null);
            int extraThickness = Math.round(excessThickness * weight / linesCount);

            final int childLength = child.getLineLength();
            final int childThickness = child.getLineThickness();

            Rect container = new Rect();
            container.top = excessOffset;
            container.left = 0;
            container.right = realControlLength;
            container.bottom = childThickness + extraThickness + excessOffset;

            Rect result = new Rect();
            Gravity.apply(gravity, childLength, childThickness, container, result);

            excessOffset += extraThickness;
            child.setLineStartLength(child.getLineStartLength() + result.left);
            child.setLineStartThickness(child.getLineStartThickness() + result.top);
            child.setLineLength(result.width());
            child.setLineThickness(result.height());
        }
    }

    private void applyGravityToLine(LineDefinition line) {
        final List<View> views = line.getViews();
        final int viewCount = views.size();
        if (viewCount <= 0) {
            return;
        }

        float totalWeight = 0;
        for (int i = 0; i < viewCount; i++) {
            final View prev = views.get(i);
            LayoutParams plp = (LayoutParams) prev.getLayoutParams();
            totalWeight += getWeight(plp);
        }

        View lastChild = views.get(viewCount - 1);
        LayoutParams lastChildLayoutParams = (LayoutParams) lastChild.getLayoutParams();
        int excessLength = line.getLineLength() - (lastChildLayoutParams.getLength() + lastChildLayoutParams.getSpacingLength() +
                lastChildLayoutParams.getInlineStartLength());
        int excessOffset = 0;
        for (int i = 0; i < viewCount; i++) {
            final View child = views.get(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();

            float weight = getWeight(layoutParams);
            int gravity = getGravity(layoutParams);
            int extraLength;
            if (totalWeight == 0) {
                extraLength = excessLength / viewCount;
            } else {
                extraLength = Math.round(excessLength * weight / totalWeight);
            }

            final int childLength = layoutParams.getLength() + layoutParams.getSpacingLength();
            final int childThickness = layoutParams.getThickness() + layoutParams.getSpacingThickness();

            Rect container = new Rect();
            container.top = 0;
            container.left = excessOffset;
            container.right = childLength + extraLength + excessOffset;
            container.bottom = line.getLineThickness();

            Rect result = new Rect();
            Gravity.apply(gravity, childLength, childThickness, container, result);

            excessOffset += extraLength;
            layoutParams.setInlineStartLength(result.left + layoutParams.getInlineStartLength());
            layoutParams.setInlineStartThickness(result.top);
            layoutParams.setLength(result.width() - layoutParams.getSpacingLength());
            layoutParams.setThickness(result.height() - layoutParams.getSpacingThickness());
        }
    }

    private int getGravity(LayoutParams lp) {
        int parentGravity = config.getGravity();

        int childGravity;
        // get childGravity of child view (if exists)
        if (lp != null && lp.gravitySpecified()) {
            childGravity = lp.getGravity();
        } else {
            childGravity = parentGravity;
        }

        childGravity = getGravityFromRelative(childGravity);
        parentGravity = getGravityFromRelative(parentGravity);

        // add parent gravity to child gravity if child gravity is not specified
        if ((childGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
            childGravity |= parentGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        }
        if ((childGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            childGravity |= parentGravity & Gravity.VERTICAL_GRAVITY_MASK;
        }

        // if childGravity is still not specified - set default top - left gravity
        if ((childGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
            childGravity |= Gravity.LEFT;
        }
        if ((childGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            childGravity |= Gravity.TOP;
        }

        return childGravity;
    }

    private int getGravityFromRelative(int childGravity) {
        // swap directions for vertical non relative view
        // if it is relative, then START is TOP, and we do not need to switch it here.
        // it will be switched later on onMeasure stage when calculations will be with length and thickness
        if (config.getOrientation() == VERTICAL && (childGravity & Gravity.RELATIVE_LAYOUT_DIRECTION) == 0) {
            int horizontalGravity = childGravity;
            childGravity = 0;
            childGravity |= (horizontalGravity & Gravity.HORIZONTAL_GRAVITY_MASK) << Gravity.AXIS_Y_SHIFT;
            childGravity |= (horizontalGravity & Gravity.VERTICAL_GRAVITY_MASK) >> Gravity.AXIS_Y_SHIFT;
        }

        // for relative layout and RTL direction swap left and right gravity
        if (config.getLayoutDirection() == LAYOUT_DIRECTION_RTL && (childGravity & Gravity.RELATIVE_LAYOUT_DIRECTION) != 0) {
            int ltrGravity = childGravity;
            childGravity = 0;
            childGravity |= (ltrGravity & Gravity.LEFT) == Gravity.LEFT ? Gravity.RIGHT : 0;
            childGravity |= (ltrGravity & Gravity.RIGHT) == Gravity.RIGHT ? Gravity.LEFT : 0;
        }

        return childGravity;
    }

    private float getWeight(LayoutParams lp) {
        return lp.weightSpecified() ? lp.getWeight() : config.getWeightDefault();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout(lp.x + lp.leftMargin, lp.y + lp.topMargin,
                    lp.x + lp.leftMargin + child.getMeasuredWidth(), lp.y + lp.topMargin + child.getMeasuredHeight());
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean more = super.drawChild(canvas, child, drawingTime);
        drawDebugInfo(canvas, child);
        return more;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    private void drawDebugInfo(Canvas canvas, View child) {
        if (!isDebugDraw()) {
            return;
        }

        Paint childPaint = createPaint(0xffffff00);
        Paint newLinePaint = createPaint(0xffff0000);

        LayoutParams lp = (LayoutParams) child.getLayoutParams();

        if (lp.rightMargin > 0) {
            float x = child.getRight();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x + lp.rightMargin, y, childPaint);
            canvas.drawLine(x + lp.rightMargin - 4.0f, y - 4.0f, x + lp.rightMargin, y, childPaint);
            canvas.drawLine(x + lp.rightMargin - 4.0f, y + 4.0f, x + lp.rightMargin, y, childPaint);
        }

        if (lp.leftMargin > 0) {
            float x = child.getLeft();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x - lp.leftMargin, y, childPaint);
            canvas.drawLine(x - lp.leftMargin + 4.0f, y - 4.0f, x - lp.leftMargin, y, childPaint);
            canvas.drawLine(x - lp.leftMargin + 4.0f, y + 4.0f, x - lp.leftMargin, y, childPaint);
        }

        if (lp.bottomMargin > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getBottom();
            canvas.drawLine(x, y, x, y + lp.bottomMargin, childPaint);
            canvas.drawLine(x - 4.0f, y + lp.bottomMargin - 4.0f, x, y + lp.bottomMargin, childPaint);
            canvas.drawLine(x + 4.0f, y + lp.bottomMargin - 4.0f, x, y + lp.bottomMargin, childPaint);
        }

        if (lp.topMargin > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getTop();
            canvas.drawLine(x, y, x, y - lp.topMargin, childPaint);
            canvas.drawLine(x - 4.0f, y - lp.topMargin + 4.0f, x, y - lp.topMargin, childPaint);
            canvas.drawLine(x + 4.0f, y - lp.topMargin + 4.0f, x, y - lp.topMargin, childPaint);
        }

        if (lp.isNewLine()) {
            if (config.getOrientation() == HORIZONTAL) {
                float x = child.getLeft();
                float y = child.getTop() + child.getHeight() / 2.0f;
                canvas.drawLine(x, y - 6.0f, x, y + 6.0f, newLinePaint);
            } else {
                float x = child.getLeft() + child.getWidth() / 2.0f;
                float y = child.getTop();
                canvas.drawLine(x - 6.0f, y, x + 6.0f, y, newLinePaint);
            }
        }
    }

    private Paint createPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(2.0f);
        return paint;
    }

    public int getOrientation() {
        return config.getOrientation();
    }

    public void setOrientation(int orientation) {
        config.setOrientation(orientation);
        requestLayout();
    }

    public boolean isDebugDraw() {
        return config.isDebugDraw() || debugDraw();
    }

    public void setDebugDraw(boolean debugDraw) {
        config.setDebugDraw(debugDraw);
        invalidate();
    }

    private boolean debugDraw() {
        try {
            // android add this method at 4.1
            @SuppressLint("PrivateApi") Method m = ViewGroup.class.getDeclaredMethod("debugDraw", (Class[]) null);
            m.setAccessible(true);
            return (boolean) m.invoke(this, new Object[]{null});
        } catch (Exception e) {
            // if no such method (android not support this at lower api level), return false
            // ignore this, it's safe here
        }

        return false;
    }

    public float getWeightDefault() {
        return config.getWeightDefault();
    }

    public void setWeightDefault(float weightDefault) {
        config.setWeightDefault(weightDefault);
        requestLayout();
    }

    public int getGravity() {
        return config.getGravity();
    }

    public void setGravity(int gravity) {
        config.setGravity(gravity);
        requestLayout();
    }

    @SuppressLint("WrongConstant")
    public int getLayoutDirection() {
        if (config == null) {
            // Workaround for android sdk that wants to use virtual methods within constructor.
            return LAYOUT_DIRECTION_LTR;
        }

        return config.getLayoutDirection();
    }

    public void setLayoutDirection(int layoutDirection) {
        config.setLayoutDirection(layoutDirection);
        requestLayout();
    }

    public static class LayoutParams extends MarginLayoutParams {
        @ViewDebug.ExportedProperty(mapping = {
                @ViewDebug.IntToString(from = Gravity.NO_GRAVITY, to = "NONE"),
                @ViewDebug.IntToString(from = Gravity.TOP, to = "TOP"),
                @ViewDebug.IntToString(from = Gravity.BOTTOM, to = "BOTTOM"),
                @ViewDebug.IntToString(from = Gravity.LEFT, to = "LEFT"),
                @ViewDebug.IntToString(from = Gravity.RIGHT, to = "RIGHT"),
                @ViewDebug.IntToString(from = Gravity.CENTER_VERTICAL, to = "CENTER_VERTICAL"),
                @ViewDebug.IntToString(from = Gravity.FILL_VERTICAL, to = "FILL_VERTICAL"),
                @ViewDebug.IntToString(from = Gravity.CENTER_HORIZONTAL, to = "CENTER_HORIZONTAL"),
                @ViewDebug.IntToString(from = Gravity.FILL_HORIZONTAL, to = "FILL_HORIZONTAL"),
                @ViewDebug.IntToString(from = Gravity.CENTER, to = "CENTER"),
                @ViewDebug.IntToString(from = Gravity.FILL, to = "FILL")
        })

        private boolean newLine = false;
        private int gravity = Gravity.NO_GRAVITY;
        private float weight = -1.0f;
        private int inlineStartLength;
        private int length;
        private int thickness;
        private int inlineStartThickness;
        private int x;
        private int y;
        private int orientation;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            readStyleParameters(context, attributeSet);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public boolean gravitySpecified() {
            return gravity != Gravity.NO_GRAVITY;
        }

        public boolean weightSpecified() {
            return weight >= 0;
        }

        private void readStyleParameters(Context context, AttributeSet attributeSet) {
            TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout_Layout);
            try {
                newLine = a.getBoolean(R.styleable.FlowLayout_Layout_layout_newLine, false);
                gravity = a.getInt(R.styleable.FlowLayout_Layout_android_layout_gravity, Gravity.NO_GRAVITY);
                weight = a.getFloat(R.styleable.FlowLayout_Layout_layout_weight, -1.0f);
            } finally {
                a.recycle();
            }
        }


        void setPosition(int x, int y) {
            x = x;
            y = y;
        }

        int getInlineStartLength() {
            return inlineStartLength;
        }

        void setInlineStartLength(int inlineStartLength) {
            inlineStartLength = inlineStartLength;
        }

        int getLength() {
            return length;
        }

        void setLength(int length) {
            length = length;
        }

        int getThickness() {
            return thickness;
        }

        void setThickness(int thickness) {
            thickness = thickness;
        }

        int getInlineStartThickness() {
            return inlineStartThickness;
        }

        void setInlineStartThickness(int inlineStartThickness) {
            inlineStartThickness = inlineStartThickness;
        }

        int getSpacingLength() {
            if (orientation == FlowLayout.HORIZONTAL) {
                return leftMargin + rightMargin;
            } else {
                return topMargin + bottomMargin;
            }
        }

        int getSpacingThickness() {
            if (orientation == FlowLayout.HORIZONTAL) {
                return topMargin + bottomMargin;
            } else {
                return leftMargin + rightMargin;
            }
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getGravity() {
            return gravity;
        }

        public void setGravity(int gravity) {
            gravity = gravity;
        }

        public float getWeight() {
            return weight;
        }

        public void setWeight(float weight) {
            weight = weight;
        }

        public boolean isNewLine() {
            return newLine;
        }

        public void setNewLine(boolean newLine) {
            newLine = newLine;
        }
    }
}
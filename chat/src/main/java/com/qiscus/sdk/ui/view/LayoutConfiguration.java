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

package com.qiscus.sdk.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;

import com.qiscus.sdk.R;

class LayoutConfiguration {
    private int orientation = QiscusFlowLayout.HORIZONTAL;
    private boolean debugDraw = false;
    private float weightDefault = 0;
    private int gravity = Gravity.LEFT | Gravity.TOP;
    private int layoutDirection = QiscusFlowLayout.LAYOUT_DIRECTION_LTR;

    public LayoutConfiguration(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.QiscusFlowLayout);
        try {
            this.setOrientation(a.getInteger(R.styleable.QiscusFlowLayout_android_orientation, QiscusFlowLayout.HORIZONTAL));
            this.setDebugDraw(a.getBoolean(R.styleable.QiscusFlowLayout_debugDraw, false));
            this.setWeightDefault(a.getFloat(R.styleable.QiscusFlowLayout_weightDefault, 0.0f));
            this.setGravity(a.getInteger(R.styleable.QiscusFlowLayout_android_gravity, Gravity.NO_GRAVITY));
            this.setLayoutDirection(a.getInteger(R.styleable.QiscusFlowLayout_layoutDirection, QiscusFlowLayout.LAYOUT_DIRECTION_LTR));
        } finally {
            a.recycle();
        }
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setOrientation(int orientation) {
        if (orientation == QiscusFlowLayout.VERTICAL) {
            this.orientation = orientation;
        } else {
            this.orientation = QiscusFlowLayout.HORIZONTAL;
        }
    }

    public boolean isDebugDraw() {
        return this.debugDraw;
    }

    public void setDebugDraw(boolean debugDraw) {
        this.debugDraw = debugDraw;
    }

    public float getWeightDefault() {
        return this.weightDefault;
    }

    public void setWeightDefault(float weightDefault) {
        this.weightDefault = Math.max(0, weightDefault);
    }

    public int getGravity() {
        return this.gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public int getLayoutDirection() {
        return layoutDirection;
    }

    public void setLayoutDirection(int layoutDirection) {
        if (layoutDirection == QiscusFlowLayout.LAYOUT_DIRECTION_RTL) {
            this.layoutDirection = layoutDirection;
        } else {
            this.layoutDirection = QiscusFlowLayout.LAYOUT_DIRECTION_LTR;
        }
    }
}

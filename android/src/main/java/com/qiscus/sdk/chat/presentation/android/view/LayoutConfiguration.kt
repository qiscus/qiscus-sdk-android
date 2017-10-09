package com.qiscus.sdk.chat.presentation.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import com.qiscus.sdk.chat.presentation.android.R

internal class LayoutConfiguration(context: Context, attributeSet: AttributeSet) {

    var orientation = FlowLayout.HORIZONTAL
        set(orientation) = if (orientation == FlowLayout.VERTICAL) {
            field = orientation
        } else {
            field = FlowLayout.HORIZONTAL
        }

    var isDebugDraw = false

    var weightDefault = 0f
        set(weightDefault) {
            field = Math.max(0f, weightDefault)
        }

    var gravity = Gravity.LEFT or Gravity.TOP

    var layoutDirection = FlowLayout.LAYOUT_DIRECTION_LTR
        set(layoutDirection) = if (layoutDirection == FlowLayout.LAYOUT_DIRECTION_RTL) {
            field = layoutDirection
        } else {
            field = FlowLayout.LAYOUT_DIRECTION_LTR
        }

    init {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout)
        try {
            this.orientation = a.getInteger(R.styleable.FlowLayout_android_orientation, FlowLayout.HORIZONTAL)
            this.isDebugDraw = a.getBoolean(R.styleable.FlowLayout_debugDraw, false)
            this.weightDefault = a.getFloat(R.styleable.FlowLayout_weightDefault, 0.0f)
            this.gravity = a.getInteger(R.styleable.FlowLayout_android_gravity, Gravity.NO_GRAVITY)
            this.layoutDirection = a.getInteger(R.styleable.FlowLayout_layoutDirection, FlowLayout.LAYOUT_DIRECTION_LTR)
        } finally {
            a.recycle()
        }
    }
}
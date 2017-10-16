package com.qiscus.sdk.chat.presentation.uikit.view

import android.view.View

import java.util.ArrayList

internal class LineDefinition(private val maxLength: Int) {
    val views = ArrayList<View>()

    var lineLength: Int = 0
    var lineThickness: Int = 0
    var lineStartThickness: Int = 0
    var lineStartLength: Int = 0

    init {
        lineStartThickness = 0
        lineStartLength = 0
    }

    fun addView(child: View) {
        addView(views.size, child)
    }

    fun addView(i: Int, child: View) {
        val lp = child.layoutParams as FlowLayout.LayoutParams

        views.add(i, child)

        lineLength += lp.length + lp.spacingLength
        lineThickness = Math.max(lineThickness, lp.thickness + lp.spacingThickness)
    }

    fun canFit(child: View): Boolean {
        val lp = child.layoutParams as FlowLayout.LayoutParams
        return lineLength + lp.length + lp.spacingLength <= maxLength
    }
}
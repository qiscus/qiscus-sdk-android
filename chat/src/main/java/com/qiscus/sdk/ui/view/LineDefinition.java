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

import android.view.View;

import java.util.ArrayList;
import java.util.List;

class LineDefinition {
    private final List<View> views = new ArrayList<>();
    private final int maxLength;
    private int lineLength;
    private int lineThickness;
    private int lineStartThickness;
    private int lineStartLength;

    public LineDefinition(int maxLength) {
        this.lineStartThickness = 0;
        this.lineStartLength = 0;
        this.maxLength = maxLength;
    }

    public void addView(View child) {
        this.addView(this.views.size(), child);
    }

    public void addView(int i, View child) {
        final QiscusFlowLayout.LayoutParams lp = (QiscusFlowLayout.LayoutParams) child.getLayoutParams();

        this.views.add(i, child);

        this.lineLength = this.lineLength + lp.getLength() + lp.getSpacingLength();
        this.lineThickness = Math.max(this.lineThickness, lp.getThickness() + lp.getSpacingThickness());
    }

    public boolean canFit(View child) {
        final QiscusFlowLayout.LayoutParams lp = (QiscusFlowLayout.LayoutParams) child.getLayoutParams();
        return lineLength + lp.getLength() + lp.getSpacingLength() <= maxLength;
    }

    public int getLineStartThickness() {
        return lineStartThickness;
    }

    public void setLineStartThickness(int lineStartThickness) {
        this.lineStartThickness = lineStartThickness;
    }

    public int getLineThickness() {
        return lineThickness;
    }

    public int getLineLength() {
        return lineLength;
    }

    public int getLineStartLength() {
        return lineStartLength;
    }

    public void setLineStartLength(int lineStartLength) {
        this.lineStartLength = lineStartLength;
    }

    public List<View> getViews() {
        return views;
    }

    public void setThickness(int thickness) {
        this.lineThickness = thickness;
    }

    public void setLength(int length) {
        this.lineLength = length;
    }
}
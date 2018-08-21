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

package com.qiscus.sdk.chat.core.data.model;

/**
 * Created on : March 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusImageCompressionConfig {
    private float maxHeight = 900.0f;
    private float maxWidth = 1440.0f;
    private int quality = 80;

    public QiscusImageCompressionConfig() {

    }

    public QiscusImageCompressionConfig(float maxHeight, float maxWidth, int quality) {
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
        this.quality = quality;
    }

    public QiscusImageCompressionConfig setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public QiscusImageCompressionConfig setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public QiscusImageCompressionConfig setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public int getQuality() {
        return quality;
    }
}

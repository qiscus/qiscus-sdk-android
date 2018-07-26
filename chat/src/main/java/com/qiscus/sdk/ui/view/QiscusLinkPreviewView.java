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
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.remote.QiscusUrlScraper;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.schinizer.rxunfurl.model.PreviewData;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : December 09, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusLinkPreviewView extends LinearLayout {

    private ImageView image;
    private TextView title;
    private TextView description;
    private PreviewData previewData;

    public QiscusLinkPreviewView(Context context) {
        super(context);
    }

    public QiscusLinkPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        injectViews();
        applyAttrs(context, attrs);
    }

    private void injectViews() {
        inflate(getContext(), R.layout.view_qiscus_link_preview, this);
        image = findViewById(R.id.image);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
    }

    private void applyAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.QiscusLinkPreviewView, 0, 0);
        int titleColor;
        int descColor;
        try {
            titleColor = a.getColor(R.styleable.QiscusLinkPreviewView_qpreview_titleColor,
                    ContextCompat.getColor(getContext(), R.color.qiscus_white));
            descColor = a.getColor(R.styleable.QiscusLinkPreviewView_qpreview_descColor,
                    ContextCompat.getColor(getContext(), R.color.qiscus_white));
        } finally {
            a.recycle();
        }

        title.setTextColor(titleColor);
        description.setTextColor(descColor);

        initLayout();
    }

    private void initLayout() {
        bind(previewData);
        setOnClickListener(v -> openUrl());
    }

    public void bind(PreviewData previewData) {
        this.previewData = previewData;
        if (previewData == null) {
            setVisibility(GONE);
        } else {
            image.setBackgroundColor(QiscusAndroidUtil.getRandomColor());
            if (previewData.getImages().size() > 0) {
                Nirmana.getInstance().get()
                        .load(previewData.getImages().get(0).getSource())
                        .into(image);
            } else {
                Nirmana.getInstance().get().load("clear it").into(image);
            }
            title.setText(previewData.getTitle().isEmpty() ?
                    getContext().getString(R.string.qiscus_link_preview_default_title) : previewData.getTitle());
            description.setText(previewData.getDescription().isEmpty() ?
                    getContext().getString(R.string.qiscus_link_preview_default_description) : previewData.getDescription());
            setVisibility(VISIBLE);
        }
    }

    private void openUrl() {
        new CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(getContext(), Qiscus.getChatConfig().getAppBarColor()))
                .setShowTitle(true)
                .addDefaultShareMenuItem()
                .enableUrlBarHiding()
                .build()
                .launchUrl(getContext(), Uri.parse(previewData.getUrl()));
    }

    public void setTitleColor(@ColorInt int color) {
        title.setTextColor(color);
    }

    public void setDescriptionColor(@ColorInt int color) {
        description.setTextColor(color);
    }

    public void load(String url) {
        setVisibility(GONE);
        if (previewData == null || !previewData.getUrl().equals(url)) {
            QiscusUrlScraper.getInstance()
                    .generatePreviewData(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(previewData -> {
                        this.previewData = previewData;
                        bind(this.previewData);
                    }, Throwable::printStackTrace);
        } else {
            bind(previewData);
        }
    }

    public void clearView() {
        previewData = null;
        bind(previewData);
    }
}

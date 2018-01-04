package com.qiscus.sdk.chat.presentation.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qiscus.sdk.chat.core.Qiscus;
import com.qiscus.sdk.chat.data.remote.WebScrapper;
import com.qiscus.sdk.chat.presentation.uikit.R;
import com.qiscus.sdk.chat.presentation.uikit.util.ColorUtilKt;
import com.schinizer.rxunfurl.model.PreviewData;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WebPreviewView extends LinearLayout {

    private ImageView image;
    private TextView title;
    private TextView description;
    private PreviewData previewData;

    private WebScrapper webScrapper = Qiscus.Companion.getInstance().getComponent()
            .getDataComponent().getWebScrapper();

    public WebPreviewView(Context context) {
        super(context);
    }

    public WebPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        injectViews();
        applyAttrs(context, attrs);
    }

    private void injectViews() {
        inflate(getContext(), R.layout.view_web_preview, this);
        image = findViewById(R.id.image);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
    }

    private void applyAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WebPreviewView, 0, 0);
        int titleColor;
        int descColor;
        try {
            titleColor = a.getColor(R.styleable.WebPreviewView_titleColor, Color.WHITE);
            descColor = a.getColor(R.styleable.WebPreviewView_descColor, Color.WHITE);
        } finally {
            a.recycle();
        }

        title.setTextColor(titleColor);
        description.setTextColor(descColor);

        initLayout();
    }

    private void initLayout() {
        bind(previewData);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl();
            }
        });
    }

    public void bind(PreviewData previewData) {
        this.previewData = previewData;
        if (previewData == null) {
            setVisibility(GONE);
        } else {
            image.setBackgroundColor(ColorUtilKt.randomColor());
            if (previewData.getImages().size() > 0) {
                Glide.with(this)
                        .load(previewData.getImages().get(0).getSource())
                        .into(image);
            } else {
                Glide.with(this).load("clear it").into(image);
            }
            title.setText(previewData.getTitle().isEmpty() ?
                    getContext().getString(R.string.qiscus_web_preview_default_title) : previewData.getTitle());
            description.setText(previewData.getDescription().isEmpty() ?
                    getContext().getString(R.string.qiscus_web_preview_default_description) : previewData.getDescription());
            setVisibility(VISIBLE);
        }
    }

    private void openUrl() {
        new CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(getContext(), R.color.qiscus_primary))
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
            webScrapper.generatePreviewData(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<PreviewData>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(PreviewData previewData) {
                            WebPreviewView.this.previewData = previewData;
                            bind(WebPreviewView.this.previewData);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        } else {
            bind(previewData);
        }
    }

    public void clearView() {
        previewData = null;
        bind(null);
    }
}

package com.qiscus.sdk.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class BaseImageView extends ImageView {
    private String imageUrl;

    public BaseImageView(Context context) {
        super(context);
    }

    public BaseImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageUrl(String url) {
        imageUrl = url;
        Glide.with(getContext())
                .load(url)
                .into(this);
    }

    public void setImageUrl(String url, int placeHolderResourceId) {
        imageUrl = url;
        Glide.with(getContext())
                .load(url)
                .placeholder(placeHolderResourceId)
                .error(placeHolderResourceId)
                .into(this);
    }

    public void setImageUrl(String url, int placeHolderResourceId, int errorResourceId) {
        imageUrl = url;
        Glide.with(getContext())
                .load(url)
                .placeholder(placeHolderResourceId)
                .error(errorResourceId)
                .into(this);
    }

    public void setImageUrl(String url, int placeHolderDrawable, Drawable errorDrawable) {
        imageUrl = url;
        Glide.with(getContext())
                .load(url)
                .placeholder(placeHolderDrawable)
                .error(errorDrawable)
                .into(this);
    }

    public void setImageUrl(String url, final ProgressBar progressBar) {
        imageUrl = url;
        progressBar.setVisibility(VISIBLE);
        Glide.with(getContext())
                .load(url)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        return false;
                    }
                })
                .into(this);
    }

    public void setImageUrl(String url, final ProgressBar progressBar, int errorResourceId) {
        imageUrl = url;
        progressBar.setVisibility(VISIBLE);
        Glide.with(getContext())
                .load(url)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        return false;
                    }
                })
                .error(errorResourceId)
                .into(this);
    }

    public void setImageUrl(String url, final ProgressBar progressBar, Drawable errorDrawable) {
        imageUrl = url;
        progressBar.setVisibility(VISIBLE);
        Glide.with(getContext())
                .load(url)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        return false;
                    }
                })
                .error(errorDrawable)
                .into(this);
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

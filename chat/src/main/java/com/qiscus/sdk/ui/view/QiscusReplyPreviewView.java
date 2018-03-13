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
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusReplyPanelConfig;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.util.QiscusImageUtil;
import com.qiscus.sdk.util.QiscusSpannableBuilder;
import com.qiscus.sdk.util.QiscusTextUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on : December 09, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusReplyPreviewView extends LinearLayout {

    private View rootView;
    private View bar;
    private TextView sender;
    private TextView content;
    private ImageView image;
    private ImageView icon;
    private View closeView;
    private QiscusComment originComment;

    private Map<String, QiscusRoomMember> members = new HashMap<>();

    public QiscusReplyPreviewView(Context context) {
        super(context);
    }

    public QiscusReplyPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        injectViews();
        applyAttrs(context, attrs);
    }

    private void injectViews() {
        inflate(getContext(), R.layout.view_qiscus_reply_preview, this);
        rootView = findViewById(R.id.root_view);
        bar = findViewById(R.id.bar);
        sender = findViewById(R.id.origin_sender);
        content = findViewById(R.id.origin_content);
        image = findViewById(R.id.origin_image);
        icon = findViewById(R.id.icon);
        closeView = findViewById(R.id.cancel_reply);
    }

    private void applyAttrs(Context context, AttributeSet attrs) {
        initLayout();
    }

    private void initLayout() {
        bind(originComment);
        closeView.setOnClickListener(v -> close());
    }

    public void updateMember(List<QiscusRoomMember> roomMembers) {
        members.clear();
        for (QiscusRoomMember roomMember : roomMembers) {
            members.put(roomMember.getEmail(), roomMember);
        }
    }

    public QiscusComment getOriginComment() {
        return originComment;
    }

    public void bind(QiscusComment originComment) {
        this.originComment = originComment;
        if (originComment == null) {
            content.setText(null);
            image.setImageBitmap(null);
            icon.setImageBitmap(null);
            content.setText(null);
            setVisibility(GONE);
        } else {
            configureColor(originComment);
            sender.setText(Qiscus.getChatConfig().getRoomSenderNameInterceptor()
                    .getSenderName(originComment));
            switch (originComment.getType()) {
                case IMAGE:
                case VIDEO:
                    image.setVisibility(VISIBLE);
                    icon.setVisibility(GONE);
                    File localPath = Qiscus.getDataStore().getLocalPath(originComment.getId());
                    if (localPath == null) {
                        showBlurryImage(originComment);
                    } else {
                        showImage(localPath);
                    }
                    content.setText(TextUtils.isEmpty(originComment.getCaption()) ?
                            originComment.getAttachmentName() :
                            new QiscusSpannableBuilder(originComment.getCaption(), members).build().toString());
                    break;
                case AUDIO:
                    image.setVisibility(GONE);
                    icon.setVisibility(VISIBLE);
                    icon.setImageResource(R.drawable.ic_qiscus_add_audio);
                    content.setText(QiscusTextUtil.getString(R.string.qiscus_voice_message));
                    break;
                case FILE:
                    image.setVisibility(GONE);
                    icon.setVisibility(VISIBLE);
                    icon.setImageResource(R.drawable.ic_qiscus_file);
                    content.setText(originComment.getAttachmentName());
                    break;
                case CONTACT:
                    image.setVisibility(GONE);
                    icon.setVisibility(VISIBLE);
                    icon.setImageResource(R.drawable.ic_qiscus_add_contact);
                    content.setText(QiscusTextUtil.getString(R.string.qiscus_contact) + ": "
                            + originComment.getContact().getName());
                    break;
                case LOCATION:
                    image.setVisibility(GONE);
                    icon.setVisibility(VISIBLE);
                    icon.setImageResource(R.drawable.ic_qiscus_location);
                    content.setText(originComment.getMessage());
                    break;
                default:
                    image.setVisibility(GONE);
                    icon.setVisibility(GONE);
                    content.setText(new QiscusSpannableBuilder(originComment.getMessage(), members).build().toString());
                    break;

            }
            setVisibility(VISIBLE);
        }
    }

    private void configureColor(QiscusComment originComment) {
        QiscusReplyPanelConfig config = Qiscus.getChatConfig().getStartReplyInterceptor()
                .getReplyPanelConfig(originComment);
        setSenderColor(config.getSenderNameColor());
        setContentColor(config.getMessageColor());
        setBackgroundColor(config.getBackgroundColor());
        setBarColor(config.getBarColor());
        setCancelIcon(config.getCancelIconResourceId());
        setCancelIconTint(config.getCancelIconTintColor());
    }

    public void setBarColor(@ColorInt int color) {
        bar.setBackgroundColor(color);
    }

    public void setSenderColor(@ColorInt int color) {
        sender.setTextColor(color);
    }

    public void setContentColor(@ColorInt int color) {
        content.setTextColor(color);
    }

    public void setBackgroundColor(@ColorInt int color) {
        rootView.setBackgroundColor(color);
    }

    public void setCancelIcon(@DrawableRes int iconResource) {
        icon.setImageResource(iconResource);
    }

    public void setCancelIconTint(@ColorInt int tintColor) {
        icon.setColorFilter(tintColor);
    }

    private void showImage(File file) {
        Nirmana.getInstance().get()
                .load(file)
                .asBitmap()
                .centerCrop()
                .dontAnimate()
                .thumbnail(0.5f)
                .placeholder(R.drawable.qiscus_image_placeholder)
                .error(R.drawable.qiscus_image_placeholder)
                .into(image);
    }

    private void showBlurryImage(QiscusComment qiscusComment) {
        Nirmana.getInstance().get()
                .load(QiscusImageUtil.generateBlurryThumbnailUrl(qiscusComment.getAttachmentUri().toString()))
                .asBitmap()
                .centerCrop()
                .dontAnimate()
                .thumbnail(0.5f)
                .placeholder(R.drawable.qiscus_image_placeholder)
                .error(R.drawable.qiscus_image_placeholder)
                .into(image);
    }

    public void close() {
        originComment = null;
        bind(originComment);
    }
}

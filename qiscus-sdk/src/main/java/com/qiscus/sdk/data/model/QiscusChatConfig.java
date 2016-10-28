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

package com.qiscus.sdk.data.model;

import android.content.Intent;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.widget.Toast;

import com.qiscus.sdk.R;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.ui.QiscusChatActivity;
import com.qiscus.sdk.util.QiscusDateUtil;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zetra. on 9/5/16.
 */
public class QiscusChatConfig {
    private int statusBarColor = R.color.qiscus_primary_dark;
    private int appBarColor = R.color.qiscus_primary;
    private int titleColor = R.color.qiscus_white;
    private int subtitleColor = R.color.qiscus_dark_white;
    private int rightBubbleColor = R.color.qiscus_primary_light;
    private int leftBubbleColor = R.color.qiscus_primary;
    private int rightBubbleTextColor = R.color.qiscus_primary_text;
    private int leftBubbleTextColor = R.color.qiscus_white;
    private int rightBubbleTimeColor = R.color.qiscus_secondary_text;
    private int leftBubbleTimeColor = R.color.qiscus_primary_light;
    private int failedToSendMessageColor = R.color.qiscus_red;
    private int dateColor = R.color.qiscus_secondary_text;
    private DateFormatter dateFormat = QiscusDateUtil::toTodayOrDate;
    private DateFormatter timeFormat = QiscusDateUtil::toHour;
    private String emptyRoomTitle = "Welcome!";
    private String emptyRoomSubtitle = "Lets start conversation";
    private int emptyRoomImageResource = R.drawable.ic_qiscus_chat_empty;
    private String messageFieldHint = "Type a message…";
    private int addPictureIcon = R.drawable.ic_qiscus_add_image;
    private int takePictureIcon = R.drawable.ic_qiscus_pick_picture;
    private int addFileIcon = R.drawable.ic_qiscus_add_file;
    private int sendActiveIcon = R.drawable.ic_qiscus_send_on;
    private int sendInactiveIcon = R.drawable.ic_qiscus_send_off;
    private int[] swipeRefreshColorScheme = new int[]{R.color.qiscus_primary, R.color.qiscus_accent};
    private int notificationSmallIcon = R.drawable.ic_qiscus_notif_app;
    private int notificationBigIcon = R.drawable.ic_qiscus_notif_app;
    private NotificationTitleHandler notificationTitleHandler = QiscusComment::getSender;
    private NotificationClickListener notificationClickListener =
            (context, qiscusComment) -> QiscusApi.getInstance()
                    .getChatRoom(qiscusComment.getRoomId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(qiscusChatRoom -> qiscusChatRoom.setName(qiscusComment.getSender()))
                    .map(qiscusChatRoom -> QiscusChatActivity.generateIntent(context, qiscusChatRoom))
                    .subscribe(intent -> {
                        context.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }, throwable -> {
                        throwable.printStackTrace();
                        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    });

    public QiscusChatConfig setStatusBarColor(@ColorRes int statusBarColor) {
        this.statusBarColor = statusBarColor;
        return this;
    }

    public QiscusChatConfig setAppBarColor(@ColorRes int appBarColor) {
        this.appBarColor = appBarColor;
        return this;
    }

    public QiscusChatConfig setTitleColor(@ColorRes int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    public QiscusChatConfig setSubtitleColor(@ColorRes int subtitleColor) {
        this.subtitleColor = subtitleColor;
        return this;
    }

    public QiscusChatConfig setRightBubbleColor(@ColorRes int rightBubbleColor) {
        this.rightBubbleColor = rightBubbleColor;
        return this;
    }

    public QiscusChatConfig setLeftBubbleColor(@ColorRes int leftBubbleColor) {
        this.leftBubbleColor = leftBubbleColor;
        return this;
    }

    public QiscusChatConfig setRightBubbleTextColor(@ColorRes int rightBubbleTextColor) {
        this.rightBubbleTextColor = rightBubbleTextColor;
        return this;
    }

    public QiscusChatConfig setLeftBubbleTextColor(@ColorRes int leftBubbleTextColor) {
        this.leftBubbleTextColor = leftBubbleTextColor;
        return this;
    }

    public QiscusChatConfig setRightBubbleTimeColor(@ColorRes int rightBubbleTimeColor) {
        this.rightBubbleTimeColor = rightBubbleTimeColor;
        return this;
    }

    public QiscusChatConfig setLeftBubbleTimeColor(@ColorRes int leftBubbleTimeColor) {
        this.leftBubbleTimeColor = leftBubbleTimeColor;
        return this;
    }

    public QiscusChatConfig setFailedToSendMessageColor(@ColorRes int failedToSendMessageColor) {
        this.failedToSendMessageColor = failedToSendMessageColor;
        return this;
    }

    public QiscusChatConfig setDateColor(@ColorRes int dateColor) {
        this.dateColor = dateColor;
        return this;
    }

    public QiscusChatConfig setDateFormat(DateFormatter dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    public QiscusChatConfig setTimeFormat(DateFormatter timeFormat) {
        this.timeFormat = timeFormat;
        return this;
    }

    public QiscusChatConfig setEmptyRoomTitle(String emptyRoomTitle) {
        this.emptyRoomTitle = emptyRoomTitle;
        return this;
    }

    public QiscusChatConfig setEmptyRoomSubtitle(String emptyRoomSubtitle) {
        this.emptyRoomSubtitle = emptyRoomSubtitle;
        return this;
    }

    public QiscusChatConfig setEmptyRoomImageResource(@DrawableRes int emptyRoomImageResource) {
        this.emptyRoomImageResource = emptyRoomImageResource;
        return this;
    }

    public QiscusChatConfig setMessageFieldHint(String messageFieldHint) {
        this.messageFieldHint = messageFieldHint;
        return this;
    }

    public QiscusChatConfig setAddPictureIcon(@DrawableRes int addPictureIcon) {
        this.addPictureIcon = addPictureIcon;
        return this;
    }

    public QiscusChatConfig setTakePictureIcon(@DrawableRes int takePictureIcon) {
        this.takePictureIcon = takePictureIcon;
        return this;
    }

    public QiscusChatConfig setAddFileIcon(@DrawableRes int addFileIcon) {
        this.addFileIcon = addFileIcon;
        return this;
    }

    public QiscusChatConfig setSendActiveIcon(@DrawableRes int sendActiveIcon) {
        this.sendActiveIcon = sendActiveIcon;
        return this;
    }

    public QiscusChatConfig setSendInactiveIcon(@DrawableRes int sendInactiveIcon) {
        this.sendInactiveIcon = sendInactiveIcon;
        return this;
    }

    public QiscusChatConfig setSwipeRefreshColorScheme(@ColorRes int... colorResIds) {
        swipeRefreshColorScheme = colorResIds;
        return this;
    }

    public QiscusChatConfig setNotificationSmallIcon(@DrawableRes int notificationSmallIcon) {
        this.notificationSmallIcon = notificationSmallIcon;
        return this;
    }

    public QiscusChatConfig setNotificationBigIcon(@DrawableRes int notificationBigIcon) {
        this.notificationBigIcon = notificationBigIcon;
        return this;
    }

    public QiscusChatConfig setNotificationTitleHandler(NotificationTitleHandler notificationTitleHandler) {
        this.notificationTitleHandler = notificationTitleHandler;
        return this;
    }

    public QiscusChatConfig setNotificationClickListener(NotificationClickListener notificationClickListener) {
        this.notificationClickListener = notificationClickListener;
        return this;
    }

    @ColorRes
    public int getStatusBarColor() {
        return statusBarColor;
    }

    @ColorRes
    public int getAppBarColor() {
        return appBarColor;
    }

    @ColorRes
    public int getTitleColor() {
        return titleColor;
    }

    @ColorRes
    public int getSubtitleColor() {
        return subtitleColor;
    }

    @ColorRes
    public int getRightBubbleColor() {
        return rightBubbleColor;
    }

    @ColorRes
    public int getLeftBubbleColor() {
        return leftBubbleColor;
    }

    @ColorRes
    public int getRightBubbleTextColor() {
        return rightBubbleTextColor;
    }

    @ColorRes
    public int getLeftBubbleTextColor() {
        return leftBubbleTextColor;
    }

    @ColorRes
    public int getRightBubbleTimeColor() {
        return rightBubbleTimeColor;
    }

    @ColorRes
    public int getLeftBubbleTimeColor() {
        return leftBubbleTimeColor;
    }

    @ColorRes
    public int getFailedToSendMessageColor() {
        return failedToSendMessageColor;
    }

    @ColorRes
    public int getDateColor() {
        return dateColor;
    }

    public DateFormatter getDateFormat() {
        return dateFormat;
    }

    public DateFormatter getTimeFormat() {
        return timeFormat;
    }

    public String getEmptyRoomTitle() {
        return emptyRoomTitle;
    }

    public String getEmptyRoomSubtitle() {
        return emptyRoomSubtitle;
    }

    @DrawableRes
    public int getEmptyRoomImageResource() {
        return emptyRoomImageResource;
    }

    public String getMessageFieldHint() {
        return messageFieldHint;
    }

    @DrawableRes
    public int getAddPictureIcon() {
        return addPictureIcon;
    }

    @DrawableRes
    public int getTakePictureIcon() {
        return takePictureIcon;
    }

    @DrawableRes
    public int getAddFileIcon() {
        return addFileIcon;
    }

    @DrawableRes
    public int getSendActiveIcon() {
        return sendActiveIcon;
    }

    @DrawableRes
    public int getSendInactiveIcon() {
        return sendInactiveIcon;
    }

    @ColorRes
    public int[] getSwipeRefreshColorScheme() {
        return swipeRefreshColorScheme;
    }

    @DrawableRes
    public int getNotificationSmallIcon() {
        return notificationSmallIcon;
    }

    @DrawableRes
    public int getNotificationBigIcon() {
        return notificationBigIcon;
    }

    public NotificationTitleHandler getNotificationTitleHandler() {
        return notificationTitleHandler;
    }

    public NotificationClickListener getNotificationClickListener() {
        return notificationClickListener;
    }
}

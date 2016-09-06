package com.qiscus.sdk.data.model;

import android.support.annotation.ColorRes;

import com.qiscus.library.chat.R;
import com.qiscus.sdk.util.QiscusDateUtil;

/**
 * Created by zetra. on 9/5/16.
 */
public class QiscusChatConfig {
    private int statusBarColor = R.color.qiscus_primary_dark;
    private int appBarColor = R.color.qiscus_primary;
    private int titleColor = R.color.qiscus_white;
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
}

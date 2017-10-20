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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.ui.QiscusChatActivity;
import com.qiscus.sdk.ui.QiscusGroupChatActivity;
import com.qiscus.sdk.util.QiscusDateUtil;
import com.qiscus.sdk.util.QiscusErrorLogger;
import com.qiscus.sdk.util.QiscusTextUtil;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : June 15, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusChatConfig {
    private int statusBarColor = R.color.qiscus_primary_dark;
    private int appBarColor = R.color.qiscus_primary;
    private int accentColor = R.color.qiscus_accent;
    private int inlineReplyColor = R.color.qiscus_primary;
    private int titleColor = R.color.qiscus_white;
    private int subtitleColor = R.color.qiscus_dark_white;
    private int rightBubbleColor = R.color.qiscus_primary_light;
    private int leftBubbleColor = R.color.qiscus_left_bubble;
    private int systemMessageBubbleColor = R.color.qiscus_accent_light;
    private int rightBubbleTextColor = R.color.qiscus_primary_text;
    private int leftBubbleTextColor = R.color.qiscus_primary_text;
    private int rightBubbleTimeColor = R.color.qiscus_secondary_text;
    private int leftBubbleTimeColor = R.color.qiscus_secondary_text;
    private int rightLinkTextColor = R.color.qiscus_primary_text;
    private int leftLinkTextColor = R.color.qiscus_primary_text;
    private int systemMessageTextColor = R.color.qiscus_primary_text;
    private int failedToSendMessageColor = R.color.qiscus_red;
    private int readIconColor = R.color.qiscus_primary;
    private int dateColor = R.color.qiscus_secondary_text;
    private int senderNameColor = R.color.qiscus_secondary_text;
    private int selectedBubbleBackgroundColor = R.color.qiscus_primary;

    private int rightProgressFinishedColor = R.color.qiscus_primary_light;
    private int leftProgressFinishedColor = R.color.qiscus_primary;

    private int accountLinkingTextColor = R.color.qiscus_primary;
    private int accountLinkingBackground = R.color.qiscus_account_linking_background;
    private String accountLinkingText = QiscusTextUtil.getString(R.string.qiscus_account_linking_text);

    private int buttonBubbleTextColor = R.color.qiscus_primary;
    private int buttonBubbleBackBackground = R.color.qiscus_account_linking_background;

    private DateFormatter dateFormat = QiscusDateUtil::toTodayOrDate;
    private DateFormatter timeFormat = QiscusDateUtil::toHour;

    private String emptyRoomTitle = QiscusTextUtil.getString(R.string.qiscus_welcome);
    private String emptyRoomSubtitle = QiscusTextUtil.getString(R.string.qiscus_desc_empty_chat);
    private int emptyRoomImageResource = R.drawable.ic_qiscus_chat_empty;
    private String messageFieldHint = QiscusTextUtil.getString(R.string.qiscus_hint_message);

    private int addPictureIcon = R.drawable.ic_qiscus_add_image;
    private int addPictureBackgroundColor = R.color.qiscus_gallery_background;
    private String addPictureText = QiscusTextUtil.getString(R.string.qiscus_gallery);
    private boolean enableAddPicture = true;

    private int takePictureIcon = R.drawable.ic_qiscus_pick_picture;
    private int takePictureBackgroundColor = R.color.qiscus_camera_background;
    private String takePictureText = QiscusTextUtil.getString(R.string.qiscus_camera);
    private boolean enableTakePicture = true;

    private int addFileIcon = R.drawable.ic_qiscus_add_file;
    private int addFileBackgroundColor = R.color.qiscus_file_background;
    private String addFileText = QiscusTextUtil.getString(R.string.qiscus_file);
    private boolean enableAddFile = true;

    private int recordAudioIcon = R.drawable.ic_qiscus_add_audio;
    private int recordBackgroundColor = R.color.qiscus_record_background;
    private String recordText = QiscusTextUtil.getString(R.string.qiscus_record);
    private boolean enableRecordAudio = true;

    private int addContactIcon = R.drawable.ic_qiscus_add_contact;
    private int addContactBackgroundColor = R.color.qiscus_contact_background;
    private String addContactText = QiscusTextUtil.getString(R.string.qiscus_contact);
    private boolean enableAddContact = true;

    private int addLocationIcon = R.drawable.ic_qiscus_location;
    private int addLocationBackgroundColor = R.color.qiscus_location_background;
    private String addLocationText = QiscusTextUtil.getString(R.string.qiscus_location);
    private boolean enableAddLocation = false;

    private int stopRecordIcon = R.drawable.ic_qiscus_send_on;
    private int cancelRecordIcon = R.drawable.ic_qiscus_cancel_record;

    private int sendButtonIcon = R.drawable.ic_qiscus_send;
    private int showAttachmentPanelIcon = R.drawable.ic_qiscus_attach;
    private int hideAttachmentPanelIcon = R.drawable.ic_qiscus_back_to_keyboard;
    private int hideAttachmentPanelBackgroundColor = R.color.qiscus_keyboard_background;

    private int replyBarColor = R.color.qiscus_primary;
    private int replySenderColor = R.color.qiscus_primary;
    private int replyMessageColor = R.color.qiscus_secondary_text;

    private int cardTitleColor = R.color.qiscus_primary_text;
    private int cardDescriptionColor = R.color.qiscus_secondary_text;
    private int cardButtonTextColor = R.color.qiscus_primary;
    private int cardButtonBackground = R.color.qiscus_white;

    private int playAudioIcon = R.drawable.ic_qiscus_play_audio;
    private int pauseAudioIcon = R.drawable.ic_qiscus_pause_audio;
    private int showEmojiIcon = R.drawable.ic_qiscus_emot;
    private int showKeyboardIcon = R.drawable.ic_qiscus_keyboard;

    private int[] swipeRefreshColorScheme = new int[]{R.color.qiscus_primary, R.color.qiscus_accent};

    private int notificationSmallIcon = R.drawable.ic_qiscus_notif_app;
    private int notificationBigIcon = R.drawable.ic_qiscus_notif_app;
    private boolean enableAvatarAsNotificationIcon = true;
    private boolean enableReplyNotification = false;
    private QiscusNotificationBuilderInterceptor notificationBuilderInterceptor;

    private QiscusImageCompressionConfig qiscusImageCompressionConfig = new QiscusImageCompressionConfig();

    private NotificationTitleHandler notificationTitleHandler = QiscusComment::getRoomName;
    private QiscusRoomSenderNameInterceptor qiscusRoomSenderNameInterceptor = QiscusComment::getSender;

    private NotificationClickListener notificationClickListener =
            (context, qiscusComment) -> QiscusApi.getInstance()
                    .getChatRoom(qiscusComment.getRoomId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(qiscusChatRoom -> {
                        if (qiscusChatRoom.isGroup()) {
                            return QiscusGroupChatActivity.generateIntent(context, qiscusChatRoom);
                        }
                        return QiscusChatActivity.generateIntent(context, qiscusChatRoom);
                    })
                    .subscribe(intent -> {
                        context.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }, throwable -> {
                        QiscusErrorLogger.print("NotificationClick", throwable);
                        Toast.makeText(context, QiscusErrorLogger.getMessage(throwable), Toast.LENGTH_SHORT).show();
                    });

    private ReplyNotificationHandler replyNotificationHandler =
            (context, qiscusComment) -> QiscusApi.getInstance().postComment(qiscusComment)
                    .doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
                    .doOnNext(comment -> {
                        comment.setState(QiscusComment.STATE_ON_QISCUS);
                        QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(comment.getId(), comment.getUniqueId());
                        if (savedQiscusComment != null && savedQiscusComment.getState() > comment.getState()) {
                            comment.setState(savedQiscusComment.getState());
                        }
                        Qiscus.getDataStore().addOrUpdate(comment);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(commentSend -> {
                        QiscusCacheManager.getInstance().clearMessageNotifItems(qiscusComment.getRoomId());
                    }, throwable -> {
                        QiscusErrorLogger.print("ReplyNotification", throwable);
                        Toast.makeText(context, QiscusErrorLogger.getMessage(throwable), Toast.LENGTH_SHORT).show();
                    });

    private boolean enablePushNotification = true;
    private boolean onlyEnablePushNotificationOutsideChatRoom = false;
    private boolean enableFcmPushNotification = false;
    private boolean enablePermission = true;

    private Drawable chatRoomBackground = new ColorDrawable(ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_dark_white));

    private ForwardCommentHandler forwardCommentHandler;
    private boolean enableForwardComment = false;

    private QiscusMentionConfig mentionConfig = new QiscusMentionConfig();

    public QiscusChatConfig setStatusBarColor(@ColorRes int statusBarColor) {
        this.statusBarColor = statusBarColor;
        return this;
    }

    public QiscusChatConfig setAppBarColor(@ColorRes int appBarColor) {
        this.appBarColor = appBarColor;
        return this;
    }

    public QiscusChatConfig setAccentColor(@ColorRes int accentColor) {
        this.accentColor = accentColor;
        return this;
    }

    public QiscusChatConfig setInlineReplyColor(@ColorRes int inlineReplyColor) {
        this.inlineReplyColor = inlineReplyColor;
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

    public QiscusChatConfig setSystemMessageBubbleColor(@ColorRes int systemMessageBubbleColor) {
        this.systemMessageBubbleColor = systemMessageBubbleColor;
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

    public QiscusChatConfig setRightLinkTextColor(@ColorRes int rightLinkTextColor) {
        this.rightLinkTextColor = rightLinkTextColor;
        return this;
    }

    public QiscusChatConfig setLeftLinkTextColor(@ColorRes int leftLinkTextColor) {
        this.leftLinkTextColor = leftLinkTextColor;
        return this;
    }

    public QiscusChatConfig setSystemMessageTextColor(@ColorRes int systemMessageTextColor) {
        this.systemMessageTextColor = systemMessageTextColor;
        return this;
    }

    public QiscusChatConfig setFailedToSendMessageColor(@ColorRes int failedToSendMessageColor) {
        this.failedToSendMessageColor = failedToSendMessageColor;
        return this;
    }

    public QiscusChatConfig setReadIconColor(@ColorRes int readIconColor) {
        this.readIconColor = readIconColor;
        return this;
    }

    public QiscusChatConfig setDateColor(@ColorRes int dateColor) {
        this.dateColor = dateColor;
        return this;
    }

    public QiscusChatConfig setSenderNameColor(@ColorRes int senderNameColor) {
        this.senderNameColor = senderNameColor;
        return this;
    }

    public QiscusChatConfig setSelectedBubbleBackgroundColor(@ColorRes int selectedBubbleBackgroundColor) {
        this.selectedBubbleBackgroundColor = selectedBubbleBackgroundColor;
        return this;
    }

    public QiscusChatConfig setRightProgressFinishedColor(@ColorRes int rightProgressFinishedColor) {
        this.rightProgressFinishedColor = rightProgressFinishedColor;
        return this;
    }

    public QiscusChatConfig setLeftProgressFinishedColor(@ColorRes int leftProgressFinishedColor) {
        this.leftProgressFinishedColor = leftProgressFinishedColor;
        return this;
    }

    public QiscusChatConfig setAccountLinkingTextColor(@ColorRes int accountLinkingTextColor) {
        this.accountLinkingTextColor = accountLinkingTextColor;
        return this;
    }

    public QiscusChatConfig setAccountLinkingBackground(@ColorRes int accountLinkingBackground) {
        this.accountLinkingBackground = accountLinkingBackground;
        return this;
    }

    public QiscusChatConfig setAccountLinkingText(String accountLinkingText) {
        this.accountLinkingText = accountLinkingText;
        return this;
    }

    public QiscusChatConfig setButtonBubbleTextColor(@ColorRes int buttonBubbleTextColor) {
        this.buttonBubbleTextColor = buttonBubbleTextColor;
        return this;
    }

    public QiscusChatConfig setButtonBubbleBackBackground(@ColorRes int buttonBubbleBackBackground) {
        this.buttonBubbleBackBackground = buttonBubbleBackBackground;
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

    public QiscusChatConfig setAddPictureBackgroundColor(@ColorRes int addPictureBackgroundColor) {
        this.addPictureBackgroundColor = addPictureBackgroundColor;
        return this;
    }

    public QiscusChatConfig setAddPictureText(String addPictureText) {
        this.addPictureText = addPictureText;
        return this;
    }

    public QiscusChatConfig setEnableAddPicture(boolean enableAddPicture) {
        this.enableAddPicture = enableAddPicture;
        return this;
    }

    public QiscusChatConfig setTakePictureIcon(@DrawableRes int takePictureIcon) {
        this.takePictureIcon = takePictureIcon;
        return this;
    }

    public QiscusChatConfig setTakePictureBackgroundColor(@ColorRes int takePictureBackgroundColor) {
        this.takePictureBackgroundColor = takePictureBackgroundColor;
        return this;
    }

    public QiscusChatConfig setTakePictureText(String takePictureText) {
        this.takePictureText = takePictureText;
        return this;
    }

    public QiscusChatConfig setEnableTakePicture(boolean enableTakePicture) {
        this.enableTakePicture = enableTakePicture;
        return this;
    }

    public QiscusChatConfig setAddFileIcon(@DrawableRes int addFileIcon) {
        this.addFileIcon = addFileIcon;
        return this;
    }

    public QiscusChatConfig setAddFileBackgroundColor(@ColorRes int addFileBackgroundColor) {
        this.addFileBackgroundColor = addFileBackgroundColor;
        return this;
    }

    public QiscusChatConfig setAddFileText(String addFileText) {
        this.addFileText = addFileText;
        return this;
    }

    public QiscusChatConfig setEnableAddFile(boolean enableAddFile) {
        this.enableAddFile = enableAddFile;
        return this;
    }

    public QiscusChatConfig setRecordAudioIcon(@DrawableRes int recordAudioIcon) {
        this.recordAudioIcon = recordAudioIcon;
        return this;
    }

    public QiscusChatConfig setRecordBackgroundColor(@ColorRes int recordBackgroundColor) {
        this.recordBackgroundColor = recordBackgroundColor;
        return this;
    }

    public QiscusChatConfig setRecordText(String recordText) {
        this.recordText = recordText;
        return this;
    }

    public QiscusChatConfig setEnableRecordAudio(boolean enableRecordAudio) {
        this.enableRecordAudio = enableRecordAudio;
        return this;
    }

    public QiscusChatConfig setAddContactIcon(@DrawableRes int addContactIcon) {
        this.addContactIcon = addContactIcon;
        return this;
    }

    public QiscusChatConfig setAddContactBackgroundColor(@ColorRes int addContactBackgroundColor) {
        this.addContactBackgroundColor = addContactBackgroundColor;
        return this;
    }

    public QiscusChatConfig setAddContactText(String addContactText) {
        this.addContactText = addContactText;
        return this;
    }

    public QiscusChatConfig setEnableAddContact(boolean enableAddContact) {
        this.enableAddContact = enableAddContact;
        return this;
    }

    public QiscusChatConfig setAddLocationIcon(@DrawableRes int addLocationIcon) {
        this.addLocationIcon = addLocationIcon;
        return this;
    }

    public QiscusChatConfig setAddLocationBackgroundColor(@ColorRes int addLocationBackgroundColor) {
        this.addLocationBackgroundColor = addLocationBackgroundColor;
        return this;
    }

    public QiscusChatConfig setAddLocationText(String addLocationText) {
        this.addLocationText = addLocationText;
        return this;
    }

    public QiscusChatConfig setEnableAddLocation(boolean enableAddLocation) {
        this.enableAddLocation = enableAddLocation;
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public QiscusChatConfig setEnableReplyNotification(boolean enableReplyNotification) {
        this.enableReplyNotification = enableReplyNotification;
        return this;
    }

    public QiscusChatConfig setStopRecordIcon(@DrawableRes int stopRecordIcon) {
        this.stopRecordIcon = stopRecordIcon;
        return this;
    }

    public QiscusChatConfig setCancelRecordIcon(@DrawableRes int cancelRecordIcon) {
        this.cancelRecordIcon = cancelRecordIcon;
        return this;
    }

    public QiscusChatConfig setSendButtonIcon(@DrawableRes int sendButtonIcon) {
        this.sendButtonIcon = sendButtonIcon;
        return this;
    }

    public QiscusChatConfig setShowAttachmentPanelIcon(@DrawableRes int showAttachmentPanelIcon) {
        this.showAttachmentPanelIcon = showAttachmentPanelIcon;
        return this;
    }

    public QiscusChatConfig setHideAttachmentPanelIcon(@DrawableRes int hideAttachmentPanelIcon) {
        this.hideAttachmentPanelIcon = hideAttachmentPanelIcon;
        return this;
    }

    public QiscusChatConfig setHideAttachmentPanelBackgroundColor(@ColorRes int hideAttachmentPanelBackgroundColor) {
        this.hideAttachmentPanelBackgroundColor = hideAttachmentPanelBackgroundColor;
        return this;
    }

    public QiscusChatConfig setPlayAudioIcon(@DrawableRes int playAudioIcon) {
        this.playAudioIcon = playAudioIcon;
        return this;
    }

    public QiscusChatConfig setPauseAudioIcon(@DrawableRes int pauseAudioIcon) {
        this.pauseAudioIcon = pauseAudioIcon;
        return this;
    }

    public QiscusChatConfig setShowEmojiIcon(@DrawableRes int showEmojiIcon) {
        this.showEmojiIcon = showEmojiIcon;
        return this;
    }

    public QiscusChatConfig setShowKeyboardIcon(@DrawableRes int showKeyboardIcon) {
        this.showKeyboardIcon = showKeyboardIcon;
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

    public QiscusChatConfig setEnableAvatarAsNotificationIcon(boolean enableAvatarAsNotificationIcon) {
        this.enableAvatarAsNotificationIcon = enableAvatarAsNotificationIcon;
        return this;
    }

    public QiscusChatConfig setNotificationBuilderInterceptor(QiscusNotificationBuilderInterceptor notificationBuilderInterceptor) {
        this.notificationBuilderInterceptor = notificationBuilderInterceptor;
        return this;
    }

    public QiscusChatConfig setReplyNotificationHandler(ReplyNotificationHandler replyNotificationHandler) {
        this.replyNotificationHandler = replyNotificationHandler;
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

    public QiscusChatConfig setEnablePushNotification(boolean enablePushNotification) {
        this.enablePushNotification = enablePushNotification;
        return this;
    }

    public QiscusChatConfig setOnlyEnablePushNotificationOutsideChatRoom(boolean onlyEnablePushNotificationOutsideChatRoom) {
        this.onlyEnablePushNotificationOutsideChatRoom = onlyEnablePushNotificationOutsideChatRoom;
        return this;
    }

    public QiscusChatConfig setEnableFcmPushNotification(boolean enableFcmPushNotification) {
        this.enableFcmPushNotification = enableFcmPushNotification;
        return this;
    }

    public QiscusChatConfig setChatRoomBackground(Drawable chatRoomBackground) {
        this.chatRoomBackground = chatRoomBackground;
        return this;
    }

    public QiscusChatConfig setQiscusImageCompressionConfig(QiscusImageCompressionConfig qiscusImageCompressionConfig) {
        this.qiscusImageCompressionConfig = qiscusImageCompressionConfig;
        return this;
    }

    public QiscusChatConfig setReplyBarColor(@ColorRes int replyBarColor) {
        this.replyBarColor = replyBarColor;
        return this;
    }

    public QiscusChatConfig setReplySenderColor(@ColorRes int replySenderColor) {
        this.replySenderColor = replySenderColor;
        return this;
    }

    public QiscusChatConfig setReplyMessageColor(@ColorRes int replyMessageColor) {
        this.replyMessageColor = replyMessageColor;
        return this;
    }

    public QiscusChatConfig setCardTitleColor(@ColorRes int cardTitleColor) {
        this.cardTitleColor = cardTitleColor;
        return this;
    }

    public QiscusChatConfig setCardDescriptionColor(@ColorRes int cardDescriptionColor) {
        this.cardDescriptionColor = cardDescriptionColor;
        return this;
    }

    public QiscusChatConfig setCardButtonTextColor(@ColorRes int cardButtonTextColor) {
        this.cardButtonTextColor = cardButtonTextColor;
        return this;
    }

    public QiscusChatConfig setCardButtonBackground(@ColorRes int cardButtonBackground) {
        this.cardButtonBackground = cardButtonBackground;
        return this;
    }

    public QiscusChatConfig setForwardCommentHandler(ForwardCommentHandler forwardCommentHandler) {
        this.forwardCommentHandler = forwardCommentHandler;
        return this;
    }

    public QiscusChatConfig setEnableForwardComment(boolean enableForwardComment) {
        this.enableForwardComment = enableForwardComment;
        return this;
    }

    public QiscusChatConfig setMentionConfig(QiscusMentionConfig mentionConfig) {
        this.mentionConfig = mentionConfig;
        return this;
    }

    public QiscusChatConfig setRoomSenderNameInterceptor(QiscusRoomSenderNameInterceptor
                                                                 qiscusRoomSenderNameInterceptor) {
        this.qiscusRoomSenderNameInterceptor = qiscusRoomSenderNameInterceptor;
        return this;
    }

    public QiscusChatConfig setEnablePermission(boolean enablePermission) {
        this.enablePermission = enablePermission;
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
    public int getAccentColor() {
        return accentColor;
    }

    @ColorRes
    public int getInlineReplyColor() {
        return inlineReplyColor;
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
    public int getSystemMessageBubbleColor() {
        return systemMessageBubbleColor;
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
    public int getRightLinkTextColor() {
        return rightLinkTextColor;
    }

    @ColorRes
    public int getLeftLinkTextColor() {
        return leftLinkTextColor;
    }

    @ColorRes
    public int getSystemMessageTextColor() {
        return systemMessageTextColor;
    }

    @ColorRes
    public int getFailedToSendMessageColor() {
        return failedToSendMessageColor;
    }

    @ColorRes
    public int getReadIconColor() {
        return readIconColor;
    }

    @ColorRes
    public int getDateColor() {
        return dateColor;
    }

    @ColorRes
    public int getSenderNameColor() {
        return senderNameColor;
    }

    @ColorRes
    public int getSelectedBubbleBackgroundColor() {
        return selectedBubbleBackgroundColor;
    }

    @ColorRes
    public int getRightProgressFinishedColor() {
        return rightProgressFinishedColor;
    }

    @ColorRes
    public int getLeftProgressFinishedColor() {
        return leftProgressFinishedColor;
    }

    @ColorRes
    public int getAccountLinkingTextColor() {
        return accountLinkingTextColor;
    }

    @ColorRes
    public int getAccountLinkingBackground() {
        return accountLinkingBackground;
    }

    public String getAccountLinkingText() {
        return accountLinkingText;
    }

    @ColorRes
    public int getButtonBubbleTextColor() {
        return buttonBubbleTextColor;
    }

    @ColorRes
    public int getButtonBubbleBackBackground() {
        return buttonBubbleBackBackground;
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

    @ColorRes
    public int getAddPictureBackgroundColor() {
        return addPictureBackgroundColor;
    }

    public String getAddPictureText() {
        return addPictureText;
    }

    public boolean isEnableAddPicture() {
        return enableAddPicture;
    }

    @DrawableRes
    public int getTakePictureIcon() {
        return takePictureIcon;
    }

    @ColorRes
    public int getTakePictureBackgroundColor() {
        return takePictureBackgroundColor;
    }

    public String getTakePictureText() {
        return takePictureText;
    }

    public boolean isEnableTakePicture() {
        return enableTakePicture;
    }

    @DrawableRes
    public int getAddFileIcon() {
        return addFileIcon;
    }

    @ColorRes
    public int getAddFileBackgroundColor() {
        return addFileBackgroundColor;
    }

    public String getAddFileText() {
        return addFileText;
    }

    public boolean isEnableAddFile() {
        return enableAddFile;
    }

    @DrawableRes
    public int getRecordAudioIcon() {
        return recordAudioIcon;
    }

    @ColorRes
    public int getRecordBackgroundColor() {
        return recordBackgroundColor;
    }

    public String getRecordText() {
        return recordText;
    }

    public boolean isEnableRecordAudio() {
        return enableRecordAudio;
    }

    @DrawableRes
    public int getStopRecordIcon() {
        return stopRecordIcon;
    }

    @DrawableRes
    public int getCancelRecordIcon() {
        return cancelRecordIcon;
    }

    @DrawableRes
    public int getAddContactIcon() {
        return addContactIcon;
    }

    @ColorRes
    public int getAddContactBackgroundColor() {
        return addContactBackgroundColor;
    }

    public String getAddContactText() {
        return addContactText;
    }

    public boolean isEnableAddContact() {
        return enableAddContact;
    }

    @DrawableRes
    public int getAddLocationIcon() {
        return addLocationIcon;
    }

    @ColorRes
    public int getAddLocationBackgroundColor() {
        return addLocationBackgroundColor;
    }

    public String getAddLocationText() {
        return addLocationText;
    }

    public boolean isEnableAddLocation() {
        return enableAddLocation;
    }

    @DrawableRes
    public int getSendButtonIcon() {
        return sendButtonIcon;
    }

    @DrawableRes
    public int getShowAttachmentPanelIcon() {
        return showAttachmentPanelIcon;
    }

    @DrawableRes
    public int getHideAttachmentPanelIcon() {
        return hideAttachmentPanelIcon;
    }

    @ColorRes
    public int getHideAttachmentPanelBackgroundColor() {
        return hideAttachmentPanelBackgroundColor;
    }

    @DrawableRes
    public int getPlayAudioIcon() {
        return playAudioIcon;
    }

    @DrawableRes
    public int getPauseAudioIcon() {
        return pauseAudioIcon;
    }

    @DrawableRes
    public int getShowEmojiIcon() {
        return showEmojiIcon;
    }

    @DrawableRes
    public int getShowKeyboardIcon() {
        return showKeyboardIcon;
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

    public boolean isEnableAvatarAsNotificationIcon() {
        return enableAvatarAsNotificationIcon;
    }

    public boolean isEnableReplyNotification() {
        return enableReplyNotification;
    }

    public QiscusNotificationBuilderInterceptor getNotificationBuilderInterceptor() {
        return notificationBuilderInterceptor;
    }

    public NotificationTitleHandler getNotificationTitleHandler() {
        return notificationTitleHandler;
    }

    public NotificationClickListener getNotificationClickListener() {
        return notificationClickListener;
    }

    public ReplyNotificationHandler getReplyNotificationHandler() {
        return replyNotificationHandler;
    }

    public boolean isEnablePushNotification() {
        return enablePushNotification;
    }

    public boolean isOnlyEnablePushNotificationOutsideChatRoom() {
        return onlyEnablePushNotificationOutsideChatRoom;
    }

    public boolean isEnableFcmPushNotification() {
        return enableFcmPushNotification;
    }

    public Drawable getChatRoomBackground() {
        return chatRoomBackground;
    }

    public QiscusImageCompressionConfig getQiscusImageCompressionConfig() {
        return qiscusImageCompressionConfig;
    }

    public boolean isEnablePermission() {
        return enablePermission;
    }

    @ColorRes
    public int getReplyBarColor() {
        return replyBarColor;
    }

    @ColorRes
    public int getReplySenderColor() {
        return replySenderColor;
    }

    @ColorRes
    public int getReplyMessageColor() {
        return replyMessageColor;
    }

    @ColorRes
    public int getCardTitleColor() {
        return cardTitleColor;
    }

    @ColorRes
    public int getCardDescriptionColor() {
        return cardDescriptionColor;
    }

    @ColorRes
    public int getCardButtonTextColor() {
        return cardButtonTextColor;
    }

    @ColorRes
    public int getCardButtonBackground() {
        return cardButtonBackground;
    }

    public ForwardCommentHandler getForwardCommentHandler() {
        return forwardCommentHandler;
    }

    public boolean isEnableForwardComment() {
        return enableForwardComment;
    }

    public QiscusMentionConfig getMentionConfig() {
        return mentionConfig;
    }

    public QiscusRoomSenderNameInterceptor getRoomSenderNameInterceptor() {
        return qiscusRoomSenderNameInterceptor;
    }
}

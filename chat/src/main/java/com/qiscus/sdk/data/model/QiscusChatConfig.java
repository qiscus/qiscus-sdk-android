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
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.data.model.CommentInfoHandler;
import com.qiscus.sdk.chat.core.data.model.DateFormatter;
import com.qiscus.sdk.chat.core.data.model.ForwardCommentHandler;
import com.qiscus.sdk.chat.core.data.model.NotificationClickListener;
import com.qiscus.sdk.chat.core.data.model.NotificationTitleHandler;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusCommentSendingInterceptor;
import com.qiscus.sdk.chat.core.data.model.QiscusImageCompressionConfig;
import com.qiscus.sdk.chat.core.data.model.ReplyNotificationHandler;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.util.QiscusDateUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;
import com.qiscus.sdk.ui.QiscusChatActivity;
import com.qiscus.sdk.ui.QiscusGroupChatActivity;

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
    private int emptyRoomTitleColor = R.color.qiscus_divider;

    private String emptyRoomSubtitle = QiscusTextUtil.getString(R.string.qiscus_desc_empty_chat);
    private int emptyRoomSubtitleColor = R.color.qiscus_divider;

    private int emptyRoomImageResource = R.drawable.ic_qiscus_chat_empty;

    private String messageFieldHint = QiscusTextUtil.getString(R.string.qiscus_hint_message);
    private int messageFieldHintColor = R.color.qiscus_secondary_text;
    private int messageFieldTextColor = R.color.qiscus_primary_text;

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

    private int dialogPermissionPositiveButtonTextColor = R.color.qiscus_primary;
    private int dialogPermissionNegativeButtonTextColor = R.color.qiscus_secondary_text;

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

    private NotificationTitleHandler notificationTitleHandler = QiscusComment::getRoomName;
    private QiscusRoomSenderNameInterceptor qiscusRoomSenderNameInterceptor = QiscusComment::getSender;
    private QiscusRoomSenderNameColorInterceptor qiscusRoomSenderNameColorInterceptor = qiscusComment -> R.color.qiscus_secondary_text;
    private QiscusRoomReplyBarColorInterceptor qiscusRoomReplyBarColorInterceptor = qiscusComment -> getReplyBarColor();
    private QiscusStartReplyInterceptor startReplyInterceptor = qiscusComment -> new QiscusReplyPanelConfig();

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
            (context, qiscusComment) -> QiscusApi.getInstance().sendMessage(qiscusComment)
                    .doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
                    .doOnNext(comment -> {
                        comment.setState(QiscusComment.STATE_ON_QISCUS);
                        QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(comment.getUniqueId());
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
    private boolean enableRequestPermission = true;
    private boolean enableCaption = true;

    private Drawable chatRoomBackground = new ColorDrawable(ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_dark_white));

    private ForwardCommentHandler forwardCommentHandler;
    private boolean enableForwardComment = false;

    private CommentInfoHandler commentInfoHandler;
    private boolean enableCommentInfo = false;

    private boolean enableShareMedia = true;

    private QiscusMentionConfig mentionConfig = new QiscusMentionConfig();

    private QiscusDeleteCommentConfig deleteCommentConfig = new QiscusDeleteCommentConfig();

    @ColorRes
    public int getStatusBarColor() {
        return statusBarColor;
    }

    public QiscusChatConfig setStatusBarColor(@ColorRes int statusBarColor) {
        this.statusBarColor = statusBarColor;
        return this;
    }

    @ColorRes
    public int getAppBarColor() {
        return appBarColor;
    }

    public QiscusChatConfig setAppBarColor(@ColorRes int appBarColor) {
        this.appBarColor = appBarColor;
        return this;
    }

    @ColorRes
    public int getAccentColor() {
        return accentColor;
    }

    public QiscusChatConfig setAccentColor(@ColorRes int accentColor) {
        this.accentColor = accentColor;
        return this;
    }

    @ColorRes
    public int getInlineReplyColor() {
        return inlineReplyColor;
    }

    public QiscusChatConfig setInlineReplyColor(@ColorRes int inlineReplyColor) {
        this.inlineReplyColor = inlineReplyColor;
        return this;
    }

    @ColorRes
    public int getTitleColor() {
        return titleColor;
    }

    public QiscusChatConfig setTitleColor(@ColorRes int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    @ColorRes
    public int getSubtitleColor() {
        return subtitleColor;
    }

    public QiscusChatConfig setSubtitleColor(@ColorRes int subtitleColor) {
        this.subtitleColor = subtitleColor;
        return this;
    }

    @ColorRes
    public int getRightBubbleColor() {
        return rightBubbleColor;
    }

    public QiscusChatConfig setRightBubbleColor(@ColorRes int rightBubbleColor) {
        this.rightBubbleColor = rightBubbleColor;
        return this;
    }

    @ColorRes
    public int getLeftBubbleColor() {
        return leftBubbleColor;
    }

    public QiscusChatConfig setLeftBubbleColor(@ColorRes int leftBubbleColor) {
        this.leftBubbleColor = leftBubbleColor;
        return this;
    }

    @ColorRes
    public int getSystemMessageBubbleColor() {
        return systemMessageBubbleColor;
    }

    public QiscusChatConfig setSystemMessageBubbleColor(@ColorRes int systemMessageBubbleColor) {
        this.systemMessageBubbleColor = systemMessageBubbleColor;
        return this;
    }

    @ColorRes
    public int getRightBubbleTextColor() {
        return rightBubbleTextColor;
    }

    public QiscusChatConfig setRightBubbleTextColor(@ColorRes int rightBubbleTextColor) {
        this.rightBubbleTextColor = rightBubbleTextColor;
        return this;
    }

    @ColorRes
    public int getLeftBubbleTextColor() {
        return leftBubbleTextColor;
    }

    public QiscusChatConfig setLeftBubbleTextColor(@ColorRes int leftBubbleTextColor) {
        this.leftBubbleTextColor = leftBubbleTextColor;
        return this;
    }

    @ColorRes
    public int getRightBubbleTimeColor() {
        return rightBubbleTimeColor;
    }

    public QiscusChatConfig setRightBubbleTimeColor(@ColorRes int rightBubbleTimeColor) {
        this.rightBubbleTimeColor = rightBubbleTimeColor;
        return this;
    }

    @ColorRes
    public int getLeftBubbleTimeColor() {
        return leftBubbleTimeColor;
    }

    public QiscusChatConfig setLeftBubbleTimeColor(@ColorRes int leftBubbleTimeColor) {
        this.leftBubbleTimeColor = leftBubbleTimeColor;
        return this;
    }

    @ColorRes
    public int getRightLinkTextColor() {
        return rightLinkTextColor;
    }

    public QiscusChatConfig setRightLinkTextColor(@ColorRes int rightLinkTextColor) {
        this.rightLinkTextColor = rightLinkTextColor;
        return this;
    }

    @ColorRes
    public int getLeftLinkTextColor() {
        return leftLinkTextColor;
    }

    public QiscusChatConfig setLeftLinkTextColor(@ColorRes int leftLinkTextColor) {
        this.leftLinkTextColor = leftLinkTextColor;
        return this;
    }

    @ColorRes
    public int getSystemMessageTextColor() {
        return systemMessageTextColor;
    }

    public QiscusChatConfig setSystemMessageTextColor(@ColorRes int systemMessageTextColor) {
        this.systemMessageTextColor = systemMessageTextColor;
        return this;
    }

    @ColorRes
    public int getFailedToSendMessageColor() {
        return failedToSendMessageColor;
    }

    public QiscusChatConfig setFailedToSendMessageColor(@ColorRes int failedToSendMessageColor) {
        this.failedToSendMessageColor = failedToSendMessageColor;
        return this;
    }

    @ColorRes
    public int getReadIconColor() {
        return readIconColor;
    }

    public QiscusChatConfig setReadIconColor(@ColorRes int readIconColor) {
        this.readIconColor = readIconColor;
        return this;
    }

    @ColorRes
    public int getDateColor() {
        return dateColor;
    }

    public QiscusChatConfig setDateColor(@ColorRes int dateColor) {
        this.dateColor = dateColor;
        return this;
    }

    @ColorRes
    public int getSenderNameColor() {
        return senderNameColor;
    }

    public QiscusChatConfig setSenderNameColor(@ColorRes int senderNameColor) {
        this.senderNameColor = senderNameColor;
        return this;
    }

    @ColorRes
    public int getSelectedBubbleBackgroundColor() {
        return selectedBubbleBackgroundColor;
    }

    public QiscusChatConfig setSelectedBubbleBackgroundColor(@ColorRes int selectedBubbleBackgroundColor) {
        this.selectedBubbleBackgroundColor = selectedBubbleBackgroundColor;
        return this;
    }

    @ColorRes
    public int getRightProgressFinishedColor() {
        return rightProgressFinishedColor;
    }

    public QiscusChatConfig setRightProgressFinishedColor(@ColorRes int rightProgressFinishedColor) {
        this.rightProgressFinishedColor = rightProgressFinishedColor;
        return this;
    }

    @ColorRes
    public int getLeftProgressFinishedColor() {
        return leftProgressFinishedColor;
    }

    public QiscusChatConfig setLeftProgressFinishedColor(@ColorRes int leftProgressFinishedColor) {
        this.leftProgressFinishedColor = leftProgressFinishedColor;
        return this;
    }

    @ColorRes
    public int getAccountLinkingTextColor() {
        return accountLinkingTextColor;
    }

    public QiscusChatConfig setAccountLinkingTextColor(@ColorRes int accountLinkingTextColor) {
        this.accountLinkingTextColor = accountLinkingTextColor;
        return this;
    }

    @ColorRes
    public int getAccountLinkingBackground() {
        return accountLinkingBackground;
    }

    public QiscusChatConfig setAccountLinkingBackground(@ColorRes int accountLinkingBackground) {
        this.accountLinkingBackground = accountLinkingBackground;
        return this;
    }

    public String getAccountLinkingText() {
        return accountLinkingText;
    }

    public QiscusChatConfig setAccountLinkingText(String accountLinkingText) {
        this.accountLinkingText = accountLinkingText;
        return this;
    }

    @ColorRes
    public int getButtonBubbleTextColor() {
        return buttonBubbleTextColor;
    }

    public QiscusChatConfig setButtonBubbleTextColor(@ColorRes int buttonBubbleTextColor) {
        this.buttonBubbleTextColor = buttonBubbleTextColor;
        return this;
    }

    @ColorRes
    public int getButtonBubbleBackBackground() {
        return buttonBubbleBackBackground;
    }

    public QiscusChatConfig setButtonBubbleBackBackground(@ColorRes int buttonBubbleBackBackground) {
        this.buttonBubbleBackBackground = buttonBubbleBackBackground;
        return this;
    }

    public DateFormatter getDateFormat() {
        return dateFormat;
    }

    public QiscusChatConfig setDateFormat(DateFormatter dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    public DateFormatter getTimeFormat() {
        return timeFormat;
    }

    public QiscusChatConfig setTimeFormat(DateFormatter timeFormat) {
        this.timeFormat = timeFormat;
        return this;
    }

    public String getEmptyRoomTitle() {
        return emptyRoomTitle;
    }

    public QiscusChatConfig setEmptyRoomTitle(String emptyRoomTitle) {
        this.emptyRoomTitle = emptyRoomTitle;
        return this;
    }

    @ColorRes
    public int getEmptyRoomTitleColor() {
        return emptyRoomTitleColor;
    }

    public QiscusChatConfig setEmptyRoomTitleColor(@ColorRes int emptyRoomTitleColor) {
        this.emptyRoomTitleColor = emptyRoomTitleColor;
        return this;
    }

    public String getEmptyRoomSubtitle() {
        return emptyRoomSubtitle;
    }

    public QiscusChatConfig setEmptyRoomSubtitle(String emptyRoomSubtitle) {
        this.emptyRoomSubtitle = emptyRoomSubtitle;
        return this;
    }

    @ColorRes
    public int getEmptyRoomSubtitleColor() {
        return emptyRoomSubtitleColor;
    }

    public QiscusChatConfig setEmptyRoomSubtitleColor(@ColorRes int emptyRoomSubtitleColor) {
        this.emptyRoomSubtitleColor = emptyRoomSubtitleColor;
        return this;
    }

    @DrawableRes
    public int getEmptyRoomImageResource() {
        return emptyRoomImageResource;
    }

    public QiscusChatConfig setEmptyRoomImageResource(@DrawableRes int emptyRoomImageResource) {
        this.emptyRoomImageResource = emptyRoomImageResource;
        return this;
    }

    public String getMessageFieldHint() {
        return messageFieldHint;
    }

    public QiscusChatConfig setMessageFieldHint(String messageFieldHint) {
        this.messageFieldHint = messageFieldHint;
        return this;
    }

    @ColorRes
    public int getMessageFieldHintColor() {
        return messageFieldHintColor;
    }

    public QiscusChatConfig setMessageFieldHintColor(@ColorRes int messageFieldHintColor) {
        this.messageFieldHintColor = messageFieldHintColor;
        return this;
    }

    @ColorRes
    public int getMessageFieldTextColor() {
        return messageFieldTextColor;
    }

    public QiscusChatConfig setMessageFieldTextColor(@ColorRes int messageFieldTextColor) {
        this.messageFieldTextColor = messageFieldTextColor;
        return this;
    }

    @DrawableRes
    public int getAddPictureIcon() {
        return addPictureIcon;
    }

    public QiscusChatConfig setAddPictureIcon(@DrawableRes int addPictureIcon) {
        this.addPictureIcon = addPictureIcon;
        return this;
    }

    @ColorRes
    public int getAddPictureBackgroundColor() {
        return addPictureBackgroundColor;
    }

    public QiscusChatConfig setAddPictureBackgroundColor(@ColorRes int addPictureBackgroundColor) {
        this.addPictureBackgroundColor = addPictureBackgroundColor;
        return this;
    }

    public String getAddPictureText() {
        return addPictureText;
    }

    public QiscusChatConfig setAddPictureText(String addPictureText) {
        this.addPictureText = addPictureText;
        return this;
    }

    public boolean isEnableAddPicture() {
        return enableAddPicture;
    }

    public QiscusChatConfig setEnableAddPicture(boolean enableAddPicture) {
        this.enableAddPicture = enableAddPicture;
        return this;
    }

    @DrawableRes
    public int getTakePictureIcon() {
        return takePictureIcon;
    }

    public QiscusChatConfig setTakePictureIcon(@DrawableRes int takePictureIcon) {
        this.takePictureIcon = takePictureIcon;
        return this;
    }

    @ColorRes
    public int getTakePictureBackgroundColor() {
        return takePictureBackgroundColor;
    }

    public QiscusChatConfig setTakePictureBackgroundColor(@ColorRes int takePictureBackgroundColor) {
        this.takePictureBackgroundColor = takePictureBackgroundColor;
        return this;
    }

    public String getTakePictureText() {
        return takePictureText;
    }

    public QiscusChatConfig setTakePictureText(String takePictureText) {
        this.takePictureText = takePictureText;
        return this;
    }

    public boolean isEnableTakePicture() {
        return enableTakePicture;
    }

    public QiscusChatConfig setEnableTakePicture(boolean enableTakePicture) {
        this.enableTakePicture = enableTakePicture;
        return this;
    }

    @DrawableRes
    public int getAddFileIcon() {
        return addFileIcon;
    }

    public QiscusChatConfig setAddFileIcon(@DrawableRes int addFileIcon) {
        this.addFileIcon = addFileIcon;
        return this;
    }

    @ColorRes
    public int getAddFileBackgroundColor() {
        return addFileBackgroundColor;
    }

    public QiscusChatConfig setAddFileBackgroundColor(@ColorRes int addFileBackgroundColor) {
        this.addFileBackgroundColor = addFileBackgroundColor;
        return this;
    }

    public String getAddFileText() {
        return addFileText;
    }

    public QiscusChatConfig setAddFileText(String addFileText) {
        this.addFileText = addFileText;
        return this;
    }

    public boolean isEnableAddFile() {
        return enableAddFile;
    }

    public QiscusChatConfig setEnableAddFile(boolean enableAddFile) {
        this.enableAddFile = enableAddFile;
        return this;
    }

    @DrawableRes
    public int getRecordAudioIcon() {
        return recordAudioIcon;
    }

    public QiscusChatConfig setRecordAudioIcon(@DrawableRes int recordAudioIcon) {
        this.recordAudioIcon = recordAudioIcon;
        return this;
    }

    @ColorRes
    public int getRecordBackgroundColor() {
        return recordBackgroundColor;
    }

    public QiscusChatConfig setRecordBackgroundColor(@ColorRes int recordBackgroundColor) {
        this.recordBackgroundColor = recordBackgroundColor;
        return this;
    }

    public String getRecordText() {
        return recordText;
    }

    public QiscusChatConfig setRecordText(String recordText) {
        this.recordText = recordText;
        return this;
    }

    public boolean isEnableRecordAudio() {
        return enableRecordAudio;
    }

    public QiscusChatConfig setEnableRecordAudio(boolean enableRecordAudio) {
        this.enableRecordAudio = enableRecordAudio;
        return this;
    }

    @DrawableRes
    public int getStopRecordIcon() {
        return stopRecordIcon;
    }

    public QiscusChatConfig setStopRecordIcon(@DrawableRes int stopRecordIcon) {
        this.stopRecordIcon = stopRecordIcon;
        return this;
    }

    @DrawableRes
    public int getCancelRecordIcon() {
        return cancelRecordIcon;
    }

    public QiscusChatConfig setCancelRecordIcon(@DrawableRes int cancelRecordIcon) {
        this.cancelRecordIcon = cancelRecordIcon;
        return this;
    }

    @DrawableRes
    public int getAddContactIcon() {
        return addContactIcon;
    }

    public QiscusChatConfig setAddContactIcon(@DrawableRes int addContactIcon) {
        this.addContactIcon = addContactIcon;
        return this;
    }

    @ColorRes
    public int getAddContactBackgroundColor() {
        return addContactBackgroundColor;
    }

    public QiscusChatConfig setAddContactBackgroundColor(@ColorRes int addContactBackgroundColor) {
        this.addContactBackgroundColor = addContactBackgroundColor;
        return this;
    }

    public String getAddContactText() {
        return addContactText;
    }

    public QiscusChatConfig setAddContactText(String addContactText) {
        this.addContactText = addContactText;
        return this;
    }

    public boolean isEnableAddContact() {
        return enableAddContact;
    }

    public QiscusChatConfig setEnableAddContact(boolean enableAddContact) {
        this.enableAddContact = enableAddContact;
        return this;
    }

    @DrawableRes
    public int getAddLocationIcon() {
        return addLocationIcon;
    }

    public QiscusChatConfig setAddLocationIcon(@DrawableRes int addLocationIcon) {
        this.addLocationIcon = addLocationIcon;
        return this;
    }

    @ColorRes
    public int getAddLocationBackgroundColor() {
        return addLocationBackgroundColor;
    }

    public QiscusChatConfig setAddLocationBackgroundColor(@ColorRes int addLocationBackgroundColor) {
        this.addLocationBackgroundColor = addLocationBackgroundColor;
        return this;
    }

    public String getAddLocationText() {
        return addLocationText;
    }

    public QiscusChatConfig setAddLocationText(String addLocationText) {
        this.addLocationText = addLocationText;
        return this;
    }

    public boolean isEnableAddLocation() {
        return enableAddLocation;
    }

    public QiscusChatConfig setEnableAddLocation(boolean enableAddLocation) {
        this.enableAddLocation = enableAddLocation;
        return this;
    }

    @DrawableRes
    public int getSendButtonIcon() {
        return sendButtonIcon;
    }

    public QiscusChatConfig setSendButtonIcon(@DrawableRes int sendButtonIcon) {
        this.sendButtonIcon = sendButtonIcon;
        return this;
    }

    @DrawableRes
    public int getShowAttachmentPanelIcon() {
        return showAttachmentPanelIcon;
    }

    public QiscusChatConfig setShowAttachmentPanelIcon(@DrawableRes int showAttachmentPanelIcon) {
        this.showAttachmentPanelIcon = showAttachmentPanelIcon;
        return this;
    }

    @DrawableRes
    public int getHideAttachmentPanelIcon() {
        return hideAttachmentPanelIcon;
    }

    public QiscusChatConfig setHideAttachmentPanelIcon(@DrawableRes int hideAttachmentPanelIcon) {
        this.hideAttachmentPanelIcon = hideAttachmentPanelIcon;
        return this;
    }

    @ColorRes
    public int getHideAttachmentPanelBackgroundColor() {
        return hideAttachmentPanelBackgroundColor;
    }

    public QiscusChatConfig setHideAttachmentPanelBackgroundColor(@ColorRes int hideAttachmentPanelBackgroundColor) {
        this.hideAttachmentPanelBackgroundColor = hideAttachmentPanelBackgroundColor;
        return this;
    }

    @DrawableRes
    public int getPlayAudioIcon() {
        return playAudioIcon;
    }

    public QiscusChatConfig setPlayAudioIcon(@DrawableRes int playAudioIcon) {
        this.playAudioIcon = playAudioIcon;
        return this;
    }

    @DrawableRes
    public int getPauseAudioIcon() {
        return pauseAudioIcon;
    }

    public QiscusChatConfig setPauseAudioIcon(@DrawableRes int pauseAudioIcon) {
        this.pauseAudioIcon = pauseAudioIcon;
        return this;
    }

    @DrawableRes
    public int getShowEmojiIcon() {
        return showEmojiIcon;
    }

    public QiscusChatConfig setShowEmojiIcon(@DrawableRes int showEmojiIcon) {
        this.showEmojiIcon = showEmojiIcon;
        return this;
    }

    @DrawableRes
    public int getShowKeyboardIcon() {
        return showKeyboardIcon;
    }

    public QiscusChatConfig setShowKeyboardIcon(@DrawableRes int showKeyboardIcon) {
        this.showKeyboardIcon = showKeyboardIcon;
        return this;
    }

    @ColorRes
    public int[] getSwipeRefreshColorScheme() {
        return swipeRefreshColorScheme;
    }

    public QiscusChatConfig setSwipeRefreshColorScheme(@ColorRes int... colorResIds) {
        swipeRefreshColorScheme = colorResIds;
        return this;
    }

    @DrawableRes
    public int getNotificationSmallIcon() {
        return notificationSmallIcon;
    }

    public QiscusChatConfig setNotificationSmallIcon(@DrawableRes int notificationSmallIcon) {
        this.notificationSmallIcon = notificationSmallIcon;
        return this;
    }

    @DrawableRes
    public int getNotificationBigIcon() {
        return notificationBigIcon;
    }

    public QiscusChatConfig setNotificationBigIcon(@DrawableRes int notificationBigIcon) {
        this.notificationBigIcon = notificationBigIcon;
        return this;
    }

    public boolean isEnableAvatarAsNotificationIcon() {
        return enableAvatarAsNotificationIcon;
    }

    public QiscusChatConfig setEnableAvatarAsNotificationIcon(boolean enableAvatarAsNotificationIcon) {
        this.enableAvatarAsNotificationIcon = enableAvatarAsNotificationIcon;
        return this;
    }

    public boolean isEnableReplyNotification() {
        return enableReplyNotification;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public QiscusChatConfig setEnableReplyNotification(boolean enableReplyNotification) {
        this.enableReplyNotification = enableReplyNotification;
        return this;
    }

    public QiscusNotificationBuilderInterceptor getNotificationBuilderInterceptor() {
        return notificationBuilderInterceptor;
    }

    public QiscusChatConfig setNotificationBuilderInterceptor(QiscusNotificationBuilderInterceptor notificationBuilderInterceptor) {
        this.notificationBuilderInterceptor = notificationBuilderInterceptor;
        return this;
    }

    public NotificationTitleHandler getNotificationTitleHandler() {
        return notificationTitleHandler;
    }

    public QiscusChatConfig setNotificationTitleHandler(NotificationTitleHandler notificationTitleHandler) {
        this.notificationTitleHandler = notificationTitleHandler;
        return this;
    }

    public NotificationClickListener getNotificationClickListener() {
        return notificationClickListener;
    }

    public QiscusChatConfig setNotificationClickListener(NotificationClickListener notificationClickListener) {
        this.notificationClickListener = notificationClickListener;
        return this;
    }

    public ReplyNotificationHandler getReplyNotificationHandler() {
        return replyNotificationHandler;
    }

    public QiscusChatConfig setReplyNotificationHandler(ReplyNotificationHandler replyNotificationHandler) {
        this.replyNotificationHandler = replyNotificationHandler;
        return this;
    }

    public boolean isEnablePushNotification() {
        return enablePushNotification;
    }

    public QiscusChatConfig setEnablePushNotification(boolean enablePushNotification) {
        this.enablePushNotification = enablePushNotification;
        return this;
    }

    public boolean isOnlyEnablePushNotificationOutsideChatRoom() {
        return onlyEnablePushNotificationOutsideChatRoom;
    }

    public QiscusChatConfig setOnlyEnablePushNotificationOutsideChatRoom(boolean onlyEnablePushNotificationOutsideChatRoom) {
        this.onlyEnablePushNotificationOutsideChatRoom = onlyEnablePushNotificationOutsideChatRoom;
        return this;
    }

    public boolean isEnableFcmPushNotification() {
        return QiscusCore.getChatConfig().isEnableFcmPushNotification();
    }

    public QiscusChatConfig setEnableFcmPushNotification(boolean enableFcmPushNotification) {
        QiscusCore.getChatConfig().setEnableFcmPushNotification(enableFcmPushNotification);
        return this;
    }

    public Drawable getChatRoomBackground() {
        return chatRoomBackground;
    }

    public QiscusChatConfig setChatRoomBackground(Drawable chatRoomBackground) {
        this.chatRoomBackground = chatRoomBackground;
        return this;
    }

    public QiscusImageCompressionConfig getQiscusImageCompressionConfig() {
        return QiscusCore.getChatConfig().getQiscusImageCompressionConfig();
    }

    public QiscusChatConfig setQiscusImageCompressionConfig(QiscusImageCompressionConfig qiscusImageCompressionConfig) {
        QiscusCore.getChatConfig().setQiscusImageCompressionConfig(qiscusImageCompressionConfig);
        return this;
    }

    public boolean isEnableRequestPermission() {
        return enableRequestPermission;
    }

    public QiscusChatConfig setEnableRequestPermission(boolean enableRequestPermission) {
        this.enableRequestPermission = enableRequestPermission;
        return this;
    }

    @ColorRes
    public int getReplyBarColor() {
        return replyBarColor;
    }

    public QiscusChatConfig setReplyBarColor(@ColorRes int replyBarColor) {
        this.replyBarColor = replyBarColor;
        return this;
    }

    @ColorRes
    public int getReplySenderColor() {
        return replySenderColor;
    }

    public QiscusChatConfig setReplySenderColor(@ColorRes int replySenderColor) {
        this.replySenderColor = replySenderColor;
        return this;
    }

    @ColorRes
    public int getReplyMessageColor() {
        return replyMessageColor;
    }

    public QiscusChatConfig setReplyMessageColor(@ColorRes int replyMessageColor) {
        this.replyMessageColor = replyMessageColor;
        return this;
    }

    @ColorRes
    public int getCardTitleColor() {
        return cardTitleColor;
    }

    public QiscusChatConfig setCardTitleColor(@ColorRes int cardTitleColor) {
        this.cardTitleColor = cardTitleColor;
        return this;
    }

    @ColorRes
    public int getCardDescriptionColor() {
        return cardDescriptionColor;
    }

    public QiscusChatConfig setCardDescriptionColor(@ColorRes int cardDescriptionColor) {
        this.cardDescriptionColor = cardDescriptionColor;
        return this;
    }

    @ColorRes
    public int getCardButtonTextColor() {
        return cardButtonTextColor;
    }

    public QiscusChatConfig setCardButtonTextColor(@ColorRes int cardButtonTextColor) {
        this.cardButtonTextColor = cardButtonTextColor;
        return this;
    }

    @ColorRes
    public int getCardButtonBackground() {
        return cardButtonBackground;
    }

    public QiscusChatConfig setCardButtonBackground(@ColorRes int cardButtonBackground) {
        this.cardButtonBackground = cardButtonBackground;
        return this;
    }

    public ForwardCommentHandler getForwardCommentHandler() {
        return forwardCommentHandler;
    }

    public QiscusChatConfig setForwardCommentHandler(ForwardCommentHandler forwardCommentHandler) {
        this.forwardCommentHandler = forwardCommentHandler;
        return this;
    }

    public boolean isEnableForwardComment() {
        return enableForwardComment;
    }

    public QiscusChatConfig setEnableForwardComment(boolean enableForwardComment) {
        this.enableForwardComment = enableForwardComment;
        return this;
    }

    public CommentInfoHandler getCommentInfoHandler() {
        return commentInfoHandler;
    }

    public QiscusChatConfig setCommentInfoHandler(CommentInfoHandler commentInfoHandler) {
        this.commentInfoHandler = commentInfoHandler;
        return this;
    }

    public boolean isEnableCommentInfo() {
        return enableCommentInfo;
    }

    public QiscusChatConfig setEnableCommentInfo(boolean enableCommentInfo) {
        this.enableCommentInfo = enableCommentInfo;
        return this;
    }

    public boolean isEnableShareMedia() {
        return enableShareMedia;
    }

    public QiscusChatConfig setEnableShareMedia(boolean enableShareMedia) {
        this.enableShareMedia = enableShareMedia;
        return this;
    }

    public QiscusMentionConfig getMentionConfig() {
        return mentionConfig;
    }

    public QiscusChatConfig setMentionConfig(QiscusMentionConfig mentionConfig) {
        this.mentionConfig = mentionConfig;
        return this;
    }

    public QiscusRoomSenderNameInterceptor getRoomSenderNameInterceptor() {
        return qiscusRoomSenderNameInterceptor;
    }

    public QiscusChatConfig setRoomSenderNameInterceptor(QiscusRoomSenderNameInterceptor
                                                                 qiscusRoomSenderNameInterceptor) {
        this.qiscusRoomSenderNameInterceptor = qiscusRoomSenderNameInterceptor;
        return this;
    }

    public QiscusRoomSenderNameColorInterceptor getRoomSenderNameColorInterceptor() {
        return qiscusRoomSenderNameColorInterceptor;
    }

    public QiscusChatConfig setRoomSenderNameColorInterceptor(QiscusRoomSenderNameColorInterceptor
                                                                      qiscusRoomSenderNameColorInterceptor) {
        this.qiscusRoomSenderNameColorInterceptor = qiscusRoomSenderNameColorInterceptor;
        return this;
    }

    public QiscusRoomReplyBarColorInterceptor getRoomReplyBarColorInterceptor() {
        return qiscusRoomReplyBarColorInterceptor;
    }

    public QiscusChatConfig setRoomReplyBarColorInterceptor(QiscusRoomReplyBarColorInterceptor
                                                                    qiscusRoomReplyBarColorInterceptor) {
        this.qiscusRoomReplyBarColorInterceptor = qiscusRoomReplyBarColorInterceptor;
        return this;
    }

    public QiscusCommentSendingInterceptor getCommentSendingInterceptor() {
        return QiscusCore.getChatConfig().getCommentSendingInterceptor();
    }

    public QiscusChatConfig setCommentSendingInterceptor(QiscusCommentSendingInterceptor
                                                                 qiscusCommentSendingInterceptor) {
        QiscusCore.getChatConfig().setCommentSendingInterceptor(qiscusCommentSendingInterceptor);
        return this;
    }

    public boolean isEnableCaption() {
        return enableCaption;
    }

    public QiscusChatConfig setEnableCaption(boolean enableCaption) {
        this.enableCaption = enableCaption;
        return this;
    }

    @ColorRes
    public int getDialogPermissionPositiveButtonTextColor() {
        return dialogPermissionPositiveButtonTextColor;
    }

    public QiscusChatConfig setDialogPermissionPositiveButtonTextColor
            (@ColorRes int dialogPermissionPositiveButtonTextColor) {
        this.dialogPermissionPositiveButtonTextColor = dialogPermissionPositiveButtonTextColor;
        return this;
    }

    @ColorRes
    public int getDialogPermissionNegativeButtonTextColor() {
        return dialogPermissionNegativeButtonTextColor;
    }

    public QiscusChatConfig setDialogPermissionNegativeButtonTextColor
            (@ColorRes int dialogPermissionNegativeButtonTextColor) {
        this.dialogPermissionNegativeButtonTextColor = dialogPermissionNegativeButtonTextColor;
        return this;
    }

    public QiscusDeleteCommentConfig getDeleteCommentConfig() {
        return deleteCommentConfig;
    }

    public QiscusChatConfig setDeleteCommentConfig(QiscusDeleteCommentConfig deleteCommentConfig) {
        this.deleteCommentConfig = deleteCommentConfig;
        return this;
    }

    public QiscusStartReplyInterceptor getStartReplyInterceptor() {
        return startReplyInterceptor;
    }

    public QiscusChatConfig setStartReplyInterceptor(QiscusStartReplyInterceptor startReplyInterceptor) {
        this.startReplyInterceptor = startReplyInterceptor;
        return this;
    }

    public boolean isEnableLog() {
        return QiscusCore.getChatConfig().isEnableLog();
    }

    @Deprecated
    public QiscusChatConfig setEnableLog(boolean enableLog) {
        QiscusCore.getChatConfig().setEnableLog(enableLog);
        return this;
    }

    public QiscusChatConfig enableDebugMode(boolean enableDebugMode) {
        QiscusCore.getChatConfig().enableDebugMode(enableDebugMode);
        return this;
    }
}

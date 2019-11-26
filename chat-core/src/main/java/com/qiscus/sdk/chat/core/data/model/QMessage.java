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

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.remote.QiscusUrlScraper;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;
import com.qiscus.sdk.chat.core.util.QiscusRawDataExtractor;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;
import com.schinizer.rxunfurl.model.PreviewData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QMessage implements Parcelable {
    public static final int STATE_FAILED = -1;
    public static final int STATE_PENDING = 0;
    public static final int STATE_SENDING = 1;
    public static final int STATE_ON_QISCUS = 2;
    public static final int STATE_DELIVERED = 3;
    public static final int STATE_READ = 4;
    public static final Creator<QMessage> CREATOR = new Creator<QMessage>() {
        @Override
        public QMessage createFromParcel(Parcel in) {
            return new QMessage(in);
        }

        @Override
        public QMessage[] newArray(int size) {
            return new QMessage[size];
        }
    };
    protected long id;
    protected long chatRoomId;
    protected String uniqueId;
    protected long previousMessageId;
    protected String message;
    protected String sender;
    protected String senderEmail;
    protected String senderAvatar;
    protected Date timestamp;
    protected int state;
    protected boolean deleted;
    protected boolean hardDeleted;
    protected String roomName;
    protected String roomAvatar;
    protected boolean groupMessage;
    protected boolean selected;
    protected boolean highlighted;
    protected boolean downloading;
    protected int progress;
    protected ProgressListener progressListener;
    protected DownloadingListener downloadingListener;
    protected PlayingAudioListener playingAudioListener;
    protected LinkPreviewListener linkPreviewListener;
    private List<String> urls;
    private PreviewData previewData;
    private QiscusContact contact;
    private QiscusLocation location;
    private String rawType;
    private JSONObject payload;
    private JSONObject extras;
    private MediaObserver observer;
    private MediaPlayer player;
    private QMessage replyTo;
    private String caption;
    private String attachmentName;

    public QMessage() {

    }

    protected QMessage(Parcel in) {
        id = in.readLong();
        chatRoomId = in.readLong();
        uniqueId = in.readString();
        previousMessageId = in.readLong();
        message = in.readString();
        sender = in.readString();
        senderEmail = in.readString();
        senderAvatar = in.readString();
        timestamp = new Date(in.readLong());
        state = in.readInt();
        deleted = in.readByte() != 0;
        hardDeleted = in.readByte() != 0;
        selected = in.readByte() != 0;
        rawType = in.readString();

        replyTo = in.readParcelable(QMessage.class.getClassLoader());
        try {
            extras = new JSONObject(in.readString());
            payload = new JSONObject(in.readString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static QMessage generateMessage(long roomId, String content) {
        QAccount qAccount = QiscusCore.getQiscusAccount();
        QMessage qiscusMessage = new QMessage();
        qiscusMessage.setId(-1);
        qiscusMessage.setChatRoomId(roomId);
        qiscusMessage.setUniqueId("android_"
                + System.currentTimeMillis()
                + QiscusTextUtil.getRandomString(8)
                + Settings.Secure.getString(QiscusCore.getApps().getContentResolver(),
                Settings.Secure.ANDROID_ID));
        qiscusMessage.setMessage(content);
        qiscusMessage.setTimestamp(new Date());
        qiscusMessage.setSenderEmail(qAccount.getId());
        qiscusMessage.setSender(qAccount.getName());
        qiscusMessage.setSenderAvatar(qAccount.getAvatarUrl());
        qiscusMessage.setState(STATE_SENDING);

        return qiscusMessage;
    }

    public static QMessage generateFileAttachmentMessage(long roomId, String fileUrl, String caption, String name) {
        QMessage qiscusMessage = generateMessage(roomId, String.format("[file] %s [/file]", fileUrl));
        qiscusMessage.setRawType("file_attachment");
        JSONObject json = new JSONObject();
        try {
            json.put("url", fileUrl)
                    .put("caption", caption)
                    .put("file_name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        qiscusMessage.setPayload(json);
        return qiscusMessage;
    }

    public static QMessage generateReplyMessage(long roomId, String content, QMessage repliedComment) {
        QMessage qiscusMessage = generateMessage(roomId, content);
        qiscusMessage.setRawType("reply");
        qiscusMessage.setReplyTo(repliedComment);
        JSONObject json = new JSONObject();
        try {
            json.put("text", qiscusMessage.getMessage())
                    .put("replied_comment_id", repliedComment.getId())
                    .put("replied_comment_message", repliedComment.getMessage())
                    .put("replied_comment_sender_username", repliedComment.getSender())
                    .put("replied_comment_sender_email", repliedComment.getSenderEmail())
                    .put("replied_comment_type", repliedComment.getRawType())
                    .put("replied_comment_payload", repliedComment.getPayload().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        qiscusMessage.setPayload(json);

        return qiscusMessage;
    }

    public static QMessage generateContactMessage(long roomId, QiscusContact contact) {
        QMessage qiscusMessage = generateMessage(roomId, contact.getName() + "\n" + contact.getValue());
        qiscusMessage.setRawType("contact_person");
        qiscusMessage.setContact(contact);
        JSONObject json = new JSONObject();
        try {
            json.put("name", contact.getName()).put("value", contact.getValue());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        qiscusMessage.setPayload(json);

        return qiscusMessage;
    }

    public static QMessage generateLocationMessage(long roomId, QiscusLocation location) {
        QMessage qiscusMessage = generateMessage(roomId, location.getName() + " - " + location.getAddress()
                + "\n" + location.getMapUrl());
        qiscusMessage.setRawType("location");
        qiscusMessage.setLocation(location);
        JSONObject json = new JSONObject();
        try {
            json.put("name", location.getName()).put("address", location.getAddress())
                    .put("latitude", location.getLatitude()).put("longitude", location.getLongitude())
                    .put("map_url", location.getMapUrl());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        qiscusMessage.setPayload(json);

        return qiscusMessage;
    }

    public static QMessage generatePostBackMessage(long roomId, String content, JSONObject payload) {
        QMessage qiscusMessage = generateMessage(roomId, content);
        qiscusMessage.setRawType("button_postback_response");
        qiscusMessage.setPayload(payload);
        return qiscusMessage;
    }

    /**
     * Helper method to generate custom comment with your defined payload
     *
     * @param text    default text message for older apps
     * @param type    your custom type
     * @param content your custom payload
     * @param roomId  room id for these comment
     * @return QMessage
     */
    public static QMessage generateCustomMessage(long roomId, String text, String type, JSONObject content) {
        QMessage qiscusMessage = generateMessage(roomId, text);
        qiscusMessage.setRawType("custom");
        JSONObject json = new JSONObject();
        try {
            json.put("type", type).put("content", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        qiscusMessage.setPayload(json);
        return qiscusMessage;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public long getPreviousMessageId() {
        return previousMessageId;
    }

    public void setPreviousMessageId(long commentBeforeId) {
        this.previousMessageId = commentBeforeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isHardDeleted() {
        return hardDeleted;
    }

    public void setHardDeleted(boolean hardDeleted) {
        this.hardDeleted = hardDeleted;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomAvatar() {
        return isGroupMessage() ? roomAvatar : senderAvatar;
    }

    public void setRoomAvatar(String roomAvatar) {
        this.roomAvatar = roomAvatar;
    }

    public boolean isGroupMessage() {
        return groupMessage;
    }

    public void setGroupMessage(boolean groupMessage) {
        this.groupMessage = groupMessage;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public String getRawType() {
        return rawType;
    }

    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

    public JSONObject getPayload() {
        return payload;
    }

    public void setPayload(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getExtras() {
        return extras;
    }

    public void setExtras(JSONObject extras) {
        this.extras = extras;
    }

    public boolean isMyComment() {
        return getSenderEmail().equals(QiscusCore.getQiscusAccount().getId());
    }

    public QMessage getReplyTo() {
        if (replyTo == null && getType() == Type.REPLY) {
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(this);
                replyTo = new QMessage();
                replyTo.id = payload.getInt("replied_comment_id");
                replyTo.uniqueId = replyTo.id + "";
                replyTo.message = payload.getString("replied_comment_message");
                replyTo.sender = payload.getString("replied_comment_sender_username");
                replyTo.senderEmail = payload.getString("replied_comment_sender_email");
                replyTo.rawType = payload.optString("replied_comment_type");
                replyTo.payload = payload.getJSONObject("replied_comment_payload");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return replyTo;
    }

    public void setReplyTo(QMessage replyTo) {
        this.replyTo = replyTo;
    }

    public void updateAttachmentUrl(String url) {
        setMessage(String.format("[file] %s [/file]", url));
        try {
            JSONObject json = getPayload();
            json.put("url", url);
            setPayload(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAttachment() {
        String trimmedMessage = message.trim().replaceAll(" ", "");
        return (trimmedMessage.startsWith("[file]") && trimmedMessage.endsWith("[/file]"))
                || (!TextUtils.isEmpty(rawType) && rawType.equals("file_attachment"));
    }

    public Uri getAttachmentUri() {
        if (!isAttachment()) {
            throw new RuntimeException("Current comment is not an attachment");
        }

        String uriStr = message.replaceAll("\\[file\\]", "").replaceAll("\\[/file\\]", "").trim();
        return Uri.parse(uriStr);
    }

    public String getCaption() {
        if (!isAttachment()) {
            return getMessage();
        }
        if (caption == null) {
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(this);
                caption = payload.optString("caption", "");
            } catch (Exception ignored) {
                //Do nothing
            }
        }
        return caption;
    }

    public String getAttachmentName() {
        if (!isAttachment()) {
            throw new RuntimeException("Current comment is not an attachment");
        }

        if (attachmentName == null) {
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(this);
                attachmentName = payload.optString("file_name", "");
            } catch (Exception ignored) {
                //Do nothing
            }

            if (!TextUtils.isEmpty(attachmentName)) {
                return attachmentName;
            }

            int fileNameEndIndex = -1;
            int fileNameBeginIndex;
            String fileName;

            fileNameEndIndex = message.lastIndexOf(" [/file]");

            if (fileNameEndIndex != -1) {
                fileNameBeginIndex = message.lastIndexOf('/', fileNameEndIndex) + 1;
                fileName = message.substring(fileNameBeginIndex, fileNameEndIndex);
            } else {
                fileNameEndIndex = message.lastIndexOf("[/file]");
                fileNameBeginIndex = message.lastIndexOf('/', fileNameEndIndex) + 1;
                fileName = message.substring(fileNameBeginIndex, fileNameEndIndex);
            }

            try {
                fileName = fileName.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                fileName = fileName.replaceAll("\\+", "%2B");
                attachmentName = URLDecoder.decode(fileName, "UTF-8");
                return attachmentName;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            throw new RuntimeException("The filename '" + fileName + "' is not valid UTF-8");
        }

        return attachmentName;
    }

    public String getExtension() {
        if (!isAttachment()) {
            throw new RuntimeException("Current comment is not an attachment");
        }

        return QiscusFileUtil.getExtension(getAttachmentName());
    }

    public boolean isImage() {
        if (isAttachment()) {
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension());
            if (type == null) {
                return false;
            } else if (type.contains("image")) {
                return true;
            }
        }
        return false;
    }

    public boolean isVideo() {
        if (isAttachment()) {
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension());
            if (type == null) {
                return false;
            } else if (type.contains("video")) {
                return true;
            }
        }
        return false;
    }

    public boolean isAudio() {
        if (isAttachment()) {
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension());
            if (type == null) {
                return false;
            } else if (type.contains("audio")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsUrl() {
        if (urls == null) {
            urls = QiscusTextUtil.extractUrl(message);
        }
        return !urls.isEmpty();
    }

    public List<String> getUrls() {
        if (urls == null) {
            urls = QiscusTextUtil.extractUrl(message);
        }
        return urls;
    }

    public void loadLinkPreviewData() {
        if (getType() == Type.LINK) {
            if (previewData != null) {
                linkPreviewListener.onLinkPreviewReady(this, previewData);
            } else {
                QiscusUrlScraper.getInstance()
                        .generatePreviewData(urls.get(0))
                        .doOnNext(previewData -> previewData.setUrl(urls.get(0)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(previewData -> {
                            this.previewData = previewData;
                            if (linkPreviewListener != null) {
                                linkPreviewListener.onLinkPreviewReady(this, previewData);
                            }
                        }, Throwable::printStackTrace);
            }
        }
    }

    public QiscusContact getContact() {
        if (contact == null && getType() == Type.CONTACT) {
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(this);
                contact = new QiscusContact(payload.optString("name"), payload.optString("value"),
                        payload.optString("type", "phone"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return contact;
    }

    public void setContact(QiscusContact contact) {
        this.contact = contact;
    }

    public QiscusLocation getLocation() {
        if (location == null && getType() == Type.LOCATION) {
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(this);
                location = new QiscusLocation();
                location.setName(payload.optString("name"));
                location.setAddress(payload.optString("address"));
                location.setLatitude(payload.optDouble("latitude"));
                location.setLongitude(payload.optDouble("longitude"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return location;
    }

    public void setLocation(QiscusLocation location) {
        this.location = location;
    }

    public Type getType() {
        if (!TextUtils.isEmpty(rawType) && rawType.equals("account_linking")) {
            return Type.ACCOUNT_LINKING;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("buttons")) {
            return Type.BUTTONS;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("reply")) {
            return Type.REPLY;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("card")) {
            return Type.CARD;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("system_event")) {
            return Type.SYSTEM_EVENT;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("contact_person")) {
            return Type.CONTACT;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("location")) {
            return Type.LOCATION;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("carousel")) {
            return Type.CAROUSEL;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("custom")) {
            return Type.CUSTOM;
        } else if (!isAttachment()) {
            if (containsUrl()) {
                return Type.LINK;
            }
            return Type.TEXT;
        } else if (isImage()) {
            return Type.IMAGE;
        } else if (isVideo()) {
            return Type.VIDEO;
        } else if (isAudio()) {
            return Type.AUDIO;
        } else {
            return Type.FILE;
        }
    }

    public boolean isDownloading() {
        return downloading;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
        QiscusAndroidUtil.runOnUIThread(() -> {
            if (downloadingListener != null) {
                downloadingListener.onDownloading(this, downloading);
            }
        });
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int percentage) {
        this.progress = percentage;
        QiscusAndroidUtil.runOnUIThread(() -> {
            if (progressListener != null) {
                progressListener.onProgress(this, progress);
            }
        });
    }

    private void setupPlayer() {
        if (player == null) {
            File localPath = QiscusCore.getDataStore().getLocalPath(id);
            if (localPath != null) {
                try {
                    player = new MediaPlayer();
                    player.setDataSource(localPath.getAbsolutePath());
                    player.prepare();
                    player.setOnCompletionListener(mp -> {
                        observer.stop();
                        if (playingAudioListener != null) {
                            playingAudioListener.onStopAudio(this);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void playAudio() {
        if (!isAudio()) {
            throw new RuntimeException("Current comment is not an audio");
        }

        if (observer == null) {
            observer = new MediaObserver();
        }

        setupPlayer();

        if (!player.isPlaying()) {
            player.start();
            observer.start();
            new Thread(observer).start();
        } else {
            player.pause();
            observer.stop();
            if (playingAudioListener != null) {
                playingAudioListener.onPauseAudio(this);
            }
        }
    }

    public boolean isPlayingAudio() {
        return player != null && player.isPlaying();
    }

    public int getAudioDuration() {
        if (player == null && isAudio()) {
            File localPath = QiscusCore.getDataStore().getLocalPath(id);
            if (localPath == null) {
                return 0;
            } else {
                setupPlayer();
            }
        }
        return player.getDuration();
    }

    public int getCurrentAudioPosition() {
        if (player == null && isAudio()) {
            File localPath = QiscusCore.getDataStore().getLocalPath(id);
            if (localPath == null) {
                return 0;
            } else {
                setupPlayer();
            }
        }
        return player.getCurrentPosition();
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void setDownloadingListener(DownloadingListener downloadingListener) {
        this.downloadingListener = downloadingListener;
    }

    public void setPlayingAudioListener(PlayingAudioListener playingAudioListener) {
        this.playingAudioListener = playingAudioListener;
    }

    public void setLinkPreviewListener(LinkPreviewListener linkPreviewListener) {
        this.linkPreviewListener = linkPreviewListener;
    }

    public void destroy() {
        if (playingAudioListener != null) {
            playingAudioListener = null;
        }
        if (observer != null) {
            observer.stop();
            observer = null;
        }
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }

        if (progressListener != null) {
            progressListener = null;
        }

        if (downloadingListener != null) {
            downloadingListener = null;
        }

        if (linkPreviewListener != null) {
            linkPreviewListener = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof QMessage) {
            QMessage qiscusMessage = (QMessage) o;
            if (id == -1) {
                return qiscusMessage.uniqueId.equals(uniqueId);
            } else {
                return qiscusMessage.id == id || qiscusMessage.uniqueId.equals(uniqueId);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }

    @Override
    public String toString() {
        return "QMessage{" +
                "id=" + id +
                ", chatRoomId=" + chatRoomId +
                ", uniqueId='" + uniqueId + '\'' +
                ", previousMessageId=" + previousMessageId +
                ", message='" + message + '\'' +
                ", sender='" + sender + '\'' +
                ", senderEmail='" + senderEmail + '\'' +
                ", senderAvatar='" + senderAvatar + '\'' +
                ", timestamp=" + timestamp +
                ", state=" + state +
                ", deleted=" + deleted +
                ", hardDeleted=" + hardDeleted +
                '}';
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(chatRoomId);
        dest.writeString(uniqueId);
        dest.writeLong(previousMessageId);
        dest.writeString(message);
        dest.writeString(sender);
        dest.writeString(senderEmail);
        dest.writeString(senderAvatar);
        if (timestamp == null) {
            timestamp = new Date();
        }
        dest.writeLong(timestamp.getTime());
        dest.writeInt(state);
        dest.writeByte((byte) (deleted ? 1 : 0));
        dest.writeByte((byte) (hardDeleted ? 1 : 0));
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeString(rawType);
        dest.writeParcelable(replyTo, flags);
        if (extras == null) {
            try {
                extras = new JSONObject("{}");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        dest.writeString(extras.toString());

        if (payload == null) {
            try {
                payload = new JSONObject("{}");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        dest.writeString(payload.toString());
    }

    public boolean areContentsTheSame(QMessage qiscusMessage) {
        return id == qiscusMessage.id
                && uniqueId.equals(qiscusMessage.uniqueId)
                && chatRoomId == qiscusMessage.chatRoomId
                && previousMessageId == qiscusMessage.previousMessageId
                && message.equals(qiscusMessage.message)
                && sender.equals(qiscusMessage.sender)
                && senderEmail.equals(qiscusMessage.senderEmail)
                && senderAvatar.equals(qiscusMessage.senderAvatar)
                && timestamp.equals(qiscusMessage.timestamp)
                && state == qiscusMessage.state
                && deleted == qiscusMessage.deleted
                && hardDeleted == qiscusMessage.hardDeleted
                && selected == qiscusMessage.selected
                && highlighted == qiscusMessage.highlighted
                && downloading == qiscusMessage.downloading
                && progress == qiscusMessage.progress;
    }

    public enum Type {
        TEXT, IMAGE, VIDEO, FILE, AUDIO, LINK, ACCOUNT_LINKING, BUTTONS, REPLY, SYSTEM_EVENT, CARD,
        CONTACT, LOCATION, CAROUSEL, CUSTOM
    }

    public interface ProgressListener {
        void onProgress(QMessage qiscusMessage, int percentage);
    }

    public interface DownloadingListener {
        void onDownloading(QMessage qiscusMessage, boolean downloading);
    }

    public interface PlayingAudioListener {
        void onPlayingAudio(QMessage qiscusMessage, int currentPosition);

        void onPauseAudio(QMessage qiscusMessage);

        void onStopAudio(QMessage qiscusMessage);
    }

    public interface LinkPreviewListener {
        void onLinkPreviewReady(QMessage qiscusMessage, PreviewData previewData);
    }

    private class MediaObserver implements Runnable {
        private AtomicBoolean stopPlay = new AtomicBoolean(false);

        public void stop() {
            stopPlay.set(true);
        }

        public void start() {
            stopPlay.set(false);
        }

        @Override
        public void run() {
            while (!stopPlay.get()) {
                QiscusAndroidUtil.runOnUIThread(() -> {
                    if (playingAudioListener != null) {
                        playingAudioListener.onPlayingAudio(QMessage.this, player.getCurrentPosition());
                    }
                });
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

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
import com.qiscus.sdk.chat.core.util.QiscusConst;
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
    public static final int STATE_SENT = 2;
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
    protected String text;
    protected QUser sender;
    protected Date timestamp;
    protected int status;
    protected boolean deleted;
    private String rawType;
    private String payload;
    private JSONObject extras;
    private String appId;
    private QMessage replyTo;
    private String attachmentName;
    protected boolean selected;
    protected boolean highlighted;
    private QiscusLocation location;
    protected boolean downloading;
    protected int progress;
    protected ProgressListener progressListener;
    protected DownloadingListener downloadingListener;
    protected PlayingAudioListener playingAudioListener;
    protected LinkPreviewListener linkPreviewListener;
    private MediaObserver observer;
    private MediaPlayer player;
    private List<String> urls;
    private PreviewData previewData;
    private QiscusContact contact;

    public QMessage() {

    }

    protected QMessage(Parcel in) {
        id = in.readLong();
        chatRoomId = in.readLong();
        uniqueId = in.readString();
        previousMessageId = in.readLong();
        text = in.readString();
        appId = in.readString();
        sender = in.readParcelable(QUser.class.getClassLoader());
        timestamp = new Date(in.readLong());
        status = in.readInt();
        deleted = in.readByte() != 0;
        selected = in.readByte() != 0;
        rawType = in.readString();
        replyTo = in.readParcelable(QMessage.class.getClassLoader());

        payload = in.readString();
        try {
            extras = new JSONObject(in.readString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static QMessage generateMessage(long roomId, String message) {
        QMessage qiscusMessage = new QMessage();
        qiscusMessage.setId(-1);
        qiscusMessage.setChatRoomId(roomId);
        qiscusMessage.setUniqueId("android_"
                + System.currentTimeMillis()
                + QiscusTextUtil.getRandomString(8)
                + Settings.Secure.getString(QiscusConst.getApps().getContentResolver(),
                Settings.Secure.ANDROID_ID));
        qiscusMessage.setText(message);
        qiscusMessage.setTimestamp(new Date());
        qiscusMessage.setStatus(STATE_SENDING);


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
        qiscusMessage.setPayload(json.toString());
        return qiscusMessage;
    }

    public static QMessage generateFileAttachmentMessage(long roomId, String caption, String name) {
        QMessage qiscusMessage = generateMessage(roomId, "");
        qiscusMessage.setRawType("file_attachment");
        JSONObject json = new JSONObject();
        try {
            json.put("caption", caption)
                    .put("file_name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        qiscusMessage.setPayload(json.toString());
        return qiscusMessage;
    }

    public static QMessage generateReplyMessage(long roomId, String text, QMessage repliedMessage) {
        QMessage qiscusMessage = generateMessage(roomId, text);
        qiscusMessage.setReplyTo(repliedMessage);
        qiscusMessage.setRawType("reply");
        JSONObject json = new JSONObject();
        try {
            json.put("text", qiscusMessage.getText())
                    .put("replied_comment_id", repliedMessage.getId())
                    .put("replied_comment_message", repliedMessage.getText())
                    .put("replied_comment_sender_username", repliedMessage.getSender().getName())
                    .put("replied_comment_sender_email", repliedMessage.getSender().getId())
                    .put("replied_comment_type", repliedMessage.getRawType())
                    .put("replied_comment_payload", repliedMessage.getPayload());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        qiscusMessage.setPayload(json.toString());

        return qiscusMessage;
    }

    public static QMessage generatePostBackMessage(long roomId, String content, JSONObject payload) {
        QMessage qiscusMessage = generateMessage(roomId, content);
        qiscusMessage.setRawType("button_postback_response");
        qiscusMessage.setPayload(payload.toString());
        return qiscusMessage;
    }

    public static QMessage generateLocationMessage(long roomId, QiscusLocation location) {
        QMessage qiscusComment = generateMessage(roomId, location.getName() + " - " + location.getAddress()
                + "\n" + location.getMapUrl());
        qiscusComment.setRawType("location");
        qiscusComment.setLocation(location);
        JSONObject json = new JSONObject();
        try {
            json.put("name", location.getName()).put("address", location.getAddress())
                    .put("latitude", location.getLatitude()).put("longitude", location.getLongitude())
                    .put("map_url", location.getMapUrl());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        qiscusComment.setPayload(json.toString());

        return qiscusComment;
    }

    /**
     * Helper method to generate custom comment with your defined payload
     *
     * @param text    default text message for older apps
     * @param type    your custom type
     * @param payload your custom payload
     * @param roomId  room id for these comment
     * @return QMessage
     */
    public static QMessage generateCustomMessage(long roomId, String text, String type, JSONObject payload) {
        QMessage qiscusMessage = generateMessage(roomId, text);
        qiscusMessage.setRawType("custom");
        JSONObject json = new JSONObject();
        try {
            json.put("type", type).put("content", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        qiscusMessage.setPayload(json.toString());
        return qiscusMessage;
    }

    public boolean isMyComment(String userId) {
        return getSender().getId().equals(userId);
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QUser getSender() {
        return sender;
    }

    public void setSender(QUser sender) {
        this.sender = sender;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getRawType() {
        return rawType;
    }

    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public JSONObject getExtras() {
        return extras;
    }

    public void setExtras(JSONObject extras) {
        this.extras = extras;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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


    public QMessage getReplyTo() {
        if (replyTo == null && getType() == Type.REPLY) {
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(this);
                replyTo = new QMessage();
                replyTo.id = payload.getLong("replied_comment_id");
                replyTo.uniqueId = replyTo.id + "";
                replyTo.text = payload.getString("replied_comment_message");

                QUser qUser = new QUser();
                qUser.setName(payload.getString("replied_comment_sender_username"));
                qUser.setId(payload.getString("replied_comment_sender_email"));

                replyTo.sender = qUser;
                replyTo.sender.id = payload.getString("replied_comment_sender_email");
                replyTo.rawType = payload.optString("replied_comment_type");
                replyTo.payload = payload.optString("replied_comment_payload");
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
        setText(String.format("[file] %s [/file]", url));
        try {
            JSONObject json = new JSONObject(getPayload());
            json.put("url", url);
            setPayload(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAttachment() {
        String trimmedMessage = text.trim().replaceAll(" ", "");
        return (trimmedMessage.startsWith("[file]") && trimmedMessage.endsWith("[/file]"))
                || (!TextUtils.isEmpty(rawType) && rawType.equals("file_attachment"));
    }

    public Uri getAttachmentUri() {
        if (!isAttachment()) {
            throw new RuntimeException("Current comment is not an attachment");
        }

        String uriStr = text.replaceAll("\\[file\\]", "").replaceAll("\\[/file\\]", "").trim();
        return Uri.parse(uriStr);
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

            fileNameEndIndex = text.lastIndexOf(" [/file]");

            if (fileNameEndIndex != -1) {
                fileNameBeginIndex = text.lastIndexOf('/', fileNameEndIndex) + 1;
                fileName = text.substring(fileNameBeginIndex, fileNameEndIndex);
            } else {
                fileNameEndIndex = text.lastIndexOf("[/file]");
                fileNameBeginIndex = text.lastIndexOf('/', fileNameEndIndex) + 1;
                fileName = text.substring(fileNameBeginIndex, fileNameEndIndex);
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

    public String getExtension() {
        if (!isAttachment()) {
            throw new RuntimeException("Current comment is not an attachment");
        }

        return QiscusFileUtil.getExtension(getAttachmentName());
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
            urls = QiscusTextUtil.extractUrl(text);
        }
        return !urls.isEmpty();
    }

    public List<String> getUrls() {
        if (urls == null) {
            urls = QiscusTextUtil.extractUrl(text);
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
                ", text='" + text + '\'' +
                ", sender='" + sender + '\'' +
                ", timestamp=" + timestamp +
                ", status=" + status +
                ", deleted=" + deleted +
                ", appId=" + appId +
                ", payload=" + payload +
                ", extras=" + extras +
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
        dest.writeString(text);
        dest.writeString(appId);
        dest.writeParcelable(sender, flags);
        if (timestamp == null) {
            timestamp = new Date();
        }
        dest.writeLong(timestamp.getTime());
        dest.writeInt(status);
        dest.writeByte((byte) (deleted ? 1 : 0));
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeString(rawType);
        dest.writeParcelable(replyTo, flags);
        dest.writeString(payload);
        if (extras == null) {
            try {
                extras = new JSONObject("{}");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        dest.writeString(extras.toString());

    }

    public boolean areContentsTheSame(QMessage qiscusMessage) {
        return id == qiscusMessage.id
                && uniqueId.equals(qiscusMessage.uniqueId)
                && chatRoomId == qiscusMessage.chatRoomId
                && previousMessageId == qiscusMessage.previousMessageId
                && text.equals(qiscusMessage.text)
                && sender.equals(qiscusMessage.sender)
                && timestamp.equals(qiscusMessage.timestamp)
                && status == qiscusMessage.status
                && deleted == qiscusMessage.deleted
                && selected == qiscusMessage.selected
                && highlighted == qiscusMessage.highlighted
                && appId.equals(qiscusMessage.appId);
    }

    public enum Type {
        TEXT, IMAGE, VIDEO, FILE, AUDIO, LINK, ACCOUNT_LINKING, BUTTONS, REPLY, SYSTEM_EVENT, CARD,
        CONTACT, LOCATION, CAROUSEL, CUSTOM
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

    private void setupPlayer(QiscusCore qiscusCore) {
        if (player == null) {
            File localPath = qiscusCore.getDataStore().getLocalPath(id);
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

    public void playAudio(QiscusCore qiscusCore) {
        if (!isAudio()) {
            throw new RuntimeException("Current comment is not an audio");
        }

        if (observer == null) {
            observer = new MediaObserver();
        }

        setupPlayer(qiscusCore);

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

    public int getAudioDuration(QiscusCore qiscusCore) {
        if (player == null && isAudio()) {
            File localPath = qiscusCore.getDataStore().getLocalPath(id);
            if (localPath == null) {
                return 0;
            } else {
                setupPlayer(qiscusCore);
            }
        }
        return player.getDuration();
    }

    public int getCurrentAudioPosition(QiscusCore qiscusCore) {
        if (player == null && isAudio()) {
            File localPath = qiscusCore.getDataStore().getLocalPath(id);
            if (localPath == null) {
                return 0;
            } else {
                setupPlayer(qiscusCore);
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

    public interface ProgressListener {
        void onProgress(QMessage qiscusComment, int percentage);
    }

    public interface DownloadingListener {
        void onDownloading(QMessage qiscusComment, boolean downloading);
    }

    public interface PlayingAudioListener {
        void onPlayingAudio(QMessage qiscusComment, int currentPosition);

        void onPauseAudio(QMessage qiscusComment);

        void onStopAudio(QMessage qiscusComment);
    }

    public interface LinkPreviewListener {
        void onLinkPreviewReady(QMessage qiscusComment, PreviewData previewData);
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

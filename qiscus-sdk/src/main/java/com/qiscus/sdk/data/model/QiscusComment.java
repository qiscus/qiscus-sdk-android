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

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.webkit.MimeTypeMap;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusFileUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class QiscusComment implements Parcelable {
    public static final int STATE_FAILED = 0;
    public static final int STATE_SENDING = 1;
    public static final int STATE_ON_QISCUS = 2;
    public static final int STATE_ON_PUSHER = 3;

    protected int id;
    protected int roomId;
    protected int topicId;
    protected String uniqueId;
    protected int commentBeforeId;
    protected String message;
    protected String sender;
    protected String senderEmail;
    protected Date time;
    protected int state;
    protected boolean downloading;
    protected int progress;
    protected ProgressListener progressListener;
    protected DownloadingListener downloadingListener;
    protected PlayingAudioListener playingAudioListener;
    private MediaObserver observer;
    private MediaPlayer player;

    public static QiscusComment generateMessage(String content, int roomId, int topicId) {
        QiscusAccount qiscusAccount = Qiscus.getQiscusAccount();
        QiscusComment qiscusComment = new QiscusComment();
        qiscusComment.setId(-1);
        qiscusComment.setRoomId(roomId);
        qiscusComment.setTopicId(topicId);
        qiscusComment.setUniqueId("android_"
                + System.currentTimeMillis()
                + Settings.Secure.getString(Qiscus.getApps().getContentResolver(),
                Settings.Secure.ANDROID_ID));
        qiscusComment.setMessage(content);
        qiscusComment.setTime(new Date());
        qiscusComment.setSenderEmail(qiscusAccount.getEmail());
        qiscusComment.setSender(qiscusAccount.getUsername());
        qiscusComment.setState(STATE_SENDING);

        return qiscusComment;
    }

    public QiscusComment() {

    }

    protected QiscusComment(Parcel in) {
        id = in.readInt();
        roomId = in.readInt();
        topicId = in.readInt();
        uniqueId = in.readString();
        commentBeforeId = in.readInt();
        message = in.readString();
        sender = in.readString();
        senderEmail = in.readString();
        time = new Date(in.readLong());
        state = in.readInt();
    }

    public static final Creator<QiscusComment> CREATOR = new Creator<QiscusComment>() {
        @Override
        public QiscusComment createFromParcel(Parcel in) {
            return new QiscusComment(in);
        }

        @Override
        public QiscusComment[] newArray(int size) {
            return new QiscusComment[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getCommentBeforeId() {
        return commentBeforeId;
    }

    public void setCommentBeforeId(int commentBeforeId) {
        this.commentBeforeId = commentBeforeId;
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

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isAttachment() {
        String trimmedMessage = message.trim();
        return trimmedMessage.startsWith("[file]") && trimmedMessage.endsWith("[/file]");
    }

    public Uri getAttachmentUri() {
        if (!isAttachment()) {
            throw new RuntimeException("Current comment is not an attachment");
        }

        String uriStr = message.replaceAll("\\[file\\]", "").replaceAll("\\[/file\\]", "").trim();
        return Uri.parse(uriStr);
    }

    public String getAttachmentName() {
        if (!isAttachment()) {
            throw new RuntimeException("Current comment is not an attachment");
        }

        int fileNameEndIndex = message.lastIndexOf(" [/file]");
        int fileNameBeginIndex = message.lastIndexOf('/', fileNameEndIndex) + 1;

        String fileName = message.substring(fileNameBeginIndex,
                fileNameEndIndex);
        try {
            return URLDecoder.decode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("The filename '" + fileName
                + "' is not valid UTF-8");
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

    public Type getType() {
        if (!isAttachment()) {
            return Type.TEXT;
        } else if (isImage()) {
            return Type.IMAGE;
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
        if (downloadingListener != null) {
            QiscusAndroidUtil.runOnUIThread(() -> downloadingListener.onDownloading(this, downloading));
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int percentage) {
        this.progress = percentage;
        if (progressListener != null) {
            QiscusAndroidUtil.runOnUIThread(() -> progressListener.onProgress(this, progress));
        }
    }

    private void setupPlayer() {
        if (player == null) {
            File localPath = Qiscus.getDataStore().getLocalPath(id);
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
            File localPath = Qiscus.getDataStore().getLocalPath(id);
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
            File localPath = Qiscus.getDataStore().getLocalPath(id);
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
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof QiscusComment) {
            QiscusComment qiscusComment = (QiscusComment) o;
            if (id == -1) {
                return qiscusComment.uniqueId.equals(uniqueId);
            } else {
                return qiscusComment.id == id || qiscusComment.uniqueId.equals(uniqueId);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "QiscusComment{" +
                "id=" + id +
                ", roomId=" + roomId +
                ", topicId=" + topicId +
                ", uniqueId='" + uniqueId + '\'' +
                ", commentBeforeId=" + commentBeforeId +
                ", message='" + message + '\'' +
                ", sender='" + sender + '\'' +
                ", senderEmail='" + senderEmail + '\'' +
                ", time=" + time +
                ", state=" + state +
                '}';
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(roomId);
        dest.writeInt(topicId);
        dest.writeString(uniqueId);
        dest.writeInt(commentBeforeId);
        dest.writeString(message);
        dest.writeString(sender);
        dest.writeString(senderEmail);
        dest.writeLong(time.getTime());
        dest.writeInt(state);
    }

    public enum Type {
        TEXT, IMAGE, FILE, AUDIO
    }

    public interface ProgressListener {
        void onProgress(QiscusComment qiscusComment, int percentage);
    }

    public interface DownloadingListener {
        void onDownloading(QiscusComment qiscusComment, boolean downloading);
    }

    public interface PlayingAudioListener {
        void onPlayingAudio(QiscusComment qiscusComment, int currentPosition);

        void onPauseAudio(QiscusComment qiscusComment);

        void onStopAudio(QiscusComment qiscusComment);
    }

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        public void stop() {
            stop.set(true);
        }

        public void start() {
            stop.set(false);
        }

        @Override
        public void run() {
            while (!stop.get()) {
                QiscusAndroidUtil.runOnUIThread(() -> {
                    if (playingAudioListener != null) {
                        playingAudioListener.onPlayingAudio(QiscusComment.this, player.getCurrentPosition());
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

package com.qiscus.library.chat.data.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.webkit.MimeTypeMap;

import com.qiscus.library.chat.Qiscus;
import com.qiscus.library.chat.data.local.LocalDataManager;
import com.qiscus.library.chat.util.AndroidUtilities;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class Comment implements Parcelable {
    public static final int STATE_SENDING = 1;
    public static final int STATE_ON_QISCUS = 2;
    public static final int STATE_ON_PUSHER = 3;
    public static final int STATE_FAILED = 4;

    private int id;
    private int roomId;
    private int topicId;
    private String uniqueId;
    private int commentBeforeId;
    private String message;
    private String sender;
    private String senderEmail;
    private Date time;
    private int state;
    private boolean downloading;
    private int progress;
    private ProgressListener progressListener;
    private DownloadingListener downloadingListener;

    public static Comment generateMessage(String content, int roomId, int topicId) {
        AccountInfo accountInfo = LocalDataManager.getInstance().getAccountInfo();
        Comment comment = new Comment();
        comment.setId(-1);
        comment.setRoomId(roomId);
        comment.setTopicId(topicId);
        comment.setUniqueId("android_"
                                    + System.currentTimeMillis()
                                    + Settings.Secure.getString(Qiscus.getApps().getContentResolver(),
                                                                Settings.Secure.ANDROID_ID));
        comment.setMessage(content);
        comment.setTime(new Date());
        comment.setSenderEmail(accountInfo.getEmail());
        comment.setSender(accountInfo.getFullname());
        comment.setState(STATE_SENDING);

        return comment;
    }

    public Comment() {

    }

    protected Comment(Parcel in) {
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

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
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

        String ext = MimeTypeMap.getFileExtensionFromUrl(getAttachmentUri().getPath());
        ext = ext.replace("_", "");
        ext = ext.toLowerCase();

        return ext;
    }

    public boolean isImage() {
        if (isAttachment()) {
            String path = getAttachmentUri().getPath();
            int lastDotPosition = path.lastIndexOf(".");
            String ext = path.substring(lastDotPosition + 1);
            ext = ext.replace("_", "");
            ext = ext.toLowerCase();
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            if (type == null) {
                return false;
            } else if (type.contains("image")) {
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
            AndroidUtilities.runOnUIThread(() -> downloadingListener.onDownloading(this, downloading));
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int percentage) {
        this.progress = percentage;
        if (progressListener != null) {
            AndroidUtilities.runOnUIThread(() -> progressListener.onProgress(this, progress));
        }
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void setDownloadingListener(DownloadingListener downloadingListener) {
        this.downloadingListener = downloadingListener;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Comment) {
            Comment comment = (Comment) o;
            if (id == -1) {
                return comment.uniqueId.equals(uniqueId);
            } else {
                return comment.id == id || comment.uniqueId.equals(uniqueId);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Comment{" +
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
        TEXT, IMAGE, FILE
    }

    public interface ProgressListener {
        void onProgress(Comment comment, int percentage);
    }

    public interface DownloadingListener {
        void onDownloading(Comment comment, boolean downloading);
    }
}

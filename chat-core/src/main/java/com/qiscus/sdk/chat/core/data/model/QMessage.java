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

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.qiscus.sdk.chat.core.util.QiscusConst;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;
import com.qiscus.sdk.chat.core.util.QiscusRawDataExtractor;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;
import com.schinizer.rxunfurl.model.PreviewData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

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
    private JSONObject payload;
    private JSONObject extras;
    private String appId;
    private QMessage replyTo;
    private String attachmentName;
    protected boolean selected;
    protected boolean highlighted;

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
        rawType = in.readString();
        selected = in.readByte() != 0;

        replyTo = in.readParcelable(QMessage.class.getClassLoader());
        try {
            payload = new JSONObject(in.readString());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        qiscusMessage.setPayload(json);
        return qiscusMessage;
    }

    public static QMessage generateReplyMessage(long roomId, String content, QMessage repliedComment) {
        QMessage qiscusMessage = generateMessage(roomId, content);
        qiscusMessage.setReplyTo(repliedComment);
        qiscusMessage.setRawType("reply");
        JSONObject json = new JSONObject();
        try {
            json.put("text", qiscusMessage.getText())
                    .put("replied_comment_id", repliedComment.getId())
                    .put("replied_comment_message", repliedComment.getText())
                    .put("replied_comment_sender_username", repliedComment.getSender().getName())
                    .put("replied_comment_sender_email", repliedComment.getSender().getId())
                    .put("replied_comment_type", repliedComment.getRawType())
                    .put("replied_comment_payload", repliedComment.getPayload().toString());
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
        setText(String.format("[file] %s [/file]", url));
        try {
            JSONObject json = getPayload();
            json.put("url", url);
            setPayload(json);
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

    public Type getType() {
        if (!TextUtils.isEmpty(rawType) && rawType.equals("buttons")) {
            return Type.BUTTONS;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("reply")) {
            return Type.REPLY;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("card")) {
            return Type.CARD;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("system_event")) {
            return Type.SYSTEM_EVENT;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("carousel")) {
            return Type.CAROUSEL;
        } else if (!TextUtils.isEmpty(rawType) && rawType.equals("custom")) {
            return Type.CUSTOM;
        } else if (!isAttachment()) {
            return Type.TEXT;
        } else if (isImage()) {
            return Type.IMAGE;
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
        TEXT, IMAGE, FILE, BUTTONS, REPLY, SYSTEM_EVENT, CARD,
        CAROUSEL, CUSTOM
    }

    public interface ProgressListener {
        void onProgress(QMessage qiscusMessage, int percentage);
    }

    public interface DownloadingListener {
        void onDownloading(QMessage qiscusMessage, boolean downloading);
    }

    public interface LinkPreviewListener {
        void onLinkPreviewReady(QMessage qiscusMessage, PreviewData previewData);
    }
}

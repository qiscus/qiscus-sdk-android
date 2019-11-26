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

import android.os.Parcel;
import android.os.Parcelable;

import com.qiscus.sdk.chat.core.util.QiscusNumberUtil;

public class QiscusPushNotificationMessage implements Parcelable {
    public static final Creator<QiscusPushNotificationMessage> CREATOR = new Creator<QiscusPushNotificationMessage>() {
        @Override
        public QiscusPushNotificationMessage createFromParcel(Parcel in) {
            return new QiscusPushNotificationMessage(in);
        }

        @Override
        public QiscusPushNotificationMessage[] newArray(int size) {
            return new QiscusPushNotificationMessage[size];
        }
    };
    private long commentId;
    private String message;
    private String roomName;
    private String roomAvatar;

    public QiscusPushNotificationMessage(long commentId, String message) {
        this.commentId = commentId;
        this.message = message;
    }

    public QiscusPushNotificationMessage(QMessage qiscusMessage) {
        this.commentId = qiscusMessage.id;
        this.message = qiscusMessage.message;
        this.roomName = qiscusMessage.roomName;
        this.roomAvatar = qiscusMessage.roomAvatar;
    }

    protected QiscusPushNotificationMessage(Parcel in) {
        commentId = in.readLong();
        message = in.readString();
        roomName = in.readString();
        roomAvatar = in.readString();
    }

    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomAvatar() {
        return roomAvatar;
    }

    public void setRoomAvatar(String roomAvatar) {
        this.roomAvatar = roomAvatar;
    }

    @Override
    public int hashCode() {
        int result = QiscusNumberUtil.convertToInt(commentId);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QiscusPushNotificationMessage && commentId == ((QiscusPushNotificationMessage) o).commentId;
    }

    @Override
    public String toString() {
        return "QiscusPushNotificationMessage{" +
                "commentId=" + commentId +
                ", message='" + message + '\'' +
                ", roomName='" + roomName + '\'' +
                ", roomAvatar='" + roomAvatar + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(commentId);
        dest.writeString(message);
        dest.writeString(roomName);
        dest.writeString(roomAvatar);
    }
}

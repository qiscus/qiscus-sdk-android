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

import android.os.Parcel;
import android.os.Parcelable;

public class QiscusPushNotificationMessage implements Parcelable {
    private String commentId;
    private String message;

    public QiscusPushNotificationMessage(String commentId, String message) {
        this.commentId = commentId;
        this.message = message;
    }

    public QiscusPushNotificationMessage(QiscusComment qiscusComment) {
        this.commentId = qiscusComment.id;
        this.message = qiscusComment.message;
    }

    protected QiscusPushNotificationMessage(Parcel in) {
        commentId = in.readString();
        message = in.readString();
    }

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

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QiscusPushNotificationMessage that = (QiscusPushNotificationMessage) o;

        return commentId.equals(that.commentId);

    }

    @Override
    public int hashCode() {
        return commentId.hashCode();
    }

    @Override
    public String toString() {
        return "QiscusPushNotificationMessage{" +
                "commentId=" + commentId +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(commentId);
        dest.writeString(message);
    }
}

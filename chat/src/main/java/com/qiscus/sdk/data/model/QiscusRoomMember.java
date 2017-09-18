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
import android.support.annotation.NonNull;

import com.qiscus.manggil.mention.Mentionable;

public class QiscusRoomMember implements Parcelable, Mentionable {
    private String email;
    private String username;
    private String avatar;
    private int lastDeliveredCommentId;
    private int lastReadCommentId;

    public QiscusRoomMember() {

    }

    protected QiscusRoomMember(Parcel in) {
        email = in.readString();
        username = in.readString();
        avatar = in.readString();
        lastDeliveredCommentId = in.readInt();
        lastReadCommentId = in.readInt();
    }

    public static final Creator<QiscusRoomMember> CREATOR = new Creator<QiscusRoomMember>() {
        @Override
        public QiscusRoomMember createFromParcel(Parcel in) {
            return new QiscusRoomMember(in);
        }

        @Override
        public QiscusRoomMember[] newArray(int size) {
            return new QiscusRoomMember[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getLastDeliveredCommentId() {
        return lastDeliveredCommentId;
    }

    public void setLastDeliveredCommentId(int lastDeliveredCommentId) {
        this.lastDeliveredCommentId = lastDeliveredCommentId;
    }

    public int getLastReadCommentId() {
        return lastReadCommentId;
    }

    public void setLastReadCommentId(int lastReadCommentId) {
        this.lastReadCommentId = lastReadCommentId;
    }

    @Override
    public String toString() {
        return "QiscusRoomMember{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                ", lastDeliveredCommentId=" + lastDeliveredCommentId +
                ", lastReadCommentId=" + lastReadCommentId +
                '}';
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QiscusRoomMember && email.equalsIgnoreCase(((QiscusRoomMember) o).email);
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(username);
        dest.writeString(avatar);
        dest.writeInt(lastDeliveredCommentId);
        dest.writeInt(lastReadCommentId);
    }

    @NonNull
    @Override
    public String getTextForDisplayMode(MentionDisplayMode mode) {
        return "@" + username;
    }

    @Override
    public String getTextForEncodeMode() {
        return "@[" + email + "]";
    }

    @Override
    public MentionDeleteStyle getDeleteStyle() {
        return MentionDeleteStyle.FULL_DELETE;
    }

    @Override
    public int getSuggestibleId() {
        return email.hashCode();
    }

    @Override
    public String getSuggestiblePrimaryText() {
        return username;
    }
}

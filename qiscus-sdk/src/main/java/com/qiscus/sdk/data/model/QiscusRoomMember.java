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

public class QiscusRoomMember implements Parcelable{
    private String email;
    private String username;
    private String avatar;

    public QiscusRoomMember() {

    }

    protected QiscusRoomMember(Parcel in) {
        email = in.readString();
        username = in.readString();
        avatar = in.readString();
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

    @Override
    public String toString() {
        return "QiscusRoomMember{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
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
    }
}

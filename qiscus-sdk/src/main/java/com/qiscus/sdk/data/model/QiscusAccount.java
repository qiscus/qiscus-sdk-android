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

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class QiscusAccount implements Parcelable {
    protected int id;
    protected String email;
    protected String avatar;
    protected String token;
    protected String username;
    protected String rtKey;

    public QiscusAccount() {

    }

    protected QiscusAccount(Parcel in) {
        id = in.readInt();
        email = in.readString();
        avatar = in.readString();
        token = in.readString();
        username = in.readString();
        rtKey = in.readString();
    }

    public static final Creator<QiscusAccount> CREATOR = new Creator<QiscusAccount>() {
        @Override
        public QiscusAccount createFromParcel(Parcel in) {
            return new QiscusAccount(in);
        }

        @Override
        public QiscusAccount[] newArray(int size) {
            return new QiscusAccount[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRtKey() {
        return rtKey;
    }

    public void setRtKey(String rtKey) {
        this.rtKey = rtKey;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(email);
        dest.writeString(avatar);
        dest.writeString(token);
        dest.writeString(username);
        dest.writeString(rtKey);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QiscusAccount && id == (((QiscusAccount) o).id);
    }

    @Override
    public String toString() {
        return "QiscusAccount{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", token='" + token + '\'' +
                ", username='" + username + '\'' +
                ", rtKey='" + rtKey + '\'' +
                '}';
    }
}

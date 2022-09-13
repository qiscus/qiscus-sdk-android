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

import org.json.JSONObject;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusAccount implements Parcelable {

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
    protected int id;
    protected String email;
    protected String avatar;
    protected String token;
    protected String refreshToken;
    protected String username;
    protected JSONObject extras;

    public QiscusAccount() {

    }

    protected QiscusAccount(Parcel in) {
        id = in.readInt();
        email = in.readString();
        avatar = in.readString();
        token = in.readString();
        refreshToken = in.readString();
        username = in.readString();
        try {
            extras = new JSONObject(in.readString());
        } catch (Exception ignored) {
            //Do nothing
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email == null ? email = "" : email;
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
        return token == null ? token = "" : token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken == null ? refreshToken = "" : refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public JSONObject getExtras() {
        return extras;
    }

    public void setExtras(JSONObject extras) {
        this.extras = extras;
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
        dest.writeString(refreshToken);
        dest.writeString(username);
        if (extras == null) {
            extras = new JSONObject();
        }
        dest.writeString(extras.toString());
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (refreshToken != null ? refreshToken.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        return result;
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
                ", refresh_token='" + refreshToken + '\'' +
                ", username='" + username + '\'' +
                ", extras=" + extras +
                '}';
    }
}

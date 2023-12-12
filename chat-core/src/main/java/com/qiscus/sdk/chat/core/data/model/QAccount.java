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
public class QAccount implements Parcelable {

    public static final Creator<QAccount> CREATOR = new Creator<QAccount>() {
        @Override
        public QAccount createFromParcel(Parcel in) {
            return new QAccount(in);
        }

        @Override
        public QAccount[] newArray(int size) {
            return new QAccount[size];
        }
    };
    protected String id;
    protected String avatarUrl;
    protected String token;
    protected String name;
    protected JSONObject extras;
    protected Long lastMessageId;
    protected Long lastSyncEventId;

    protected String refreshToken;
    protected String tokenExpiresAt;



    public QAccount() {

    }

    protected QAccount(Parcel in) {
        id = in.readString();
        avatarUrl = in.readString();
        token = in.readString();
        name = in.readString();
        lastSyncEventId = in.readLong();
        lastMessageId = in.readLong();

        refreshToken = in.readString();
        tokenExpiresAt = in.readString();
        try {
            extras = new JSONObject(in.readString());
        } catch (Exception ignored) {
            //Do nothing
        }
    }

    public String getId() {
        return id == null ? id = "" : id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

    public String getTokenExpiresAt() {
        return tokenExpiresAt == null ? tokenExpiresAt = "" : tokenExpiresAt;
    }

    public void setTokenExpiresAt(String tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getExtras() {
        return extras;
    }

    public void setExtras(JSONObject extras) {
        this.extras = extras;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public Long getLastSyncEventId() {
        return lastSyncEventId;
    }

    public void setLastSyncEventId(Long lastSyncEventId) {
        this.lastSyncEventId = lastSyncEventId;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(avatarUrl);
        dest.writeString(token);
        dest.writeString(name);
        dest.writeLong(lastSyncEventId);
        dest.writeLong(lastMessageId);
        dest.writeString(refreshToken);
        dest.writeString(tokenExpiresAt);
        if (extras == null) {
            extras = new JSONObject();
        }
        dest.writeString(extras.toString());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof QAccount && id == (((QAccount) o).id);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (avatarUrl != null ? avatarUrl.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (extras != null ? extras.hashCode() : 0);
        result = 31 * result + (refreshToken != null ? refreshToken.hashCode() : 0);
        result = 31 * result + (tokenExpiresAt != null ? tokenExpiresAt.hashCode() : 0);
        result = 31 * result + (lastMessageId != null ? lastMessageId.hashCode() : 0);
        result = 31 * result + (lastSyncEventId != null ? lastSyncEventId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QAccount{" +
                "id=" + id +
                ", avatar='" + avatarUrl + '\'' +
                ", token='" + token + '\'' +
                ", refresh_token='" + refreshToken + '\'' +
                ", token_expires_at='" + tokenExpiresAt + '\'' +
                ", name='" + name + '\'' +
                ", extras=" + extras + '\'' +
                ", lastMessageId=" + lastMessageId + '\'' +
                ", lastSyncEventId=" + lastSyncEventId +
                '}';
    }
}

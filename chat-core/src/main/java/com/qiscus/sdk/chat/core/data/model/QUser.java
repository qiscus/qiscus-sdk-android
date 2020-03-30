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
public class QUser implements Parcelable {

    public static final Creator<QUser> CREATOR = new Creator<QUser>() {
        @Override
        public QUser createFromParcel(Parcel in) {
            return new QUser(in);
        }

        @Override
        public QUser[] newArray(int size) {
            return new QUser[size];
        }
    };
    protected String id;
    protected String avatarUrl;
    protected String name;
    protected JSONObject extras;

    public QUser() {

    }

    protected QUser(Parcel in) {
        id = in.readString();
        avatarUrl = in.readString();
        name = in.readString();
        try {
            extras = new JSONObject(in.readString());
        } catch (Exception ignored) {
            //Do nothing
        }
    }

    public String getId() {
        return id;
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

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(avatarUrl);
        dest.writeString(name);
        if (extras == null) {
            extras = new JSONObject();
        }
        dest.writeString(extras.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QUser qUser = (QUser) o;

        if (id != null ? !id.equals(qUser.id) : qUser.id != null) return false;
        if (avatarUrl != null ? !avatarUrl.equals(qUser.avatarUrl) : qUser.avatarUrl != null)
            return false;
        if (name != null ? !name.equals(qUser.name) : qUser.name != null) return false;
        return extras != null ? extras.equals(qUser.extras) : qUser.extras == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (avatarUrl != null ? avatarUrl.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (extras != null ? extras.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QAccount{" +
                "id=" + id +
                ", avatar='" + avatarUrl + '\'' +
                ", name='" + name + '\'' +
                ", extras=" + extras +
                '}';
    }
}

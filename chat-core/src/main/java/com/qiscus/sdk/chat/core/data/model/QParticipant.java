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
import androidx.annotation.NonNull;

import com.qiscus.manggil.mention.Mentionable;

import org.json.JSONObject;

public class QParticipant implements Parcelable, Mentionable {
    public static final Creator<QParticipant> CREATOR = new Creator<QParticipant>() {
        @Override
        public QParticipant createFromParcel(Parcel in) {
            return new QParticipant(in);
        }

        @Override
        public QParticipant[] newArray(int size) {
            return new QParticipant[size];
        }
    };
    private String id;
    private String name;
    private String avatarUrl;
    private long lastMessageDeliveredId;
    private long lastMessageReadId;
    private JSONObject extras;

    public QParticipant() {

    }

    protected QParticipant(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatarUrl = in.readString();
        lastMessageDeliveredId = in.readLong();
        lastMessageReadId = in.readLong();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public long getLastMessageDeliveredId() {
        return lastMessageDeliveredId;
    }

    public void setLastMessageDeliveredId(long lastMessageDeliveredId) {
        this.lastMessageDeliveredId = lastMessageDeliveredId;
    }

    public long getLastMessageReadId() {
        return lastMessageReadId;
    }

    public void setLastMessageReadId(long lastMessageReadId) {
        this.lastMessageReadId = lastMessageReadId;
    }

    @Override
    public String toString() {
        return "QParticipant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", lastMessageDeliveredId=" + lastMessageDeliveredId +
                ", lastMessageReadId=" + lastMessageReadId +
                ", extras=" + extras +
                '}';
    }

    public JSONObject getExtras() {
        return extras;
    }

    public void setExtras(JSONObject extras) {
        this.extras = extras;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QParticipant && id.equalsIgnoreCase(((QParticipant) o).id);
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatarUrl);
        dest.writeLong(lastMessageDeliveredId);
        dest.writeLong(lastMessageReadId);
        if (extras == null) {
            extras = new JSONObject();
        }
        dest.writeString(extras.toString());
    }

    @NonNull
    @Override
    public String getTextForDisplayMode(MentionDisplayMode mode) {
        return "@" + name;
    }

    @Override
    public String getTextForEncodeMode() {
        return "@[" + id + "]";
    }

    @Override
    public MentionDeleteStyle getDeleteStyle() {
        return MentionDeleteStyle.FULL_DELETE;
    }

    @Override
    public int getSuggestibleId() {
        return id.hashCode();
    }

    @Override
    public String getSuggestiblePrimaryText() {
        return name;
    }
}

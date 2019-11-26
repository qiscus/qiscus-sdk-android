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

import org.json.JSONObject;

import java.util.List;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QChatRoom implements Parcelable {
    public static final Creator<QChatRoom> CREATOR = new Creator<QChatRoom>() {
        @Override
        public QChatRoom createFromParcel(Parcel in) {
            return new QChatRoom(in);
        }

        @Override
        public QChatRoom[] newArray(int size) {
            return new QChatRoom[size];
        }
    };
    protected long id;
    protected String distinctId;
    protected String uniqueId;
    protected String name;
    protected JSONObject extras;
    protected String type;
    protected String avatarUrl;
    protected List<QParticipant> participants;
    protected int unreadCount;
    protected QMessage lastMessage;
    protected int totalParticipants;

    public QChatRoom() {

    }

    protected QChatRoom(Parcel in) {
        id = in.readLong();
        distinctId = in.readString();
        uniqueId = in.readString();
        name = in.readString();
        try {
            extras = new JSONObject(in.readString());
        } catch (Exception ignored) {
            //Do nothing
        }
        type       = in.readString();
        avatarUrl = in.readString();
        participants = in.createTypedArrayList(QParticipant.CREATOR);
        unreadCount = in.readInt();
        lastMessage = in.readParcelable(QMessage.class.getClassLoader());
        totalParticipants = in.readInt();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDistinctId() {
        return distinctId;
    }

    public void setDistinctId(String distinctId) {
        this.distinctId = distinctId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<QParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<QParticipant> participants) {
        this.participants = participants;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public QMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(QMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(int totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    @Override
    public int hashCode() {
        return QiscusNumberUtil.convertToInt(id);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QChatRoom && id == ((QChatRoom) o).id;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(distinctId);
        dest.writeString(uniqueId);
        dest.writeString(name);
        if (extras == null) {
            extras = new JSONObject();
        }
        dest.writeString(extras.toString());
        dest.writeString(type);
        dest.writeString(avatarUrl);
        dest.writeTypedList(participants);
        dest.writeInt(unreadCount);
        dest.writeParcelable(lastMessage, flags);
        dest.writeInt(totalParticipants);
    }

    @Override
    public String toString() {
        return "QChatRoom{" +
                "id=" + id +
                ", distinctId='" + distinctId + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", name='" + name + '\'' +
                ", extras=" + extras +
                ", type=" + type +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", participants=" + participants +
                ", unreadCount=" + unreadCount +
                ", lastMessage=" + lastMessage +
                ", totalParticipants=" + totalParticipants +
                '}';
    }
}

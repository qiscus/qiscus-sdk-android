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

import org.json.JSONObject;

import java.util.List;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusChatRoom implements Parcelable {
    protected int id;
    protected String distinctId;
    protected String name;
    @Deprecated protected String subtitle = "";
    protected int lastTopicId;
    protected JSONObject options;
    protected boolean group;
    protected String avatarUrl;
    protected List<QiscusRoomMember> member;
    protected int unreadCount;
    protected QiscusComment lastComment;


    public QiscusChatRoom() {

    }


    protected QiscusChatRoom(Parcel in) {
        id = in.readInt();
        distinctId = in.readString();
        name = in.readString();
        subtitle = in.readString();
        lastTopicId = in.readInt();
        try {
            options = new JSONObject(in.readString());
        } catch (Exception ignored) {
            //Do nothing
        }
        group = in.readByte() != 0;
        avatarUrl = in.readString();
        member = in.createTypedArrayList(QiscusRoomMember.CREATOR);
        unreadCount = in.readInt();
        lastComment = in.readParcelable(QiscusComment.class.getClassLoader());
    }

    public static final Creator<QiscusChatRoom> CREATOR = new Creator<QiscusChatRoom>() {
        @Override
        public QiscusChatRoom createFromParcel(Parcel in) {
            return new QiscusChatRoom(in);
        }

        @Override
        public QiscusChatRoom[] newArray(int size) {
            return new QiscusChatRoom[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDistinctId() {
        return distinctId;
    }

    public void setDistinctId(String distinctId) {
        this.distinctId = distinctId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Deprecated
    public String getSubtitle() {
        return subtitle;
    }

    @Deprecated
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getLastTopicId() {
        return lastTopicId;
    }

    public void setLastTopicId(int lastTopicId) {
        this.lastTopicId = lastTopicId;
    }

    public JSONObject getOptions() {
        return options;
    }

    public void setOptions(JSONObject options) {
        this.options = options;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<QiscusRoomMember> getMember() {
        return member;
    }

    public void setMember(List<QiscusRoomMember> member) {
        this.member = member;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public QiscusComment getLastComment() {
        return lastComment;
    }

    public void setLastComment(QiscusComment lastComment) {
        this.lastComment = lastComment;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QiscusChatRoom && id == ((QiscusChatRoom) o).id;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(distinctId);
        dest.writeString(name);
        dest.writeString(subtitle);
        dest.writeInt(lastTopicId);
        if (options == null) {
            options = new JSONObject();
        }
        dest.writeString(options.toString());
        dest.writeByte((byte) (group ? 1 : 0));
        dest.writeString(avatarUrl);
        dest.writeTypedList(member);
        dest.writeInt(unreadCount);
        dest.writeParcelable(lastComment, flags);
    }

    @Override
    public String toString() {
        return "QiscusChatRoom{" +
                "id=" + id +
                ", distinctId='" + distinctId + '\'' +
                ", name='" + name + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", lastTopicId=" + lastTopicId +
                ", options='" + options + '\'' +
                ", group=" + group +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", member=" + member +
                ", unreadCount=" + unreadCount +
                ", lastComment=" + lastComment +
                '}';
    }
}

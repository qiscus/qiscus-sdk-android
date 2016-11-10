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

import java.util.Date;
import java.util.List;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class QiscusChatRoom implements Parcelable {
    protected int id;
    protected String distinctId;
    protected String name;
    protected String subtitle = "";
    protected int lastCommentId;
    protected String lastCommentMessage;
    protected String lastCommentSender;
    protected String lastCommentSenderEmail;
    protected Date lastCommentTime;
    protected int lastTopicId;
    protected String options;
    protected boolean group;
    protected List<String> member;

    public QiscusChatRoom() {
        lastCommentTime = new Date(0L);
    }

    protected QiscusChatRoom(Parcel in) {
        id = in.readInt();
        distinctId = in.readString();
        name = in.readString();
        subtitle = in.readString();
        lastCommentId = in.readInt();
        lastCommentMessage = in.readString();
        lastCommentSender = in.readString();
        lastCommentSenderEmail = in.readString();
        lastCommentTime = new Date(in.readLong());
        lastTopicId = in.readInt();
        options = in.readString();
        group = in.readByte() != 0;
        member = in.createStringArrayList();
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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getLastCommentId() {
        return lastCommentId;
    }

    public void setLastCommentId(int lastCommentId) {
        this.lastCommentId = lastCommentId;
    }

    public String getLastCommentMessage() {
        return lastCommentMessage;
    }

    public void setLastCommentMessage(String lastCommentMessage) {
        this.lastCommentMessage = lastCommentMessage;
    }

    public String getLastCommentSender() {
        return lastCommentSender;
    }

    public void setLastCommentSender(String lastCommentSender) {
        this.lastCommentSender = lastCommentSender;
    }

    public String getLastCommentSenderEmail() {
        return lastCommentSenderEmail;
    }

    public void setLastCommentSenderEmail(String lastCommentSenderEmail) {
        this.lastCommentSenderEmail = lastCommentSenderEmail;
    }

    public Date getLastCommentTime() {
        return lastCommentTime;
    }

    public void setLastCommentTime(Date lastCommentTime) {
        this.lastCommentTime = lastCommentTime;
    }

    public int getLastTopicId() {
        return lastTopicId;
    }

    public void setLastTopicId(int lastTopicId) {
        this.lastTopicId = lastTopicId;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public List<String> getMember() {
        return member;
    }

    public void setMember(List<String> member) {
        this.member = member;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QiscusChatRoom && id == ((QiscusChatRoom) o).id;
    }

    @Override
    public String toString() {
        return "QiscusChatRoom{" +
                "id=" + id +
                ", distinctId='" + distinctId + '\'' +
                ", name='" + name + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", lastCommentId=" + lastCommentId +
                ", lastCommentMessage='" + lastCommentMessage + '\'' +
                ", lastCommentSender='" + lastCommentSender + '\'' +
                ", lastCommentSenderEmail='" + lastCommentSenderEmail + '\'' +
                ", lastCommentTime=" + lastCommentTime +
                ", lastTopicId=" + lastTopicId +
                ", options='" + options + '\'' +
                ", group=" + group +
                ", member=" + member +
                '}';
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
        dest.writeInt(lastCommentId);
        dest.writeString(lastCommentMessage);
        dest.writeString(lastCommentSender);
        dest.writeString(lastCommentSenderEmail);
        dest.writeLong(lastCommentTime.getTime());
        dest.writeInt(lastTopicId);
        dest.writeString(options);
        dest.writeByte((byte) (group ? 1 : 0));
        dest.writeStringList(member);
    }
}

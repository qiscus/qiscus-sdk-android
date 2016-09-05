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
public class QiscusChatRoom implements Parcelable {
    protected int id;
    protected String name;
    protected String codeEn;
    protected int lastCommentId;
    protected int lastTopicId;

    public QiscusChatRoom() {

    }

    protected QiscusChatRoom(Parcel in) {
        id = in.readInt();
        name = in.readString();
        codeEn = in.readString();
        lastCommentId = in.readInt();
        lastTopicId = in.readInt();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeEn() {
        return codeEn;
    }

    public void setCodeEn(String codeEn) {
        this.codeEn = codeEn;
    }

    public int getLastCommentId() {
        return lastCommentId;
    }

    public void setLastCommentId(int lastCommentId) {
        this.lastCommentId = lastCommentId;
    }

    public int getLastTopicId() {
        return lastTopicId;
    }

    public void setLastTopicId(int lastTopicId) {
        this.lastTopicId = lastTopicId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QiscusChatRoom && id == ((QiscusChatRoom) o).id;
    }

    @Override
    public String toString() {
        return "QiscusChatRoom{" +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", codeEn='" + codeEn + '\'' +
                ", lastCommentId=" + lastCommentId +
                ", lastTopicId=" + lastTopicId +
                '}';
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(codeEn);
        dest.writeInt(lastCommentId);
        dest.writeInt(lastTopicId);
    }
}

package com.qiscus.library.chat.data.model;

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
public class ChatRoom implements Parcelable {
    private String roomType;
    private int id;
    private String name;
    private String code;
    private String codeEn;
    private int lastCommentId;
    private String lastMessage;
    private String lastCommentMessage;
    private int lastTopicId;
    private int lastCommentTopicId;
    private String lastTopicTitle;
    private String lastCommentTopicTitle;
    private int countNotif;
    private int countUnread;
    private String urlSecretCode;
    private String sender;
    private String secretCode;
    private int secretCodeEnabled;
    private double rate;
    private boolean archived;
    private boolean isAdmin;
    private int interlocutorId;
    private String interlocutorName;
    private String interlocutorEmail;
    private String interlocutorAvatar;
    private String interlocutorGender;

    public ChatRoom() {

    }

    protected ChatRoom(Parcel in) {
        roomType = in.readString();
        id = in.readInt();
        name = in.readString();
        code = in.readString();
        codeEn = in.readString();
        lastCommentId = in.readInt();
        lastMessage = in.readString();
        lastCommentMessage = in.readString();
        lastTopicId = in.readInt();
        lastCommentTopicId = in.readInt();
        lastTopicTitle = in.readString();
        lastCommentTopicTitle = in.readString();
        countNotif = in.readInt();
        countUnread = in.readInt();
        urlSecretCode = in.readString();
        sender = in.readString();
        secretCode = in.readString();
        secretCodeEnabled = in.readInt();
        rate = in.readDouble();
        archived = in.readByte() != 0;
        isAdmin = in.readByte() != 0;
        interlocutorId = in.readInt();
        interlocutorName = in.readString();
        interlocutorEmail = in.readString();
        interlocutorAvatar = in.readString();
        interlocutorGender = in.readString();
    }

    public static final Creator<ChatRoom> CREATOR = new Creator<ChatRoom>() {
        @Override
        public ChatRoom createFromParcel(Parcel in) {
            return new ChatRoom(in);
        }

        @Override
        public ChatRoom[] newArray(int size) {
            return new ChatRoom[size];
        }
    };

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastCommentMessage() {
        return lastCommentMessage;
    }

    public void setLastCommentMessage(String lastCommentMessage) {
        this.lastCommentMessage = lastCommentMessage;
    }

    public int getLastTopicId() {
        return lastTopicId;
    }

    public void setLastTopicId(int lastTopicId) {
        this.lastTopicId = lastTopicId;
    }

    public int getLastCommentTopicId() {
        return lastCommentTopicId;
    }

    public void setLastCommentTopicId(int lastCommentTopicId) {
        this.lastCommentTopicId = lastCommentTopicId;
    }

    public String getLastTopicTitle() {
        return lastTopicTitle;
    }

    public void setLastTopicTitle(String lastTopicTitle) {
        this.lastTopicTitle = lastTopicTitle;
    }

    public String getLastCommentTopicTitle() {
        return lastCommentTopicTitle;
    }

    public void setLastCommentTopicTitle(String lastCommentTopicTitle) {
        this.lastCommentTopicTitle = lastCommentTopicTitle;
    }

    public int getCountNotif() {
        return countNotif;
    }

    public void setCountNotif(int countNotif) {
        this.countNotif = countNotif;
    }

    public int getCountUnread() {
        return countUnread;
    }

    public void setCountUnread(int countUnread) {
        this.countUnread = countUnread;
    }

    public String getUrlSecretCode() {
        return urlSecretCode;
    }

    public void setUrlSecretCode(String urlSecretCode) {
        this.urlSecretCode = urlSecretCode;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public boolean isSecretCodeEnabled() {
        return secretCodeEnabled == 1;
    }

    public void setSecretCodeEnabled(int secretCodeEnabled) {
        this.secretCodeEnabled = secretCodeEnabled;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public int getInterlocutorId() {
        return interlocutorId;
    }

    public void setInterlocutorId(int interlocutorId) {
        this.interlocutorId = interlocutorId;
    }

    public String getInterlocutorName() {
        return interlocutorName;
    }

    public void setInterlocutorName(String interlocutorName) {
        this.interlocutorName = interlocutorName;
    }

    public String getInterlocutorEmail() {
        return interlocutorEmail;
    }

    public void setInterlocutorEmail(String interlocutorEmail) {
        this.interlocutorEmail = interlocutorEmail;
    }

    public String getInterlocutorAvatar() {
        return interlocutorAvatar;
    }

    public void setInterlocutorAvatar(String interlocutorAvatar) {
        this.interlocutorAvatar = interlocutorAvatar;
    }

    public String getInterlocutorGender() {
        return interlocutorGender;
    }

    public void setInterlocutorGender(String interlocutorGender) {
        this.interlocutorGender = interlocutorGender;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ChatRoom && id == ((ChatRoom) o).id;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "roomType='" + roomType + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", codeEn='" + codeEn + '\'' +
                ", lastCommentId=" + lastCommentId +
                ", lastMessage='" + lastMessage + '\'' +
                ", lastCommentMessage='" + lastCommentMessage + '\'' +
                ", lastTopicId=" + lastTopicId +
                ", lastCommentTopicId=" + lastCommentTopicId +
                ", lastTopicTitle='" + lastTopicTitle + '\'' +
                ", lastCommentTopicTitle='" + lastCommentTopicTitle + '\'' +
                ", countNotif=" + countNotif +
                ", countUnread=" + countUnread +
                ", urlSecretCode='" + urlSecretCode + '\'' +
                ", sender='" + sender + '\'' +
                ", secretCode='" + secretCode + '\'' +
                ", secretCodeEnabled=" + secretCodeEnabled +
                ", rate=" + rate +
                ", archived=" + archived +
                ", isAdmin=" + isAdmin +
                ", interlocutorId=" + interlocutorId +
                ", interlocutorName='" + interlocutorName + '\'' +
                ", interlocutorEmail='" + interlocutorEmail + '\'' +
                ", interlocutorAvatar='" + interlocutorAvatar + '\'' +
                ", interlocutorGender='" + interlocutorGender + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomType);
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(code);
        dest.writeString(codeEn);
        dest.writeInt(lastCommentId);
        dest.writeString(lastMessage);
        dest.writeString(lastCommentMessage);
        dest.writeInt(lastTopicId);
        dest.writeInt(lastCommentTopicId);
        dest.writeString(lastTopicTitle);
        dest.writeString(lastCommentTopicTitle);
        dest.writeInt(countNotif);
        dest.writeInt(countUnread);
        dest.writeString(urlSecretCode);
        dest.writeString(sender);
        dest.writeString(secretCode);
        dest.writeInt(secretCodeEnabled);
        dest.writeDouble(rate);
        dest.writeByte((byte) (archived ? 1 : 0));
        dest.writeByte((byte) (isAdmin ? 1 : 0));
        dest.writeInt(interlocutorId);
        dest.writeString(interlocutorName);
        dest.writeString(interlocutorEmail);
        dest.writeString(interlocutorAvatar);
        dest.writeString(interlocutorGender);
    }
}

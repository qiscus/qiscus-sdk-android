package com.qiscus.sdk.chat.core.event;

public class QiscusChatRoomTypingAIEvent {

    private long roomId;
    private boolean typing;
    private String senderId;
    private String senderName;
    private String textMessage;

    public long getRoomId() {
        return roomId;
    }

    public QiscusChatRoomTypingAIEvent setRoomId(long roomId) {
        this.roomId = roomId;
        return this;
    }
    public boolean isTyping() {
        return typing;
    }

    public QiscusChatRoomTypingAIEvent setTyping(boolean typing) {
        this.typing = typing;
        return this;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getTexttextMessage() {
        return textMessage;
    }

    public QiscusChatRoomTypingAIEvent setSenderId(String senderId) {
        this.senderId = senderId;
        return this;
    }

    public QiscusChatRoomTypingAIEvent setSenderName(String senderName) {
        this.senderName = senderName;
        return this;
    }

    public QiscusChatRoomTypingAIEvent setTextMessage(String textMessage) {
        this.textMessage = textMessage;
        return this;
    }

    @Override
    public String toString() {
        return "QiscusChatRoomEvent{" +
                "roomId=" + roomId +
                ", typing=" + typing +
                ", senderId='" + senderId +
                ", senderName='" + senderName +
                ", textMessage=" + textMessage +
                '}';
    }
}
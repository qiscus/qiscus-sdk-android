package com.qiscus.sdk.chat.core.data.model;

import org.json.JSONObject;

public class QiscusChannels {
    private String avatarUrl;
    private String createdAt;
    private JSONObject extras;
    private Boolean isJoined;
    private String name;
    private String uniqueId;
    private Long roomId;

    @Override
    public String toString() {
        return "QiscusChannels{" +
                "avatarUrl='" + avatarUrl + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", extras=" + extras +
                ", isJoined=" + isJoined +
                ", name='" + name + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", roomId=" + roomId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QiscusChannels channels = (QiscusChannels) o;

        if (avatarUrl != null ? !avatarUrl.equals(channels.avatarUrl) : channels.avatarUrl != null)
            return false;
        if (createdAt != null ? !createdAt.equals(channels.createdAt) : channels.createdAt != null)
            return false;
        if (extras != null ? !extras.equals(channels.extras) : channels.extras != null)
            return false;
        if (isJoined != null ? !isJoined.equals(channels.isJoined) : channels.isJoined != null)
            return false;
        if (name != null ? !name.equals(channels.name) : channels.name != null) return false;
        if (uniqueId != null ? !uniqueId.equals(channels.uniqueId) : channels.uniqueId != null)
            return false;
        return roomId != null ? roomId.equals(channels.roomId) : channels.roomId == null;
    }

    @Override
    public int hashCode() {
        int result = avatarUrl != null ? avatarUrl.hashCode() : 0;
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (extras != null ? extras.hashCode() : 0);
        result = 31 * result + (isJoined != null ? isJoined.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (uniqueId != null ? uniqueId.hashCode() : 0);
        result = 31 * result + (roomId != null ? roomId.hashCode() : 0);
        return result;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public JSONObject getExtras() {
        return extras;
    }

    public void setExtras(JSONObject extras) {
        this.extras = extras;
    }

    public Boolean getJoined() {
        return isJoined;
    }

    public void setJoined(Boolean joined) {
        isJoined = joined;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}

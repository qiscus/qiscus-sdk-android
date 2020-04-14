package com.qiscus.sdk.chat.core.data.model;

import java.util.Date;

public class QUserPresence {
    private String userId;
    private Boolean status;
    private Date timestamp;

    @Override
    public String toString() {
        return "QUserPresence{" +
                "userId='" + userId + '\'' +
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QUserPresence userStatus = (QUserPresence) o;

        if (status != null ? !status.equals(userStatus.status) : userStatus.status != null)
            return false;
        if (timestamp != null ? !timestamp.equals(userStatus.timestamp) : userStatus.timestamp != null)
            return false;

        return userId != null ? !userId.equals(userStatus.userId) : userStatus.userId != null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}

package com.qiscus.sdk.chat.core.data.model;

public class QiscusRealtimeStatus {
    private Boolean realtimeStatus;

    public Boolean getRealtimeStatus() {
        return realtimeStatus;
    }

    public void setRealtimeStatus(Boolean realtimeStatus) {
        this.realtimeStatus = realtimeStatus;
    }

    @Override
    public String toString() {
        return "QiscusRealtimeStatus{" +
                "realtimeStatus=" + realtimeStatus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QiscusRealtimeStatus that = (QiscusRealtimeStatus) o;

        return realtimeStatus.equals(that.realtimeStatus);
    }

    @Override
    public int hashCode() {
        return realtimeStatus.hashCode();
    }
}

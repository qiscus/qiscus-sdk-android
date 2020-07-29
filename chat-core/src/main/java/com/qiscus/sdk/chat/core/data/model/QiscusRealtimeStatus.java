package com.qiscus.sdk.chat.core.data.model;

public class QiscusRealtimeStatus {
    private boolean realtimeStatus;

    public boolean getRealtimeStatus() {
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
}

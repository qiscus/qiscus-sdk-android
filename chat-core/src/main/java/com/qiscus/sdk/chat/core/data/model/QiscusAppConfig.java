package com.qiscus.sdk.chat.core.data.model;

import androidx.annotation.NonNull;

public class QiscusAppConfig  {

    private String baseURL;
    private String brokerLBURL;
    private String brokerURL;
    private Boolean enableEventReport;
    private Integer syncInterval;
    private Integer syncOnConnect;
    private Boolean enableRealtime;
    private Boolean enableRealtimeCheck;

    public QiscusAppConfig() {
        super();
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QiscusAppConfig that = (QiscusAppConfig) o;

        if (!baseURL.equals(that.baseURL)) return false;
        if (!brokerLBURL.equals(that.brokerLBURL)) return false;
        if (!brokerURL.equals(that.brokerURL)) return false;
        if (!enableEventReport.equals(that.enableEventReport)) return false;
        if (!enableRealtime.equals(that.enableRealtime)) return false;
        if (!syncInterval.equals(that.syncInterval)) return false;
        if (!enableRealtimeCheck.equals(that.enableRealtimeCheck)) return false;
        return syncOnConnect.equals(that.syncOnConnect);
    }

    @Override
    public int hashCode() {
        int result = baseURL.hashCode();
        result = 31 * result + brokerLBURL.hashCode();
        result = 31 * result + brokerURL.hashCode();
        result = 31 * result + enableEventReport.hashCode();
        result = 31 * result + enableRealtime.hashCode();
        result = 31 * result + syncInterval.hashCode();
        result = 31 * result + syncOnConnect.hashCode();
        result = 31 * result + enableRealtimeCheck.hashCode();
        return result;
    }



    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getBrokerLBURL() {
        return brokerLBURL;
    }

    public void setBrokerLBURL(String brokerLBURL) {
        this.brokerLBURL = brokerLBURL;
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public Boolean getEnableEventReport() {
        return enableEventReport;
    }

    public void setEnableEventReport(Boolean enableEventReport) {
        this.enableEventReport = enableEventReport;
    }

    public Integer getSyncInterval() {
        return syncInterval;
    }

    public void setSyncInterval(Integer syncInterval) {
        this.syncInterval = syncInterval;
    }

    public Integer getSyncOnConnect() {
        return syncOnConnect;
    }

    public void setSyncOnConnect(Integer syncOnConnect) {
        this.syncOnConnect = syncOnConnect;
    }

    public Boolean getEnableRealtime() {
        return enableRealtime;
    }

    public void setEnableRealtime(Boolean enableRealtime) {
        this.enableRealtime = enableRealtime;
    }

    public Boolean getEnableRealtimeCheck() {
        return enableRealtimeCheck;
    }

    public void setEnableRealtimeCheck(Boolean enableRealtimeCheck) {
        this.enableRealtimeCheck = enableRealtimeCheck;
    }
}

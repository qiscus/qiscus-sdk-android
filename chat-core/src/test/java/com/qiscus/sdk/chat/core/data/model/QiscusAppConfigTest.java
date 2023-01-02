package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QiscusAppConfigTest {
    QiscusAppConfig appConfig ;
    @Before
    public void setUp() throws Exception {
        appConfig = new QiscusAppConfig();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testToString() {
        setAutoRefreshToken();
        setBaseURL();
        setBrokerLBURL();
        setBrokerURL();
        setEnableRealtime();
        setEnableEventReport();
        setEnableRealtimeCheck();
        setEnableSync();
        setEnableSyncEvent();
        setNetworkConnectionInterval();
        setSyncInterval();
        setSyncOnConnect();
        appConfig.toString();
    }

    @Test
    public void testEquals() {
        setBaseURL();
        setBrokerLBURL();
        setBrokerURL();
        setEnableRealtime();
        setEnableEventReport();
        setEnableRealtimeCheck();
        setEnableSync();
        setEnableSyncEvent();
        setNetworkConnectionInterval();
        setSyncInterval();
        setSyncOnConnect();
        setAutoRefreshToken();
    }

    @Test
    public void testHashCode() {
        setAutoRefreshToken();
        setBaseURL();
        setBrokerLBURL();
        setBrokerURL();
        setEnableRealtime();
        setEnableEventReport();
        setEnableRealtimeCheck();
        setEnableSync();
        setEnableSyncEvent();
        setNetworkConnectionInterval();
        setSyncInterval();
        setSyncOnConnect();
        appConfig.hashCode();

        appConfig.equals(appConfig);
    }

    @Test
    public void getBaseURL() {
        appConfig.getBaseURL();
    }

    @Test
    public void setBaseURL() {
        appConfig.setBaseURL("https://");
    }

    @Test
    public void getBrokerLBURL() {
        appConfig.getBrokerLBURL();
    }

    @Test
    public void setBrokerLBURL() {
        appConfig.setBrokerLBURL("https://");
    }

    @Test
    public void getBrokerURL() {
        appConfig.getBrokerURL();
    }

    @Test
    public void setBrokerURL() {
        appConfig.setBrokerURL("https://");
    }

    @Test
    public void getEnableEventReport() {
        appConfig.getEnableEventReport();
    }

    @Test
    public void setEnableEventReport() {
        appConfig.setEnableEventReport(false);
    }

    @Test
    public void getSyncInterval() {
        appConfig.getSyncInterval();
    }

    @Test
    public void setSyncInterval() {
        appConfig.setSyncInterval(5000);
    }

    @Test
    public void getSyncOnConnect() {
        appConfig.getSyncOnConnect();
    }

    @Test
    public void setSyncOnConnect() {
        appConfig.setSyncOnConnect(30000);
    }

    @Test
    public void getNetworkConnectionInterval() {
        appConfig.getNetworkConnectionInterval();
    }

    @Test
    public void setNetworkConnectionInterval() {
        appConfig.setNetworkConnectionInterval(3000);
    }

    @Test
    public void getEnableRealtime() {
        appConfig.getEnableRealtime();
    }

    @Test
    public void setEnableRealtime() {
        appConfig.setEnableRealtime(true);
    }

    @Test
    public void getEnableSync() {
        appConfig.getEnableSync();
    }

    @Test
    public void setEnableSync() {
        appConfig.setEnableSync(true);
    }

    @Test
    public void getEnableSyncEvent() {
        appConfig.getEnableSyncEvent();
    }

    @Test
    public void setEnableSyncEvent() {
        appConfig.setEnableSyncEvent(true);
    }

    @Test
    public void getEnableRealtimeCheck() {
        appConfig.getEnableRealtimeCheck();
    }

    @Test
    public void setEnableRealtimeCheck() {
        appConfig.setEnableRealtimeCheck(true);
    }

    @Test
    public void getAutoRefreshToken() {
        appConfig.getAutoRefreshToken();
    }

    @Test
    public void setAutoRefreshToken() {
        appConfig.setAutoRefreshToken(false);
    }
}
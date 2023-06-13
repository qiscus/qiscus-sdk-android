package com.qiscus.sdk.chat.core;

import android.app.Application;
import android.os.Handler;

import com.qiscus.sdk.chat.core.data.local.QiscusDataBaseHelper;
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;

import org.json.JSONObject;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class QiscusAppComponent {

    private final Application application;

//    public void setAppId(String appId) {
//        this.appId = appId;
//    }

    private String appId;
    private String baseURLLB;
    private String mqttBrokerUrl;
    private String appServer;
    private boolean enableMqttLB = true;
    private int heartBeat = 5000;
    private int automaticHeartBeat = 30000;
    private int networkConnectionInterval = 5000;
    private Boolean enableRealtime = true;
    private Boolean enableEventReport = true;
    private Boolean isBuiltIn = false;
    private Boolean syncServiceDisabled = false;
    private Boolean enableSync = true;
    private Boolean enableSyncEvent = false;
    private Boolean autoRefreshToken = true;
    private Boolean forceDisableRealtimeFromExactAlarm = false;

    private JSONObject customHeader;

    private final Handler appHandler;
    private final ScheduledThreadPoolExecutor taskExecutor = new ScheduledThreadPoolExecutor(5);
    private final QiscusCore.LocalDataManager localDataManager;

    private QiscusDataBaseHelper dataBaseHelper;
    private QiscusDataStore dataStore;
    private static QiscusAppComponent INSTANCE;

    public static QiscusAppComponent create(Application application, String qiscusAppId, String serverBaseUrl) {
        synchronized (QiscusAppComponent.class) {
            INSTANCE = new QiscusAppComponent(application, qiscusAppId, serverBaseUrl);
        }
        return INSTANCE;
    }

    public static QiscusAppComponent getInstance() {
        if (INSTANCE == null) {
            synchronized (QiscusAppComponent.class) {
                if (INSTANCE == null) {
                    throw new IllegalArgumentException("Failed QiscusAppComponent getInstance()");
                }
            }
        }
        return INSTANCE;
    }

    private QiscusAppComponent(Application application, String qiscusAppId, String serverBaseUrl) {
        this.application = application;
        this.appId = qiscusAppId;
        this.appServer = !serverBaseUrl.endsWith("/") ? serverBaseUrl + "/" : serverBaseUrl;
        this.appHandler =  new Handler(application.getMainLooper());
        this.localDataManager = new QiscusCore.LocalDataManager(application);
        //this.chatConfig = new QiscusCoreChatConfig();
        this.dataStore = new QiscusDataBaseHelper(application);
        QiscusTextUtil.createInstance(application);
    }

    public Application getApplication() {
        return application;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppServer() {
        return appServer;
    }


    public void setAppServer(String appServer) {
        this.appServer = appServer;
    }

    public int getHeartBeat() {
        return heartBeat;
    }


    public void setHeartBeat(int heartBeat) {
        this.heartBeat = heartBeat;
    }

    public int getAutomaticHeartBeat() {
        return automaticHeartBeat;
    }

    public void setAutomaticHeartBeat(int automaticHeartBeat) {
        this.automaticHeartBeat = automaticHeartBeat;
    }

    public int getNetworkConnectionInterval() {
        return networkConnectionInterval;
    }

    public void setNetworkConnectionInterval(int automaticHeartBeatnetworkConnectionInterval) {
        this.networkConnectionInterval = networkConnectionInterval;
    }

    public Boolean getEnableEventReport() {
        return enableEventReport;
    }

    public void setEnableEventReport(Boolean enableEventReport) {
        this.enableEventReport = enableEventReport;
    }

    public String getBaseURLLB() {
        return baseURLLB;
    }

    public void setBaseURLLB(String baseURLLB) {
        this.baseURLLB = baseURLLB;
    }

    public Boolean getIsBuiltIn() {
        return isBuiltIn;
    }

    public void setIsBuiltIn(Boolean isBuiltIn) {
        this.isBuiltIn = isBuiltIn;
    }

    public String getMqttBrokerUrl() {
        return mqttBrokerUrl;
    }

    public void setMqttBrokerUrl(String mqttBrokerUrl) {
        this.mqttBrokerUrl = mqttBrokerUrl;
    }

    public Boolean getEnableMqttLB() {
        return enableMqttLB;
    }

    public void setEnableMqttLB(Boolean enableMqttLB) {
        this.enableMqttLB = enableMqttLB;
    }

    public Boolean getEnableRealtime() {
        return enableRealtime;
    }

    public void setEnableRealtime(Boolean enableRealtime) {
        this.enableRealtime = enableRealtime;
    }

    public Boolean getSyncServiceDisabled() {
        return syncServiceDisabled;
    }

    public void setSyncServiceDisabled(Boolean syncServiceDisabled) {
        this.syncServiceDisabled = syncServiceDisabled;
    }

    public Boolean getEnableSync() {
        return enableSync;
    }

    public void setEnableSync(Boolean enableSync) {
        this.enableSync = enableSync;
    }

    public Boolean getEnableSyncEvent() {
        return enableSyncEvent;
    }

    public void setEnableSyncEvent(Boolean enableSyncEvent) {
        this.enableSyncEvent = enableSyncEvent;
    }

    public Boolean getAutoRefreshToken() {
        return autoRefreshToken;
    }

    public void setAutoRefreshToken(Boolean autoRefreshToken) {
        this.autoRefreshToken = autoRefreshToken;
    }

    public Boolean getForceDisableRealtimeFromExactAlarm() {
        return forceDisableRealtimeFromExactAlarm;
    }

    public void setForceDisableRealtimeFromExactAlarm(Boolean forceDisableRealtimeFromExactAlarm) {
        this.forceDisableRealtimeFromExactAlarm = forceDisableRealtimeFromExactAlarm;
    }

    public JSONObject getCustomHeader() {
        return customHeader;
    }

    public void setCustomHeader(JSONObject customHeader) {
        this.customHeader = customHeader;
    }

    public Handler getAppHandler() {
        return appHandler;
    }

    public ScheduledThreadPoolExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public QiscusCore.LocalDataManager getLocalDataManager() {
        return localDataManager;
    }

    public QiscusDataBaseHelper getDataBaseHelper() {
        return dataBaseHelper;
    }

    public QiscusDataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(QiscusDataStore qiscusDataStore) {
        this.dataStore = qiscusDataStore;
    }

}

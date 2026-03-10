package com.qiscus.sdk.chat.core;

import android.app.Application;
import android.os.Handler;

import com.qiscus.sdk.chat.core.data.local.QiscusDataBaseHelper;
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore;
import com.qiscus.sdk.chat.core.data.local.QiscusDataManagement;
import com.qiscus.sdk.chat.core.event.QiscusInitWithCustomServerEvent;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class QiscusAppComponent {
    // ✅ Add volatile to ALL fields for thread-safe reads
    private volatile Application application;
    private volatile String appId;
    private volatile String baseURLLB;
    private volatile String mqttBrokerUrl;
    private volatile String appServer;
    private volatile boolean enableMqttLB = true;
    private volatile int heartBeat = 5000;
    private volatile int automaticHeartBeat = 30000;
    private volatile int networkConnectionInterval = 5000;
    private volatile Boolean enableRealtime = true;
    private volatile String usernameMQTT = "";
    private volatile String passwordMQTT = "";
    private volatile Boolean enableEventReport = true;
    private volatile Boolean isBuiltIn = false;
    private volatile Boolean syncServiceDisabled = false;
    private volatile Boolean enableSync = true;
    private volatile Boolean enableSyncEvent = false;
    private volatile Boolean autoRefreshToken = true;
    private volatile Boolean enableRefreshToken = false;
    private volatile Boolean forceDisableRealtimeFromExactAlarm = false;
    private volatile JSONObject customHeader;
    private volatile Handler appHandler;
    private final ScheduledThreadPoolExecutor taskExecutor = new ScheduledThreadPoolExecutor(5);
    private volatile QiscusCore.LocalDataManager localDataManager;

    private volatile QiscusDataBaseHelper dataBaseHelper;
    private volatile QiscusDataStore dataStore;
    private static volatile QiscusAppComponent INSTANCE;

    public static QiscusAppComponent create() {
        synchronized (QiscusAppComponent.class) {
            INSTANCE = new QiscusAppComponent();
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

    public synchronized void setup(Application application, String qiscusAppId, String serverBaseUrl) {
        this.application = application;
        this.appId = qiscusAppId;
        this.appServer = !serverBaseUrl.endsWith("/") ? serverBaseUrl + "/" : serverBaseUrl;
        this.appHandler =  new Handler(application.getMainLooper());
        this.localDataManager = new QiscusCore.LocalDataManager(application);
        //this.chatConfig = new QiscusCoreChatConfig();
        this.dataStore = new QiscusDataBaseHelper(application);
        QiscusTextUtil.createInstance(application);
        QiscusDataManagement.validateCustomKey(qiscusAppId);
        EventBus.getDefault().post((QiscusInitWithCustomServerEvent.wasSetup));
    }

    public Application getApplication() {
        return application;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppServer() {
        return this.appServer;
    }

    public synchronized void setAppServer(String appServer) {
        this.appServer = appServer;
    }

    public int getHeartBeat() {
        return heartBeat;
    }

    public synchronized void setHeartBeat(int heartBeat) {
        this.heartBeat = heartBeat;
    }

    public int getAutomaticHeartBeat() {
        return automaticHeartBeat;
    }

    public synchronized void setAutomaticHeartBeat(int automaticHeartBeat) {
        this.automaticHeartBeat = automaticHeartBeat;
    }

    public int getNetworkConnectionInterval() {
        return this.networkConnectionInterval;
    }

    public synchronized void setNetworkConnectionInterval(int automaticHeartBeatnetworkConnectionInterval) {
        this.networkConnectionInterval = automaticHeartBeatnetworkConnectionInterval;
    }

    public Boolean getEnableEventReport() {
        return enableEventReport;
    }

    public synchronized void setEnableEventReport(Boolean enableEventReport) {
        this.enableEventReport = enableEventReport;
    }

    public String getBaseURLLB() {
        return baseURLLB;
    }

    public synchronized void setBaseURLLB(String baseURLLB) {
        this.baseURLLB = baseURLLB;
    }

    public Boolean getIsBuiltIn() {
        return isBuiltIn;
    }

    public synchronized void setIsBuiltIn(Boolean isBuiltIn) {
        this.isBuiltIn = isBuiltIn;
    }

    public String getMqttBrokerUrl() {
        return mqttBrokerUrl;
    }

    public synchronized void setMqttBrokerUrl(String mqttBrokerUrl) {
        this.mqttBrokerUrl = mqttBrokerUrl;
    }

    public Boolean getEnableMqttLB() {
        return enableMqttLB;
    }

    public synchronized void setEnableMqttLB(Boolean enableMqttLB) {
        this.enableMqttLB = enableMqttLB;
    }

    public Boolean getEnableRealtime() {
        return enableRealtime;
    }

    public synchronized void setEnableRealtime(Boolean enableRealtime) {
        this.enableRealtime = enableRealtime;
    }

    public String getUsernameMQTT() {
        return usernameMQTT;
    }

    public synchronized void setUsernameMQTT(String usernameMQTT) {
        this.usernameMQTT = usernameMQTT;
    }

    public String getPasswordMQTT() {
        return passwordMQTT;
    }

    public synchronized void setPaswwordMQTT(String passwordMQTT) {
        this.passwordMQTT = passwordMQTT;
    }

    public Boolean getSyncServiceDisabled() {
        return syncServiceDisabled;
    }

    public synchronized void setSyncServiceDisabled(Boolean syncServiceDisabled) {
        this.syncServiceDisabled = syncServiceDisabled;
    }

    public Boolean getEnableSync() {
        return enableSync;
    }

    public synchronized void setEnableSync(Boolean enableSync) {
        this.enableSync = enableSync;
    }

    public Boolean getEnableSyncEvent() {
        return enableSyncEvent;
    }

    public synchronized void setEnableSyncEvent(Boolean enableSyncEvent) {
        this.enableSyncEvent = enableSyncEvent;
    }

    public Boolean getAutoRefreshToken() {
        return autoRefreshToken;
    }

    public synchronized void setAutoRefreshToken(Boolean autoRefreshToken) {
        this.autoRefreshToken = autoRefreshToken;
    }

    public Boolean getEnableRefreshToken() {
        return enableRefreshToken;
    }

    public synchronized void setEnableRefreshToken(Boolean enableRefreshToken) {
        this.enableRefreshToken = enableRefreshToken;
    }


    public Boolean getForceDisableRealtimeFromExactAlarm() {
        return forceDisableRealtimeFromExactAlarm;
    }

    public synchronized void setForceDisableRealtimeFromExactAlarm(Boolean forceDisableRealtimeFromExactAlarm) {
        this.forceDisableRealtimeFromExactAlarm = forceDisableRealtimeFromExactAlarm;
    }

    public JSONObject getCustomHeader() {
        return customHeader;
    }

    public synchronized void setCustomHeader(JSONObject customHeader) {
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

    public synchronized  void setDataStore(QiscusDataStore qiscusDataStore) {
        this.dataStore = qiscusDataStore;
    }

    public synchronized void setCustomKey(String key) {
        QiscusDataManagement.setCustomKey(key);
    }

}

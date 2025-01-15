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

    private Application application;
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
    private Boolean enableRefreshToken = false;
    private Boolean forceDisableRealtimeFromExactAlarm = false;
    private JSONObject customHeader;
    private Handler appHandler;
    private final ScheduledThreadPoolExecutor taskExecutor = new ScheduledThreadPoolExecutor(5);
    private QiscusCore.LocalDataManager localDataManager;

    private QiscusDataBaseHelper dataBaseHelper;
    private QiscusDataStore dataStore;
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

    public synchronized Application getApplication() {
        return application;
    }

    public synchronized String getAppId() {
        return appId;
    }

    public synchronized String getAppServer() {
        return appServer;
    }

    public synchronized void setAppServer(String appServer) {
        this.appServer = appServer;
    }

    public synchronized int getHeartBeat() {
        return heartBeat;
    }

    public synchronized void setHeartBeat(int heartBeat) {
        this.heartBeat = heartBeat;
    }

    public synchronized int getAutomaticHeartBeat() {
        return automaticHeartBeat;
    }

    public synchronized void setAutomaticHeartBeat(int automaticHeartBeat) {
        this.automaticHeartBeat = automaticHeartBeat;
    }

    public synchronized int getNetworkConnectionInterval() {
        return networkConnectionInterval;
    }

    public synchronized void setNetworkConnectionInterval(int automaticHeartBeatnetworkConnectionInterval) {
        this.networkConnectionInterval = networkConnectionInterval;
    }

    public synchronized Boolean getEnableEventReport() {
        return enableEventReport;
    }

    public synchronized void setEnableEventReport(Boolean enableEventReport) {
        this.enableEventReport = enableEventReport;
    }

    public synchronized String getBaseURLLB() {
        return baseURLLB;
    }

    public synchronized void setBaseURLLB(String baseURLLB) {
        this.baseURLLB = baseURLLB;
    }

    public synchronized Boolean getIsBuiltIn() {
        return isBuiltIn;
    }

    public synchronized void setIsBuiltIn(Boolean isBuiltIn) {
        this.isBuiltIn = isBuiltIn;
    }

    public synchronized String getMqttBrokerUrl() {
        return mqttBrokerUrl;
    }

    public synchronized void setMqttBrokerUrl(String mqttBrokerUrl) {
        this.mqttBrokerUrl = mqttBrokerUrl;
    }

    public synchronized Boolean getEnableMqttLB() {
        return enableMqttLB;
    }

    public synchronized void setEnableMqttLB(Boolean enableMqttLB) {
        this.enableMqttLB = enableMqttLB;
    }

    public synchronized Boolean getEnableRealtime() {
        return enableRealtime;
    }

    public synchronized void setEnableRealtime(Boolean enableRealtime) {
        this.enableRealtime = enableRealtime;
    }

    public synchronized Boolean getSyncServiceDisabled() {
        return syncServiceDisabled;
    }

    public synchronized void setSyncServiceDisabled(Boolean syncServiceDisabled) {
        this.syncServiceDisabled = syncServiceDisabled;
    }

    public synchronized Boolean getEnableSync() {
        return enableSync;
    }

    public synchronized void setEnableSync(Boolean enableSync) {
        this.enableSync = enableSync;
    }

    public synchronized Boolean getEnableSyncEvent() {
        return enableSyncEvent;
    }

    public synchronized void setEnableSyncEvent(Boolean enableSyncEvent) {
        this.enableSyncEvent = enableSyncEvent;
    }

    public synchronized Boolean getAutoRefreshToken() {
        return autoRefreshToken;
    }

    public synchronized void setAutoRefreshToken(Boolean autoRefreshToken) {
        this.autoRefreshToken = autoRefreshToken;
    }

    public synchronized Boolean getEnableRefreshToken() {
        return enableRefreshToken;
    }

    public synchronized void setEnableRefreshToken(Boolean enableRefreshToken) {
        this.enableRefreshToken = enableRefreshToken;
    }


    public synchronized Boolean getForceDisableRealtimeFromExactAlarm() {
        return forceDisableRealtimeFromExactAlarm;
    }

    public synchronized void setForceDisableRealtimeFromExactAlarm(Boolean forceDisableRealtimeFromExactAlarm) {
        this.forceDisableRealtimeFromExactAlarm = forceDisableRealtimeFromExactAlarm;
    }

    public synchronized JSONObject getCustomHeader() {
        return customHeader;
    }

    public synchronized void setCustomHeader(JSONObject customHeader) {
        this.customHeader = customHeader;
    }

    public synchronized Handler getAppHandler() {
        return appHandler;
    }

    public synchronized ScheduledThreadPoolExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public synchronized QiscusCore.LocalDataManager getLocalDataManager() {
        return localDataManager;
    }

    public synchronized QiscusDataBaseHelper getDataBaseHelper() {
        return dataBaseHelper;
    }

    public synchronized QiscusDataStore getDataStore() {
        return dataStore;
    }

    public synchronized  void setDataStore(QiscusDataStore qiscusDataStore) {
        this.dataStore = qiscusDataStore;
    }

    public synchronized void setCustomKey(String key) {
        QiscusDataManagement.setCustomKey(key);
    }

}

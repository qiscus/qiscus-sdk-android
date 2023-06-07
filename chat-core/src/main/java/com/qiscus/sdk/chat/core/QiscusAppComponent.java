package com.qiscus.sdk.chat.core;

import android.app.Application;
import android.os.Handler;

import com.qiscus.sdk.chat.core.data.local.QiscusDataBaseHelper;
import com.qiscus.sdk.chat.core.data.model.QiscusCoreChatConfig;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class QiscusAppComponent {

    private final Application application;

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppServer(String appServer) {
        this.appServer = appServer;
    }

    private String appId;
    private String appServer;
    private final int heartBeat = 5000;
    private final int automaticHeartBeat = 30000;
    private final int networkConnectionInterval = 5000;

    private final Handler appHandler;
    private final ScheduledThreadPoolExecutor taskExecutor = new ScheduledThreadPoolExecutor(5);
    private final QiscusCore.LocalDataManager localDataManager;
    private final QiscusCoreChatConfig chatConfig;
    private final QiscusDataBaseHelper dataStore;

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
                    throw new IllegalArgumentException("");
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
        this.chatConfig = new QiscusCoreChatConfig();
        this.dataStore = new QiscusDataBaseHelper(application);
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

    public int getHeartBeat() {
        return heartBeat;
    }

    public int getAutomaticHeartBeat() {
        return automaticHeartBeat;
    }

    public int getNetworkConnectionInterval() {
        return networkConnectionInterval;
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

    public QiscusCoreChatConfig getChatConfig() {
        return chatConfig;
    }

    public QiscusDataBaseHelper getDataStore() {
        return dataStore;
    }

}

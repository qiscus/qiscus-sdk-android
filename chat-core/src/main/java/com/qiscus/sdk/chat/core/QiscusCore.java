/**
 * Copyright (c) 2016 Qiscus.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.chat.core;

import android.app.Application;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import androidx.annotation.RestrictTo;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.data.local.QiscusDataBaseHelper;
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore;
import com.qiscus.sdk.chat.core.data.local.QiscusEventCache;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.data.model.QUser;
import com.qiscus.sdk.chat.core.data.model.QiscusCoreChatConfig;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.data.remote.QiscusClearCommentsHandler;
import com.qiscus.sdk.chat.core.data.remote.QiscusDeleteCommentHandler;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.data.remote.QiscusResendCommentHelper;
import com.qiscus.sdk.chat.core.event.QiscusUserEvent;
import com.qiscus.sdk.chat.core.mediator.QiscusMediator;
import com.qiscus.sdk.chat.core.util.BuildVersionUtil;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusFirebaseMessagingUtil;
import com.qiscus.sdk.chat.core.util.QiscusLogger;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Jul, Wed 25 2018 15.35
 **/
public class QiscusCore {

    private Application appInstance;
    private String appId;
    private String appServer;
    private String mqttBrokerUrl;
    private String baseURLLB;
    private Long userIsActive;
    private LocalDataManager localDataManager;
    private SetUserBuilder setUserBuilder;
    private long heartBeat;
    private long automaticHeartBeat;
    private QiscusDataStore dataStore;
    private QiscusCoreChatConfig chatConfig;
    private Handler appHandler;
    private ScheduledThreadPoolExecutor taskExecutor;
    private boolean enableMqttLB = true;
    private JSONObject customHeader;
    private Boolean enableEventReport = true;
    private Boolean enableRealtime = true;
    private QiscusMediator qiscusMediator;

    public QiscusCore() {
    }

    /**
     * The first method you need to be invoke to using qiscus sdk. Call this method from your Application
     * class. You can not using another qiscus feature if you not invoke this method first. Here sample
     * to call this method:
     * <pre>
     * {@code
     * public class SampleApps extends Application {
     *  public void onCreate() {
     *      super.onCreate();
     *      QiscusCore.init(this, "yourQiscusAppId");
     *  }
     * }
     * }
     * </pre>
     *
     * @param application Application instance
     * @param qiscusAppId Your qiscus application Id
     */
    @Deprecated
    public void init(Application application, String qiscusAppId, String localPrefKey) {
        initWithCustomServer(application, qiscusAppId, BuildConfig.BASE_URL_SERVER,
                BuildConfig.BASE_URL_MQTT_BROKER, true, BuildConfig.BASE_URL_MQTT_LB, localPrefKey);
    }

    /**
     * The first method you need to be invoke to using qiscus sdk. Call this method from your Application
     * class. You can not using another qiscus feature if you not invoke this method first. Here sample
     * to call this method:
     * <pre>
     * {@code
     * public class SampleApps extends Application {
     *  public void onCreate() {
     *      super.onCreate();
     *      QiscusCore.setup(this, "yourQiscusAppId");
     *  }
     * }
     * }
     * </pre>
     *
     * @param application Application instance
     * @param appID       Your qiscus application Id
     */
    public void setup(Application application, String appID, String localPrefKey) {
        initWithCustomServer(application, appID, BuildConfig.BASE_URL_SERVER,
                BuildConfig.BASE_URL_MQTT_BROKER, true, BuildConfig.BASE_URL_MQTT_LB, localPrefKey);
    }

    /**
     * The first method you need to be invoke to using qiscus sdk. Call this method from your Application
     * class. You can not using another qiscus feature if you not invoke this method first. Here sample
     * to call this method:
     * <pre>
     * {@code
     * public class SampleApps extends Application {
     *  public void onCreate() {
     *      super.onCreate();
     *      QiscusCore.initWithCustomServer(this, my-app-id, "http://myserver.com/", "ssl://mqtt.myserver.com:1885");
     *  }
     * }
     * }
     * </pre>
     *
     * @param application Application instance
     * @param appId       Your Qiscus App Id
     * @param baseUrl     Your qiscus chat engine base url
     * @param brokerUrl   Your Mqtt Broker url
     */
    @Deprecated
    public void initWithCustomServer(Application application, String appId, String baseUrl,
                                     String brokerUrl, String brokerLBUrl, String localPrefKey) {
        if (brokerLBUrl == null) {
            initWithCustomServer(application, appId, baseUrl, brokerUrl, false, brokerLBUrl, localPrefKey);
        } else {
            initWithCustomServer(application, appId, baseUrl, brokerUrl, true, brokerLBUrl, localPrefKey);
        }
    }

    /**
     * The first method you need to be invoke to using qiscus sdk. Call this method from your Application
     * class. You can not using another qiscus feature if you not invoke this method first. Here sample
     * to call this method:
     * <pre>
     * {@code
     * public class SampleApps extends Application {
     *  public void onCreate() {
     *      super.onCreate();
     *      QiscusCore.initWithCustomServer(this, my-app-id, "http://myserver.com/", "ssl://mqtt.myserver.com:1885");
     *  }
     * }
     * }
     * </pre>
     *
     * @param application Application instance
     * @param appId       Your Qiscus App Id
     * @param baseUrl     Your qiscus chat engine base url
     * @param brokerUrl   Your Mqtt Broker url
     */

    public void setupWithCustomServer(Application application, String appId, String baseUrl,
                                      String brokerUrl, String brokerLBUrl, String localPrefKey) {
        if (brokerLBUrl == null) {
            initWithCustomServer(application, appId, baseUrl, brokerUrl, false, brokerLBUrl, localPrefKey);
        } else {
            initWithCustomServer(application, appId, baseUrl, brokerUrl, true, brokerLBUrl, localPrefKey);
        }
    }

    /**
     * This method @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
     *
     * @param application   Application instance
     * @param qiscusAppId   Your Qiscus App Id
     * @param serverBaseUrl Your qiscus chat engine base url
     * @param mqttBrokerUrl Your Mqtt Broker url
     * @param enableMqttLB  Qiscus using own MQTT Load Balancer for get mqtt server url
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public void initWithCustomServer(Application application, String qiscusAppId, String serverBaseUrl,
                                     String mqttBrokerUrl, boolean enableMqttLB, String baseURLLB, String localPrefKey) {

        appInstance = application;
        appId = qiscusAppId;

        appServer = !serverBaseUrl.endsWith("/") ? serverBaseUrl + "/" : serverBaseUrl;

        chatConfig = new QiscusCoreChatConfig();

        appHandler = new Handler(getApps().getApplicationContext().getMainLooper());
        taskExecutor = new ScheduledThreadPoolExecutor(5);
        localDataManager = new LocalDataManager(localPrefKey);
        setUserBuilder = new SetUserBuilder();
        dataStore = new QiscusDataBaseHelper(this, localPrefKey);
        qiscusMediator = new QiscusMediator();
        heartBeat = 5000;
        automaticHeartBeat = 30000;

        this.enableMqttLB = enableMqttLB;
        this.mqttBrokerUrl = mqttBrokerUrl;
        this.baseURLLB = baseURLLB;
        enableEventReport = false;
        localDataManager.setURLLB(baseURLLB);

        qiscusMediator.initAllClass(this);

        getAppConfig();

        configureFcmToken();
    }

    private void getAppConfig() {
        getApi().getAppConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appConfig -> {
                    enableEventReport = appConfig.getEnableEventReport();
                    if (!appConfig.getBaseURL().isEmpty()) {
                        String oldAppServer = appServer;
                        String newAppServer = !appConfig.getBaseURL().endsWith("/") ?
                                appConfig.getBaseURL() + "/" : appConfig.getBaseURL();

                        if (!oldAppServer.equals(newAppServer)) {
                            appServer = newAppServer;
                        }
                    }

                    getApi().reInitiateInstance();

                    if (!appConfig.getBrokerLBURL().isEmpty()) {
                        this.baseURLLB = appConfig.getBrokerLBURL();
                    }

                    if (!appConfig.getBrokerURL().isEmpty()) {

                        String oldMqttBrokerUrl = this.mqttBrokerUrl;
                        String newMqttBrokerUrl = String.format("ssl://%s:1885",
                                appConfig.getBrokerURL());

                        if (!oldMqttBrokerUrl.equals(newMqttBrokerUrl)) {
                            this.mqttBrokerUrl = newMqttBrokerUrl;
                            setCacheMqttBrokerUrl(newMqttBrokerUrl, false);
                        } else {
                            setCacheMqttBrokerUrl(mqttBrokerUrl, false);
                        }
                    }

                    if (appConfig.getSyncInterval() != 0) {
                        heartBeat = appConfig.getSyncInterval();
                    }

                    if (appConfig.getSyncOnConnect() != 0) {
                        automaticHeartBeat = appConfig.getSyncOnConnect();
                    }

                    enableRealtime = appConfig.getEnableRealtime();

                    if (!enableRealtime) {
                        //enable realtime false
                        getPusherApi().disconnect();
                    }

                }, throwable -> {
                    getErrorLogger().print(throwable);
                    getApi().reInitiateInstance();
                    setCacheMqttBrokerUrl(mqttBrokerUrl, false);
                });

    }

    /**
     * Use this method if we need application context instance
     *
     * @return Your application instance
     */
    public Application getApps() {
        checkAppIdSetup();
        return appInstance;
    }

    /**
     * AppId checker
     *
     * @throws RuntimeException
     */
    public void checkAppIdSetup() throws RuntimeException {
        if (appServer == null) {
            throw new RuntimeException("Please init Qiscus with your app id before!");
        }
    }

    /**
     * Accessor to get current qiscus app id
     *
     * @return Current app id
     */
    public String getAppId() {
        checkAppIdSetup();
        return appId;
    }

    /**
     * Accessor to get current qiscus app server
     *
     * @return Current qiscus app server
     */
    public String getAppServer() {
        checkAppIdSetup();
        return appServer;
    }

    /**
     * isEnableMqttLB
     * Checker for enable or disable own MQTT Load Balancer
     *
     * @return boolean
     */
    public boolean isEnableMqttLB() {
        checkAppIdSetup();
        return enableMqttLB;
    }

    /**
     * enableEventReport
     * Checker for enable or disable EventReport
     *
     * @return boolean
     */
    public boolean getEnableEventReport() {
        return enableEventReport;
    }

    /**
     * enableRealtime
     * Checker for enable or disable Realtime
     *
     * @return boolean
     */
    public boolean getEnableRealtime() {
        return enableRealtime;
    }

    /**
     * Accessor to get current mqtt broker url
     *
     * @return Current mqtt broker url
     */
    public String getMqttBrokerUrl() {
        checkAppIdSetup();

        if (localDataManager.getMqttBrokerUrl() == null) {
            localDataManager.setMqttBrokerUrl(mqttBrokerUrl);
        }

        return isEnableMqttLB() ? localDataManager.getMqttBrokerUrl() : mqttBrokerUrl;
    }

    /**
     * Accessor to get current mqtt broker url
     *
     * @return Current mqtt broker url
     */
    public String getBaseURLLB() {
        checkAppIdSetup();

        if (localDataManager.getURLLB() == null) {
            localDataManager.setURLLB(baseURLLB);
        }

        return isEnableMqttLB() ? localDataManager.getURLLB() : baseURLLB;
    }

    /**
     * this method is used if isEnableMqttLB() == true
     *
     * @param mqttBaseUrl
     */
    public void setCacheMqttBrokerUrl(String mqttBaseUrl, boolean everConnected) {
        localDataManager.setMqttBrokerUrl(mqttBaseUrl);
        localDataManager.setWillGetNewNodeMqttBrokerUrl(everConnected);
    }

    /**
     * this is mechanism used by MQTT LB
     *
     * @return boolean
     */
    public boolean willGetNewNodeMqttBrokerUrl() {
        return localDataManager.willGetNewNodeMqttBrokerUrl();
    }

    /**
     * Accessor to get current LocalDataManager
     *
     * @return current localDataManager
     */
    @Deprecated
    public LocalDataManager getLocalDataManager() {
        checkAppIdSetup();
        return localDataManager;
    }

    /**
     * Accessor to get SetUserBuilder
     *
     * @return current setUserBuider
     */
    public SetUserBuilder getSetUserBuilder() {
        return setUserBuilder;
    }

    /**
     * For checking is qiscus user has been setup
     *
     * @return true if already setup, false if not yet
     */
    public boolean hasSetupUser() {
        return appServer != null && localDataManager.isLogged();
    }

    /**
     * Accessor to get current qiscus user account
     *
     * @return Current qiscus user account
     */
    public QAccount getQiscusAccount() {
        checkUserSetup();
        return localDataManager.getAccountInfo();
    }

    /**
     * User Checker
     *
     * @throws RuntimeException
     */
    public void checkUserSetup() throws RuntimeException {
        checkAppIdSetup();
        if (!hasSetupUser()) {
            throw new RuntimeException("Please set Qiscus user before start the chatting!");
        }
    }

    /**
     * Accessor to get current qiscus user token
     *
     * @return Current qiscus user token
     */
    public String getToken() {
        checkUserSetup();
        return localDataManager.getToken();
    }

    /**
     * Get the current qiscus heartbeat duration
     *
     * @return Heartbeat duration in milliseconds
     */
    public long getHeartBeat() {
        return heartBeat;
    }

    /**
     * Set the heartbeat of qiscus synchronization chat data. Default value is 500ms
     *
     * @param heartBeat Heartbeat duration in milliseconds
     */
    @Deprecated
    public void setHeartBeat(long heartBeat) {
        checkAppIdSetup();
        this.heartBeat = heartBeat;
    }

    /**
     * Get the current qiscus automaticheartbeat duration (default 30s)
     *
     * @return automaticHeartbeat duration in milliseconds
     */
    public long getAutomaticHeartBeat() {
        return automaticHeartBeat;
    }

    /**
     * Set the syncInterval of qiscus synchronization chat data. Default value is 500ms
     *
     * @param interval Heartbeat duration in milliseconds
     */
    public void setSyncInterval(long interval) {
        checkAppIdSetup();
        this.heartBeat = interval;
    }

    /**
     * Get current qiscus chat data store
     *
     * @return Singleton of qiscus data store
     */
    public QiscusDataStore getDataStore() {
        return dataStore;
    }

    /**
     * Use this method if you want to use your own data store implementation, e.g using Realm, your own table,
     * your own orm, etc
     *
     * @param dataStore Your own chat datastore
     */
    public void setDataStore(QiscusDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Use this method to get current qiscus chatting configuration, you can also modify it.
     *
     * @return Current qiscus chatting configuration
     */
    public QiscusCoreChatConfig getChatConfig() {
        checkAppIdSetup();
        return chatConfig;
    }

    public void sendMessage(QMessage qMessage, OnSendMessageListener onCalbackSendMessage) {
        QAccount qAccount = getQiscusAccount();
        QUser qUser = new QUser();
        qUser.setAvatarUrl(qAccount.getAvatarUrl());
        qUser.setId(qAccount.getId());
        qUser.setExtras(qAccount.getExtras());
        qUser.setName(qAccount.getName());
        qMessage.setSender(qUser);

        onCalbackSendMessage.onSending(qMessage);

        getApi().sendMessage(qMessage)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<QMessage>() {
                    @Override
                    public void call(QMessage qMessage) {
                        onCalbackSendMessage.onSuccess(qMessage);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        onCalbackSendMessage.onFailed(throwable, qMessage);
                    }
                })
        ;
    }

    public void sendFileMessage(QMessage qMessage, File file, OnProgressUploadListener onProgressUploadListener) {
        QAccount qAccount = getQiscusAccount();
        QUser qUser = new QUser();
        qUser.setAvatarUrl(qAccount.getAvatarUrl());
        qUser.setId(qAccount.getId());
        qUser.setExtras(qAccount.getExtras());
        qUser.setName(qAccount.getName());
        qMessage.setSender(qUser);

        onProgressUploadListener.onSending(qMessage);

        getApi().sendFileMessage(qMessage, file, new QiscusApi.ProgressListener() {
            @Override
            public void onProgress(long total) {
                onProgressUploadListener.onProgress(total);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<QMessage>() {
                    @Override
                    public void call(QMessage qMessage) {
                        onProgressUploadListener.onSuccess(qMessage);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        onProgressUploadListener.onFailed(throwable, qMessage);
                    }
                });
    }

    /**
     * Use this method to call QiscusApi.
     *
     * @return Current QiscusApi
     */

    public QiscusApi getApi() {
        checkAppIdSetup();
        return qiscusMediator.getApi();
    }

    public QiscusLogger getLogger() {
        checkAppIdSetup();
        return qiscusMediator.getLogger();
    }

    public QiscusErrorLogger getErrorLogger() {
        checkAppIdSetup();
        return qiscusMediator.getErrorLogger();
    }

    public QiscusPusherApi getPusherApi() {
        checkAppIdSetup();
        return qiscusMediator.getPusherApi();
    }

    public QiscusAndroidUtil getAndroidUtil() {
        checkAppIdSetup();
        return qiscusMediator.getAndroidUtil();
    }

    public QiscusActivityCallback getActivityCallback() {
        checkAppIdSetup();
        return qiscusMediator.getActivityCallback();
    }

    public QiscusResendCommentHelper getQiscusResendCommentHelper() {
        checkAppIdSetup();
        return qiscusMediator.getResendCommentHelper();
    }

    public QiscusEventCache getEventCache() {
        checkAppIdSetup();
        return qiscusMediator.getEventCache();
    }

    public QiscusDeleteCommentHandler getDeleteCommentHandler() {
        checkAppIdSetup();
        return qiscusMediator.getDeleteCommentHandler();
    }

    public QiscusFirebaseMessagingUtil getFirebaseMessagingUtil() {
        checkAppIdSetup();
        return qiscusMediator.getFirebaseMessagingUtil();
    }

    public QiscusCacheManager getCacheManager() {
        checkAppIdSetup();
        return qiscusMediator.getCacheManager();
    }

    public QiscusClearCommentsHandler getClearCommentsHandler() {
        checkAppIdSetup();
        return qiscusMediator.getClearCommentsHandler();
    }

    /**
     * Use this method set qiscus user. If user doesn't exist at your qiscus engine, He/She will be
     * registered automatically.
     *
     * @param userEmail The email or username of qiscus user
     * @param userKey   Qiscus user key
     * @return User builder
     */
    public SetUserBuilder setUser(String userEmail, String userKey) {
        return new SetUserBuilder().withAccount(userEmail, userKey);
    }

    /**
     * Use this method to set qiscus user with jwt token from your apps backend
     *
     * @param token the jwt token
     * @return observable of qiscus account
     */
    @Deprecated
    public Observable<QAccount> setUserAsObservable(String token) {
        return getApi()
                .login(token)
                .doOnNext(qiscusAccount -> {
                    if (hasSetupUser()) {
                        localDataManager.saveAccountInfo(qiscusAccount);
                        configureFcmToken();
                    } else {
                        localDataManager.saveAccountInfo(qiscusAccount);
                        configureFcmToken();
                        EventBus.getDefault().post(QiscusUserEvent.LOGIN);
                    }
                });
    }

    /**
     * Use this method to set qiscus user with jwt token from your apps backend
     *
     * @param token the jwt token
     * @return observable of qiscus account
     */
    public Observable<QAccount> setUserWithIdentityToken(String token) {
        return getApi()
                .setUserWithIdentityToken(token)
                .doOnNext(qiscusAccount -> {
                    if (hasSetupUser()) {
                        localDataManager.saveAccountInfo(qiscusAccount);
                        configureFcmToken();
                    } else {
                        localDataManager.saveAccountInfo(qiscusAccount);
                        configureFcmToken();
                        EventBus.getDefault().post(QiscusUserEvent.LOGIN);
                    }
                });
    }

    /**
     * Use this method to set qiscus user with jwt token from your apps backend
     *
     * @param token    the jwt token
     * @param listener completion listener
     */
    @Deprecated
    public void setUser(String token, SetUserListener listener) {
        setUserWithIdentityToken(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onSuccess, listener::onError);
    }

    /**
     * Use this method to set qiscus user with jwt token from your apps backend
     *
     * @param token    the jwt token
     * @param listener completion listener
     */
    public void setUserWithIdentityToken(String token, SetUserListener listener) {
        setUserWithIdentityToken(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onSuccess, listener::onError);
    }

    /**
     * Use this method to update qiscus user data such as name and avatar
     *
     * @param name      user name
     * @param avatarUrl user avatar url
     * @param extras    user extras
     * @return observable of qiscus account
     */
    public Observable<QAccount> updateUserAsObservable(String name, String avatarUrl, JSONObject extras) {
        return getApi().updateUser(name, avatarUrl, extras)
                .doOnNext(qiscusAccount -> localDataManager.saveAccountInfo(qiscusAccount));
    }

    /**
     * Use this method to update qiscus user data such as name and avatar
     *
     * @param name      user name
     * @param avatarURL user avatar url
     * @param extras    user extras
     * @return observable of qiscus account
     */
    public Observable<QAccount> updateUser(String name, String avatarURL, JSONObject extras) {
        return getApi().updateUser(name, avatarURL, extras)
                .doOnNext(qiscusAccount -> localDataManager.saveAccountInfo(qiscusAccount));
    }

    /**
     * Use this method to update qiscus user data such as name and avatar
     *
     * @param name      user name
     * @param avatarUrl user avatar url
     * @return observable of qiscus account
     */
    public Observable<QAccount> updateUserAsObservable(String name, String avatarUrl) {
        return updateUserAsObservable(name, avatarUrl, null);
    }

    /**
     * Use this method to update qiscus user data such as name and avatar
     *
     * @param name      user name
     * @param avatarUrl user avatar url
     * @param extras    user extras
     * @param listener  completion listener
     */
    public void updateUser(String name, String avatarUrl, JSONObject extras, SetUserListener listener) {
        checkUserSetup();
        updateUser(name, avatarUrl, extras)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onSuccess, listener::onError);
    }

    /**
     * Use this method to update qiscus user data such as name and avatar
     *
     * @param name      user name
     * @param avatarUrl user avatar url
     * @param listener  completion listener
     */
    public void updateUser(String name, String avatarUrl, SetUserListener listener) {
        updateUser(name, avatarUrl, null, listener);
    }

    /**
     * Will return the exact apps name
     *
     * @return The apps name.
     */
    public String getAppsName() {
        checkAppIdSetup();
        return appInstance.getApplicationInfo().loadLabel(appInstance.getPackageManager()).toString();
    }

    /**
     * Needed to run something at main thread handler
     *
     * @return Main thread handler
     */
    public Handler getAppsHandler() {
        checkAppIdSetup();
        return appHandler;
    }

    public JSONObject getCustomHeader() {
        return customHeader;
    }

    /**
     * Use this method to set custom header
     *
     * @param customHeader custom header
     */
    public void setCustomHeader(JSONObject customHeader) {
        this.customHeader = customHeader;
    }

    /**
     * @return current fcm token, null if not set
     */
    public String getFcmToken() {
        return localDataManager.getFcmToken();
    }

    /**
     * Set the FCM token to configure push notification with firebase cloud messaging
     *
     * @param fcmToken the token
     */
    @Deprecated
    public void setFcmToken(String fcmToken) {
        if (hasSetupUser() && getChatConfig().isEnableFcmPushNotification()) {
            getApi().registerFcmToken(fcmToken)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aVoid -> {
                    }, throwable -> getErrorLogger().print("SetFCMToken", throwable));
        }

        localDataManager.setFcmToken(fcmToken);
    }

    /**
     * Set the FCM token to configure push notification with firebase cloud messaging
     *
     * @param token the token (fcmToken)
     */
    public void registerDeviceToken(String token) {
        if (hasSetupUser() && getChatConfig().isEnableFcmPushNotification()) {
            getApi().registerDeviceToken(token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aVoid -> {
                    }, throwable -> getErrorLogger().print("SetFCMToken", throwable));
        }

        localDataManager.setFcmToken(token);
    }

    /**
     * Remove the FCM token
     *
     * @param token the token (fcmToken)
     */
    public void removeDeviceToken(String token) {
        if (hasSetupUser()) {
            getApi().removeDeviceToken(token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aVoid -> {
                    }, throwable -> getErrorLogger().print("SetFCMToken", throwable));
        }

        localDataManager.setFcmToken(null);
    }

    private void configureFcmToken() {
        if (hasSetupUser() && getChatConfig().isEnableFcmPushNotification()) {
            String fcmToken = getFcmToken();
            if (fcmToken != null) {
                registerDeviceToken(fcmToken);
            } else {
                try {
                    FirebaseMessaging.getInstance().deleteToken();
                } catch ( IllegalStateException e) {
                    //Do nothing
                }
            }
        }
    }

    /**
     * Check is apps opened on foreground
     *
     * @return true if apps on foreground, and false if on background
     */
    public boolean isOnForeground() {
        return getActivityCallback().isForeground();
    }

    /**
     * Needed to run something at background thread handler
     *
     * @return ScheduledExecutorService instance
     */
    public ScheduledThreadPoolExecutor getTaskExecutor() {
        checkAppIdSetup();
        return taskExecutor;
    }

    /**
     * Clear all current user qiscus data, you can call this method when user logout for example.
     */
    public void clearUser() {
        if (BuildVersionUtil.isOreoOrHigher()) {
            JobScheduler jobScheduler = (JobScheduler) appInstance.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.cancelAll();
            }
        }
        localDataManager.clearData();
        dataStore.clear();
        getCacheManager().clearData();
        EventBus.getDefault().post(QiscusUserEvent.LOGOUT);
    }


    /**
     * openRealtimeConnection
     * Open realtime connection (manual)
     *
     * @return boolean
     */
    public Boolean openRealtimeConnection(){
        if (hasSetupUser() && getAndroidUtil().isNetworkAvailable() && getEnableRealtime()) {
            getLocalDataManager().setEnableDisableRealtimeManually(true);
            qiscusMediator.getPusherApi().restartConnection();
            qiscusMediator.getSyncTimer().startSchedule();
            return true;
        } else {
            return false;
        }
    }

    /**
     * closeRealtimeConnection
     * Close realtime connection (manual)
     *
     * @return boolean
     */
    public Boolean closeRealtimeConnection(){
        if (hasSetupUser()) {
            qiscusMediator.getPusherApi().disconnect();
            getLocalDataManager().setEnableDisableRealtimeManually(false);
            return true;
        } else {
            return false;
        }

    }

    public Boolean getStatusRealtimeEnableDisable(){
        return getLocalDataManager().getEnableDisableRealtimeManually();
    }

    public Boolean getStatusRealtimeIsConnected(){
        if (hasSetupUser()) {
            return qiscusMediator.getPusherApi().isConnected();
        } else {
            return false;
        }
    }


    public interface OnSendMessageListener {
        void onSending(QMessage qMessage);

        void onSuccess(QMessage qMessage);

        void onFailed(Throwable throwable, QMessage qMessage);
    }

    public interface OnProgressUploadListener {
        void onSending(QMessage qMessage);

        void onProgress(long progress);

        void onSuccess(QMessage qMessage);

        void onFailed(Throwable throwable, QMessage qMessage);
    }


    public interface SetUserListener {
        /**
         * Called if saving user succeed
         *
         * @param qiscusAccount Saved qiscus account
         */
        void onSuccess(QAccount qiscusAccount);

        /**
         * Called if error happened while saving qiscus user account. e.g network error
         *
         * @param throwable The cause of error
         */
        void onError(Throwable throwable);
    }

    private class LocalDataManager {
        private final SharedPreferences sharedPreferences;
        private final Gson gson;
        private String token;

        LocalDataManager(String localPrefKey) {
            sharedPreferences = getApps().getSharedPreferences(getAppId() + localPrefKey + "qiscus.cfg", Context.MODE_PRIVATE);
            gson = new Gson();
            token = isLogged() ? getAccountInfo().getToken() : "";
        }

        private boolean isLogged() {
            return sharedPreferences.contains("cached_account");
        }

        private void saveAccountInfo(QAccount qiscusAccount) {
            sharedPreferences.edit().putString("cached_account", gson.toJson(qiscusAccount)).apply();
            setToken(qiscusAccount.getToken());
        }

        private QAccount getAccountInfo() {
            return gson.fromJson(sharedPreferences.getString("cached_account", ""), QAccount.class);
        }

        private String getToken() {
            return token == null ? token = "" : token;
        }

        private void setToken(String token) {
            this.token = token;
        }

        private String getFcmToken() {
            return sharedPreferences.getString("fcm_token", null);
        }

        private void setFcmToken(String fcmToken) {
            sharedPreferences.edit().putString("fcm_token", fcmToken).apply();
        }

        /**
         * this is used if enableMqttLB = true
         *
         * @return mqttBrokerUrl
         */
        private String getMqttBrokerUrl() {
            return sharedPreferences.getString("mqtt_broker_url", null);
        }

        /**
         * this is used if enableMqttLB = true
         *
         * @param mqttBrokerUrl
         */
        private void setMqttBrokerUrl(String mqttBrokerUrl) {
            sharedPreferences.edit().putString("mqtt_broker_url", mqttBrokerUrl).apply();
        }

        /**
         * this is used if enableMqttLB = true
         *
         * @return UrlLB
         */
        private String getURLLB() {
            return sharedPreferences.getString("lb_url", null);
        }

        /**
         * this is used if enableMqttLB = true
         *
         * @param urlLb
         */
        private void setURLLB(String urlLb) {
            sharedPreferences.edit().putString("lb_url", urlLb).apply();
        }

        /**
         * Mechanism for MQTT LB
         *
         * @return boolean
         */
        private boolean willGetNewNodeMqttBrokerUrl() {
            return sharedPreferences.getBoolean("mqtt_will_get_new", true);
        }

        private void setWillGetNewNodeMqttBrokerUrl(boolean will) {
            sharedPreferences.edit().putBoolean("mqtt_will_get_new", will).apply();
        }

        private void setEnableDisableRealtimeManually(Boolean enableDisableRealtimeManually) {
            sharedPreferences.edit().putBoolean("realtime_enable_disable", enableDisableRealtimeManually).apply();
        }

        /**
         * save local sharedPref for enable / disable realtime (manual)
         *
         * @return mqttBrokerUrl
         */
        private Boolean getEnableDisableRealtimeManually() {
            return sharedPreferences.getBoolean("realtime_enable_disable", true);
        }


        private void clearData() {
            sharedPreferences.edit().clear().apply();
            setToken("");
        }
    }

    public class SetUserBuilder {
        private String email;
        private String password;
        private String username;
        private String avatarUrl;
        private JSONObject extras;

        public SetUserBuilder() {
        }

        public SetUserBuilder withAccount(String email, String password) {
            this.email = email;
            this.password = password;
            this.username = email;
            return this;
        }

        /**
         * Set the qiscus user name, if you not call this method, the username will be the same with
         * qiscus user email
         *
         * @param username The name
         * @return builder
         */
        public SetUserBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        /**
         * Set the avatar url for the qiscus user.
         *
         * @param avatarUrl The string url of avatar
         * @return builder
         */
        public SetUserBuilder withAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        /**
         * Set user extras to qiscus
         *
         * @param extras JSONObject
         * @return builder
         */
        public SetUserBuilder withExtras(JSONObject extras) {
            this.extras = extras;
            return this;
        }

        /**
         * Submit to qiscus engine and save the user account
         *
         * @param listener Listener of saving user process
         */
        public void save(SetUserListener listener) {
            save().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::onSuccess, listener::onError);
        }

        /**
         * Submit to qiscus engine and save the user account with RxJava style.
         *
         * @return Observable of Qiscus account
         */
        public Observable<QAccount> save() {
            return getApi()
                    .setUser(email, password, username, avatarUrl, extras)
                    .doOnNext(qiscusAccount -> {
                        if (hasSetupUser()) {
                            localDataManager.saveAccountInfo(qiscusAccount);
                            configureFcmToken();
                        } else {
                            localDataManager.saveAccountInfo(qiscusAccount);
                            configureFcmToken();
                            EventBus.getDefault().post(QiscusUserEvent.LOGIN);
                        }
                    });
        }
    }
}

/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.local.QiscusDataBaseHelper;
import com.qiscus.sdk.data.local.QiscusDataStore;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.event.QiscusUserEvent;
import com.qiscus.sdk.service.QiscusPusherService;
import com.qiscus.sdk.ui.QiscusChatActivity;
import com.qiscus.sdk.ui.fragment.QiscusChatFragment;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.one.EmojiOneProvider;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * The main class of Qiscus SDK. Init qiscus engine sdk, set qiscus user, start the chatting and all
 * about qiscus configuration can be accessed from here.
 */
public class Qiscus {

    private static Application appInstance;
    private static volatile Context applicationContext;
    private static volatile Handler appHandler;
    private static LocalDataManager localDataManager;
    private static QiscusDataStore dataStore;
    private static QiscusChatConfig chatConfig;

    private static String appServer;
    private static long heartBeat;
    private static String authorities;

    private Qiscus() {
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
     *      Qiscus.init(this, "yourQiscusAppId");
     *  }
     * }
     * }
     * </pre>
     *
     * @param application Application instance
     * @param qiscusAppId Your qiscus application Id
     */
    public static void init(Application application, String qiscusAppId) {
        initWithCustomServer(application, "https://" + qiscusAppId + ".qiscus.com");
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
     *      Qiscus.initWithCustomServer(this, "http://myserver.com");
     *  }
     * }
     * }
     * </pre>
     *
     * @param application   Application instance
     * @param serverBaseUrl Your qiscus chat engine base url
     */
    public static void initWithCustomServer(Application application, String serverBaseUrl) {
        appInstance = application;
        appServer = serverBaseUrl;
        applicationContext = appInstance.getApplicationContext();
        appHandler = new Handler(applicationContext.getMainLooper());
        localDataManager = new LocalDataManager();
        dataStore = new QiscusDataBaseHelper();
        chatConfig = new QiscusChatConfig();
        heartBeat = 60000;
        appInstance.registerActivityLifecycleCallbacks(QiscusActivityCallback.INSTANCE);
        authorities = appInstance.getPackageName() + ".qiscus.sdk.provider";

        startPusherService();
        QiscusCacheManager.getInstance().setLastChatActivity(false, 0);

        configureFcmToken();

        EmojiManager.install(new EmojiOneProvider());
    }

    public static void startPusherService() {
        checkAppIdSetup();
        applicationContext.startService(new Intent(applicationContext, QiscusPusherService.class));
    }

    /**
     * Use this method set qiscus user. If user doesn't exist at your qiscus engine, He/She will be
     * registered automatically.
     *
     * @param userEmail The email or username of qiscus user
     * @param userKey   Qiscus user key
     * @return User builder
     */
    public static SetUserBuilder setUser(String userEmail, String userKey) {
        return new SetUserBuilder(userEmail, userKey);
    }

    /**
     * Use this method if we need application context instance
     *
     * @return Your application instance
     */
    public static Application getApps() {
        checkAppIdSetup();
        return appInstance;
    }

    /**
     * Will return the exact apps name
     *
     * @return The apps name.
     */
    public static String getAppsName() {
        checkAppIdSetup();
        return appInstance.getApplicationInfo().loadLabel(appInstance.getPackageManager()).toString();
    }

    /**
     * Needed to run something at main thread handler
     *
     * @return Main thread handler
     */
    public static Handler getAppsHandler() {
        checkAppIdSetup();
        return appHandler;
    }

    /**
     * Accessor to get current qiscus app id
     *
     * @return Current qiscus app id
     */
    public static String getAppServer() {
        checkAppIdSetup();
        return appServer;
    }

    /**
     * For checking is qiscus user has been setup
     *
     * @return true if already setup, false if not yet
     */
    public static boolean hasSetupUser() {
        return localDataManager.isLogged();
    }

    /**
     * Accessor to get current qiscus user account
     *
     * @return Current qiscus user account
     */
    public static QiscusAccount getQiscusAccount() {
        checkUserSetup();
        return localDataManager.getAccountInfo();
    }

    /**
     * Accessor to get current qiscus user token
     *
     * @return Current qiscus user token
     */
    public static String getToken() {
        checkUserSetup();
        return localDataManager.getToken();
    }

    /**
     * Get current qiscus chat data store
     *
     * @return Singleton of qiscus data store
     */
    public static QiscusDataStore getDataStore() {
        return dataStore;
    }

    /**
     * Use this method if you want to use your own data store implementation, e.g using Realm, your own table,
     * your own orm, etc
     *
     * @param dataStore Your own chat datastore
     */
    public static void setDataStore(QiscusDataStore dataStore) {
        Qiscus.dataStore = dataStore;
    }

    /**
     * Use this method to get current qiscus chatting configuration, you can also modify it.
     *
     * @return Current qiscus chatting configuration
     */
    public static QiscusChatConfig getChatConfig() {
        checkAppIdSetup();
        return chatConfig;
    }

    /**
     * Use this method to open a chatting room with other user.
     *
     * @param email Email or username of other user to chat.
     * @return Chat room builder
     */
    public static ChatBuilder buildChatRoomWith(String email) {
        checkUserSetup();
        return new ChatBuilder(email);
    }

    /**
     * Use this method to start an Activity for chatting with other user.
     *
     * @param email Email or username of other user to chat.
     * @return Chatting Activity builder
     */
    public static ChatActivityBuilder buildChatWith(String email) {
        checkUserSetup();
        return new ChatActivityBuilder(email);
    }

    /**
     * Use this method if you want to generate a Fragment for chatting with other user.
     *
     * @param email Email or username of other user to chat.
     * @return Chatting Fragment builder
     */
    public static ChatFragmentBuilder buildChatFragmentWith(String email) {
        checkUserSetup();
        return new ChatFragmentBuilder(email);
    }

    /**
     * Use this method to create new group chat.
     *
     * @param email Email or username of group chat member.
     * @return Group Chat room builder
     */
    public static GroupChatBuilder buildGroupChatRoom(String name, String email) {
        checkUserSetup();
        return new GroupChatBuilder(name, email);
    }

    /**
     * Use this method to create new group chat.
     *
     * @param emails Emails or username of group chat member.
     * @return Group Chat room builder
     */
    public static GroupChatBuilder buildGroupChatRoom(String name, List<String> emails) {
        checkUserSetup();
        return new GroupChatBuilder(name, emails);
    }

    /**
     * Set the heartbeat of qiscus synchronization chat data. Default value is 500ms
     *
     * @param heartBeat Heartbeat duration in milliseconds
     */
    public static void setHeartBeat(long heartBeat) {
        checkAppIdSetup();
        Qiscus.heartBeat = heartBeat;
    }

    /**
     * Get the current qiscus heartbeat duration
     *
     * @return Heartbeat duration in milliseconds
     */
    public static long getHeartBeat() {
        return heartBeat;
    }

    /**
     * Set the FCM token to configure push notification with firebase cloud messaging
     *
     * @param fcmToken the token
     */
    public static void setFcmToken(String fcmToken) {
        if (hasSetupUser()) {
            QiscusApi.getInstance().registerFcmToken(fcmToken)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aVoid -> {
                    }, Throwable::printStackTrace);
        }

        localDataManager.setFcmToken(fcmToken);
    }

    /**
     * @return current fcm token, null if not set
     */
    public static String getFcmToken() {
        return localDataManager.getFcmToken();
    }

    private static void configureFcmToken() {
        if (hasSetupUser()) {
            String fcmToken = getFcmToken();
            if (fcmToken != null) {
                setFcmToken(fcmToken);
            } else {
                Observable.just(null)
                        .doOnNext(o -> {
                            try {
                                FirebaseInstanceId.getInstance().deleteInstanceId();
                            } catch (IOException ignored) {
                                //Do nothing
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aVoid -> {
                        }, throwable -> {
                        });
            }
        }
    }

    public static String getProviderAuthorities() {
        return authorities;
    }

    public static void setProviderAuthorities(String providerAuthorities) {
        authorities = providerAuthorities;
    }

    /**
     * Check is apps opened on foreground
     *
     * @return true if apps on foreground, and false if on background
     */
    public static boolean isOnForeground() {
        return QiscusActivityCallback.INSTANCE.isForeground();
    }

    private static void checkAppIdSetup() throws RuntimeException {
        if (appServer == null) {
            throw new RuntimeException("Please init Qiscus with your app id before!");
        }
    }

    private static void checkUserSetup() throws RuntimeException {
        checkAppIdSetup();
        if (!hasSetupUser()) {
            throw new RuntimeException("Please set Qiscus user before start the chatting!");
        }
    }

    /**
     * Clear all current user qiscus data, you can call this method when user logout for example.
     */
    public static void clearUser() {
        localDataManager.clearData();
        dataStore.clear();
        QiscusCacheManager.getInstance().clearData();
        EventBus.getDefault().post(QiscusUserEvent.LOGOUT);
    }

    private static class LocalDataManager {
        private final SharedPreferences sharedPreferences;
        private final Gson gson;
        private String token;

        LocalDataManager() {
            sharedPreferences = Qiscus.getApps().getSharedPreferences("qiscus.cfg", Context.MODE_PRIVATE);
            gson = new Gson();
            token = isLogged() ? getAccountInfo().getToken() : null;
        }

        private boolean isLogged() {
            return sharedPreferences.contains("cached_account");
        }

        private void saveAccountInfo(QiscusAccount qiscusAccount) {
            sharedPreferences.edit().putString("cached_account", gson.toJson(qiscusAccount)).apply();
            setToken(qiscusAccount.getToken());
        }

        private QiscusAccount getAccountInfo() {
            return gson.fromJson(sharedPreferences.getString("cached_account", ""), QiscusAccount.class);
        }

        private String getToken() {
            return token;
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

        private void clearData() {
            sharedPreferences.edit().clear().apply();
            setToken(null);
        }
    }

    public static class SetUserBuilder {
        private String email;
        private String password;
        private String username;
        private String avatarUrl;

        private SetUserBuilder(String email, String password) {
            this.email = email;
            this.password = password;
            this.username = email;
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
        public Observable<QiscusAccount> save() {
            return QiscusApi.getInstance()
                    .loginOrRegister(email, password, username, avatarUrl)
                    .doOnNext(qiscusAccount -> {
                        if (Qiscus.hasSetupUser()) {
                            Qiscus.localDataManager.saveAccountInfo(qiscusAccount);
                            configureFcmToken();
                        } else {
                            Qiscus.localDataManager.saveAccountInfo(qiscusAccount);
                            configureFcmToken();
                            EventBus.getDefault().post(QiscusUserEvent.LOGIN);
                        }
                    });
        }
    }

    public interface SetUserListener {
        /**
         * Called if saving user succeed
         *
         * @param qiscusAccount Saved qiscus account
         */
        void onSuccess(QiscusAccount qiscusAccount);

        /**
         * Called if error happened while saving qiscus user account. e.g network error
         *
         * @param throwable The cause of error
         */
        void onError(Throwable throwable);
    }

    public static class ChatBuilder {
        private String email;
        private String title;
        private String subtitle;
        private String distinctId;
        private String options;

        private ChatBuilder(String email) {
            title = "Chat";
            subtitle = "";
            this.email = email;
        }

        /**
         * Set the title of of chat. Default id "Chat"
         *
         * @param title The title of chat room
         * @return builder
         */
        public ChatBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the subtitle of chat
         *
         * @param subtitle The subtitle of chat
         * @return builder
         */
        public ChatBuilder withSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        /**
         * If you need different room for those users
         *
         * @param distinctId The unique id for every rooms
         * @return builder
         */
        public ChatBuilder withDistinctId(String distinctId) {
            this.distinctId = distinctId;
            return this;
        }

        /**
         * If you need to save options or extra data to this room
         *
         * @param options The data need to save
         * @return builder
         */
        public ChatBuilder withOptions(String options) {
            this.options = options;
            return this;
        }

        /**
         * Build the chat room
         *
         * @param listener Listener of building chat room process
         */
        public void build(ChatBuilderListener listener) {
            build().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::onSuccess, listener::onError);
        }

        /**
         * Build the chat room as Observable
         *
         * @return Observable chat room
         */
        public Observable<QiscusChatRoom> build() {
            return QiscusApi.getInstance()
                    .getChatRoom(email, distinctId, options)
                    .doOnNext(qiscusChatRoom -> {
                        qiscusChatRoom.setName(title);
                        qiscusChatRoom.setSubtitle(subtitle);
                    })
                    .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom));
        }
    }

    public interface ChatBuilderListener {
        /**
         * Called if building chat room succeed
         *
         * @param qiscusChatRoom Built chat room
         */
        void onSuccess(QiscusChatRoom qiscusChatRoom);

        /**
         * Called if error happened while building chat room. e.g network error
         *
         * @param throwable The cause of error
         */
        void onError(Throwable throwable);
    }

    public static class ChatActivityBuilder {
        private String email;
        private String title;
        private String subtitle;
        private String distinctId;
        private String options;
        private String message;

        private ChatActivityBuilder(String email) {
            title = "Chat";
            subtitle = "";
            this.email = email;
        }

        /**
         * Set the title of of chat activity. Default id "Chat"
         *
         * @param title The title of chat room
         * @return builder
         */
        public ChatActivityBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the subtitle of chat activity
         *
         * @param subtitle The subtitle of chat
         * @return builder
         */
        public ChatActivityBuilder withSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        /**
         * If you need different room for those users
         *
         * @param distinctId The unique id for every rooms
         * @return builder
         */
        public ChatActivityBuilder withDistinctId(String distinctId) {
            this.distinctId = distinctId;
            return this;
        }

        /**
         * If you need to save options or extra data to this room
         *
         * @param options The data need to save
         * @return builder
         */
        public ChatActivityBuilder withOptions(String options) {
            this.options = options;
            return this;
        }

        /**
         * If you want to automatically send a message after the activity started
         *
         * @param message The message
         * @return builder
         */
        public ChatActivityBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Build the Chat activity intent
         *
         * @param context  Context for start the Activity
         * @param listener Listener of building chat activity
         */
        public void build(Context context, ChatActivityBuilderListener listener) {
            build(context).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::onSuccess, listener::onError);
        }

        /**
         * Build the chat activity as observable
         *
         * @param context Context to start the activity
         * @return Observable of Intent for start chat activity
         */
        public Observable<Intent> build(Context context) {
            return QiscusApi.getInstance()
                    .getChatRoom(email, distinctId, options)
                    .doOnNext(qiscusChatRoom -> {
                        qiscusChatRoom.setName(title);
                        qiscusChatRoom.setSubtitle(subtitle);
                    })
                    .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom))
                    .map(qiscusChatRoom -> {
                        if (message == null || message.isEmpty()) {
                            return QiscusChatActivity.generateIntent(context, qiscusChatRoom);
                        }
                        return QiscusChatActivity.generateIntent(context, qiscusChatRoom, message);
                    });
        }
    }

    public interface ChatActivityBuilderListener {
        /**
         * Called if building Chat Activity succeed
         *
         * @param intent Intent for start chat activity
         */
        void onSuccess(Intent intent);

        /**
         * Called if error happened while building chat activity. e.g network error
         *
         * @param throwable The cause of error
         */
        void onError(Throwable throwable);
    }

    public static class ChatFragmentBuilder {
        private String email;
        private String title;
        private String subtitle;
        private String distinctId;
        private String options;
        private String message;

        private ChatFragmentBuilder(String email) {
            title = "Chat";
            subtitle = "";
            this.email = email;
        }

        /**
         * Set the title of of chat. Default id "Chat"
         *
         * @param title The title of chat room
         * @return builder
         */
        public ChatFragmentBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the subtitle of chat
         *
         * @param subtitle The subtitle of chat
         * @return builder
         */
        public ChatFragmentBuilder withSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        /**
         * If you need different room for those users
         *
         * @param distinctId The unique id for every rooms
         * @return builder
         */
        public ChatFragmentBuilder withDistinctId(String distinctId) {
            this.distinctId = distinctId;
            return this;
        }

        /**
         * If you need to save options or extra data to this room
         *
         * @param options The data need to save
         * @return builder
         */
        public ChatFragmentBuilder withOptions(String options) {
            this.options = options;
            return this;
        }

        /**
         * If you want to automatically send a message after the activity started
         *
         * @param message The message
         * @return builder
         */
        public ChatFragmentBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Build the Chat fragment instance
         *
         * @param listener Listener of building chat fragment
         */
        public void build(ChatFragmentBuilderListener listener) {
            build().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::onSuccess, listener::onError);
        }

        /**
         * Build the chat fragment as observable
         *
         * @return Observable of chat fragment
         */
        public Observable<QiscusChatFragment> build() {
            return QiscusApi.getInstance()
                    .getChatRoom(email, distinctId, options)
                    .doOnNext(qiscusChatRoom -> {
                        qiscusChatRoom.setName(title);
                        qiscusChatRoom.setSubtitle(subtitle);
                    })
                    .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom))
                    .map(qiscusChatRoom -> {
                        if (message == null || message.isEmpty()) {
                            return QiscusChatFragment.newInstance(qiscusChatRoom);
                        }
                        return QiscusChatFragment.newInstance(qiscusChatRoom, message);
                    });
        }
    }

    public interface ChatFragmentBuilderListener {
        /**
         * Called if building Chat Fragment succeed
         *
         * @param qiscusChatFragment Chat Fragment instance
         */
        void onSuccess(QiscusChatFragment qiscusChatFragment);

        /**
         * Called if error happened while building chat fragment. e.g network error
         *
         * @param throwable The cause of error
         */
        void onError(Throwable throwable);
    }

    public static class GroupChatBuilder {
        private Set<String> emails;
        private String name;
        private String options;
        private String avatarUrl;

        private GroupChatBuilder(String name, String email) {
            this.name = name;
            emails = new HashSet<>();
            emails.add(email);
        }

        private GroupChatBuilder(String name, List<String> emails) {
            this.name = name;
            this.emails = new HashSet<>(emails);
        }

        /**
         * Adding initial group chat members
         *
         * @param email qiscus member
         * @return builder
         */
        public GroupChatBuilder addEmail(String email) {
            emails.add(email);
            return this;
        }

        /**
         * If you need to set the avatar or picture of group chat room
         *
         * @param avatarUrl the picture url
         * @return builder
         */
        public GroupChatBuilder withAvatar(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        /**
         * If you need to save options or extra data to this room
         *
         * @param options The data need to save
         * @return builder
         */
        public GroupChatBuilder withOptions(String options) {
            this.options = options;
            return this;
        }

        /**
         * Build the chat room
         *
         * @param listener Listener of building chat room process
         */
        public void build(ChatBuilderListener listener) {
            build().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::onSuccess, listener::onError);
        }

        /**
         * Build the chat room as Observable
         *
         * @return Observable chat room
         */
        public Observable<QiscusChatRoom> build() {
            return QiscusApi.getInstance()
                    .createGroupChatRoom(name, new ArrayList<>(emails), avatarUrl, options)
                    .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom));
        }
    }
}

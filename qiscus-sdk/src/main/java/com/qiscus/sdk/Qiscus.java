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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * The main class of Qiscus SDK. Init qiscus engine sdk, set qiscus user, start the chatting and all
 * about qiscus configuration can be accessed from here.
 */
public class Qiscus {

    private static Application APP_INSTANCE;
    private static volatile Handler APP_HANDLER;
    private static LocalDataManager LOCAL_DATA_MANAGER;
    private static QiscusDataStore DATA_STORE;
    private static QiscusChatConfig CHAT_CONFIG;

    private static String APP_SERVER;
    private static long HEART_BEAT;

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
        initWithCustomServer(application, "http://" + qiscusAppId + ".qiscus.com");
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
        APP_INSTANCE = application;
        APP_SERVER = serverBaseUrl;
        APP_HANDLER = new Handler(APP_INSTANCE.getMainLooper());
        LOCAL_DATA_MANAGER = new LocalDataManager();
        DATA_STORE = new QiscusDataBaseHelper();
        CHAT_CONFIG = new QiscusChatConfig();
        HEART_BEAT = 5000;

        APP_INSTANCE.startService(new Intent(APP_INSTANCE, QiscusPusherService.class));
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
        return APP_INSTANCE;
    }

    /**
     * Will return the exact apps name
     *
     * @return The apps name.
     */
    public static String getAppsName() {
        checkAppIdSetup();
        return APP_INSTANCE.getApplicationInfo().loadLabel(APP_INSTANCE.getPackageManager()).toString();
    }

    /**
     * Needed to run something at main thread handler
     *
     * @return Main thread handler
     */
    public static Handler getAppsHandler() {
        checkAppIdSetup();
        return APP_HANDLER;
    }

    /**
     * Accessor to get current qiscus app id
     *
     * @return Current qiscus app id
     */
    public static String getAppServer() {
        checkAppIdSetup();
        return APP_SERVER;
    }

    /**
     * For checking is qiscus user has been setup
     *
     * @return true if already setup, false if not yet
     */
    public static boolean hasSetupUser() {
        return LOCAL_DATA_MANAGER.isLogged();
    }

    /**
     * Accessor to get current qiscus user account
     *
     * @return Current qiscus user account
     */
    public static QiscusAccount getQiscusAccount() {
        checkUserSetup();
        return LOCAL_DATA_MANAGER.getAccountInfo();
    }

    /**
     * Accessor to get current qiscus user token
     *
     * @return Current qiscus user token
     */
    public static String getToken() {
        checkUserSetup();
        return LOCAL_DATA_MANAGER.getToken();
    }

    /**
     * Get current qiscus chat data store
     *
     * @return Singleton of qiscus data store
     */
    public static QiscusDataStore getDataStore() {
        return DATA_STORE;
    }

    /**
     * Use this method if you want to use your own data store implementation, e.g using Realm, your own table,
     * your own orm, etc
     *
     * @param dataStore Your own chat datastore
     */
    public static void setDataStore(QiscusDataStore dataStore) {
        DATA_STORE = dataStore;
    }

    /**
     * Use this method to get current qiscus chatting configuration, you can also modify it.
     *
     * @return Current qiscus chatting configuration
     */
    public static QiscusChatConfig getChatConfig() {
        checkAppIdSetup();
        return CHAT_CONFIG;
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
     * Set the heartbeat of qiscus synchronization chat data. Default value is 500ms
     *
     * @param heartBeat Heartbeat duration in milliseconds
     */
    public static void setHeartBeat(long heartBeat) {
        checkAppIdSetup();
        HEART_BEAT = heartBeat;
    }

    /**
     * Get the current qiscus heartbeat duration
     *
     * @return Heartbeat duration in milliseconds
     */
    public static long getHeartBeat() {
        return HEART_BEAT;
    }

    private static void checkAppIdSetup() throws RuntimeException {
        if (APP_SERVER == null) {
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
        LOCAL_DATA_MANAGER.clearData();
        DATA_STORE.clear();
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
                        Qiscus.LOCAL_DATA_MANAGER.saveAccountInfo(qiscusAccount);
                        EventBus.getDefault().post(QiscusUserEvent.LOGIN);
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
        private Set<String> emails;
        private String distinctId;
        private String options;

        private ChatBuilder(String email) {
            emails = new HashSet<>();
            emails.add(email);
        }

        /**
         * Add more qiscus user email to the chat room. For group chat.
         *
         * @param email Qiscus user email
         * @return builder
         */
        public ChatBuilder addEmail(String email) {
            emails.add(email);
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
                    .getChatRoom(new ArrayList<>(emails), distinctId, options)
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
        private Set<String> emails;
        private String title;
        private String subtitle;
        private String distinctId;
        private String options;

        private ChatActivityBuilder(String email) {
            emails = new HashSet<>();
            title = "Chat";
            subtitle = "";
            emails.add(email);
        }

        /**
         * Add more qiscus user email to the chat room. For group chat.
         *
         * @param email Qiscus user email
         * @return builder
         */
        public ChatActivityBuilder addEmail(String email) {
            emails.add(email);
            return this;
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
                    .getChatRoom(new ArrayList<>(emails), distinctId, options)
                    .doOnNext(qiscusChatRoom -> {
                        qiscusChatRoom.setName(title);
                        qiscusChatRoom.setSubtitle(subtitle);
                    })
                    .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom))
                    .map(qiscusChatRoom -> QiscusChatActivity.generateIntent(context, qiscusChatRoom));
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
        private Set<String> emails;
        private String distinctId;
        private String options;

        private ChatFragmentBuilder(String email) {
            emails = new HashSet<>();
            emails.add(email);
        }

        /**
         * Add more qiscus user email to the chat room. For group chat.
         *
         * @param email Qiscus user email
         * @return builder
         */
        public ChatFragmentBuilder addEmail(String email) {
            emails.add(email);
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
                    .getChatRoom(new ArrayList<>(emails), distinctId, options)
                    .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom))
                    .map(QiscusChatFragment::newInstance);
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
}

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
import android.os.Handler;

import com.qiscus.jupuk.Jupuk;
import com.qiscus.sdk.chat.core.BuildConfig;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.util.QiscusLogger;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.ui.QiscusChatActivity;
import com.qiscus.sdk.ui.fragment.QiscusChatFragment;
import com.qiscus.sdk.util.QiscusPushNotificationUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.qiscus.sdk.chat.core.QiscusCore.checkAppIdSetup;
import static com.qiscus.sdk.chat.core.QiscusCore.checkUserSetup;

/**
 * The main class of Qiscus SDK. Init qiscus engine sdk, set qiscus user, start the chatting and all
 * about qiscus configuration can be accessed from here.
 */
public class Qiscus {

    private static QiscusChatConfig chatConfig;
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
        initWithCustomServer(application, qiscusAppId, BuildConfig.BASE_URL_SERVER, BuildConfig.BASE_URL_MQTT_BROKER);
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
     *      Qiscus.initWithCustomServer(this, my-app-id, "http://myserver.com/", "ssl://mqtt.myserver.com:1885");
     *  }
     * }
     * }
     * </pre>
     *
     * @param application   Application instance
     * @param qiscusAppId   Your Qiscus App Id
     * @param serverBaseUrl Your qiscus chat engine base url
     * @param mqttBrokerUrl Your Mqtt Broker url
     */
    public static void initWithCustomServer(Application application, String qiscusAppId, String serverBaseUrl, String mqttBrokerUrl) {
        QiscusCore.initWithCustomServer(application, qiscusAppId, serverBaseUrl, mqttBrokerUrl);
        chatConfig = new QiscusChatConfig();
        authorities = QiscusCore.getApps().getPackageName() + ".qiscus.sdk.provider";
        QiscusCacheManager.getInstance().setLastChatActivity(false, 0);

        Jupuk.init(application);
        QiscusLogger.print("init Qiscus with app Id " + QiscusCore.getAppId());

        QiscusCore.getChatConfig()
                .setNotificationListener(QiscusPushNotificationUtil::handlePushNotification)
                .setDeleteCommentListener(QiscusPushNotificationUtil::handleDeletedCommentNotification);
    }

    /**
     * Use this method set qiscus user. If user doesn't exist at your qiscus engine, He/She will be
     * registered automatically.
     *
     * @param userEmail The email or username of qiscus user
     * @param userKey   Qiscus user key
     * @return User builder
     */
    public static QiscusCore.SetUserBuilder setUser(String userEmail, String userKey) {
        return QiscusCore.setUser(userEmail, userKey);
    }


    /**
     * Use this method to set qiscus user with jwt token from your apps backend
     *
     * @param token the jwt token
     * @return observable of qiscus account
     */
    public static Observable<QiscusAccount> setUserAsObservable(String token) {
        return QiscusCore.setUserAsObservable(token);
    }

    /**
     * Use this method to set qiscus user with jwt token from your apps backend
     *
     * @param token    the jwt token
     * @param listener completion listener
     */
    public static void setUser(String token, QiscusCore.SetUserListener listener) {
        QiscusCore.setUser(token, listener);
    }

    /**
     * Use this method to update qiscus user data such as name and avatar
     *
     * @param name      user name
     * @param avatarUrl user avatar url
     * @return observable of qiscus account
     */
    public static Observable<QiscusAccount> updateUserAsObservable(String name, String avatarUrl) {
        return QiscusCore.updateUserAsObservable(name, avatarUrl);
    }

    /**
     * Use this method to update qiscus user data such as name and avatar
     *
     * @param name      user name
     * @param avatarUrl user avatar url
     * @param listener  completion listener
     */
    public static void updateUser(String name, String avatarUrl, QiscusCore.SetUserListener listener) {
        QiscusCore.updateUser(name, avatarUrl, listener);
    }

    /**
     * Use this method if we need application context instance
     *
     * @return Your application instance
     */
    public static Application getApps() {
        return QiscusCore.getApps();
    }

    /**
     * Will return the exact apps name
     *
     * @return The apps name.
     */
    public static String getAppsName() {
        return QiscusCore.getAppsName();
    }

    /**
     * Needed to run something at main thread handler
     *
     * @return Main thread handler
     */
    public static Handler getAppsHandler() {
        return QiscusCore.getAppsHandler();
    }

    /**
     * Needed to run something at background thread handler
     *
     * @return ScheduledExecutorService instance
     */
    public static ScheduledThreadPoolExecutor getTaskExecutor() {
        return QiscusCore.getTaskExecutor();
    }

    /**
     * Accessor to get current qiscus app id
     *
     * @return Current app id
     */
    public static String getAppId() {
        return QiscusCore.getAppId();
    }

    /**
     * Accessor to get current qiscus app server
     *
     * @return Current qiscus app server
     */
    public static String getAppServer() {
        return QiscusCore.getAppServer();
    }

    /**
     * Accessor to get current mqtt broker url
     *
     * @return Current mqtt broker url
     */
    public static String getMqttBrokerUrl() {
        return QiscusCore.getMqttBrokerUrl();
    }

    /**
     * For checking is qiscus user has been setup
     *
     * @return true if already setup, false if not yet
     */
    public static boolean hasSetupUser() {
        return QiscusCore.hasSetupUser();
    }

    /**
     * Accessor to get current qiscus user account
     *
     * @return Current qiscus user account
     */
    public static QiscusAccount getQiscusAccount() {
        return QiscusCore.getQiscusAccount();
    }

    /**
     * Accessor to get current qiscus user token
     *
     * @return Current qiscus user token
     */
    public static String getToken() {
        return QiscusCore.getToken();
    }

    /**
     * Get current qiscus chat data store
     *
     * @return Singleton of qiscus data store
     */
    public static QiscusDataStore getDataStore() {
        return QiscusCore.getDataStore();
    }

    /**
     * Use this method if you want to use your own data store implementation, e.g using Realm, your own table,
     * your own orm, etc
     *
     * @param dataStore Your own chat datastore
     */
    public static void setDataStore(QiscusDataStore dataStore) {
        QiscusCore.setDataStore(dataStore);
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
     * Use this method to create or join specific room chat with defined unique id.
     *
     * @param channelName the room channel name (must be unique).
     * @return Defined Id Group Chat room builder
     */
    public static DefinedIdGroupChatBuilder buildGroupChatRoomWith(String channelName) {
        checkUserSetup();
        return new DefinedIdGroupChatBuilder(channelName);
    }

    /**
     * Get the current qiscus heartbeat duration
     *
     * @return Heartbeat duration in milliseconds
     */
    public static long getHeartBeat() {
        return QiscusCore.getHeartBeat();
    }

    /**
     * Set the heartbeat of qiscus synchronization chat data. Default value is 500ms
     *
     * @param heartBeat Heartbeat duration in milliseconds
     */
    public static void setHeartBeat(long heartBeat) {
        QiscusCore.setHeartBeat(heartBeat);
    }

    /**
     * @return current fcm token, null if not set
     */
    public static String getFcmToken() {
        return QiscusCore.getFcmToken();
    }

    /**
     * Set the FCM token to configure push notification with firebase cloud messaging
     *
     * @param fcmToken the token
     */
    public static void setFcmToken(String fcmToken) {
        QiscusCore.setFcmToken(fcmToken);
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
        return QiscusCore.isOnForeground();
    }

    /**
     * Clear all current user qiscus data, you can call this method when user logout for example.
     */
    public static void clearUser() {
        QiscusCore.clearUser();
    }

    /**
     * Get the log qiscus
     *
     * @return enableLog status in boolean
     */
    @Deprecated
    public static boolean isEnableLog() {
        return QiscusCore.getChatConfig().isEnableLog();
    }

    /**
     * Set the log of qiscus data. Default value is false
     *
     * @param enableLog boolean
     */
    @Deprecated
    public static void setEnableLog(boolean enableLog) {
        QiscusCore.getChatConfig().setEnableLog(enableLog);
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

    public static class ChatBuilder {
        private String email;
        private String distinctId;
        private JSONObject options;

        private ChatBuilder(String email) {
            this.email = email;
        }

        /**
         * Set the title of of chat. Default id "Chat"
         *
         * @param title The title of chat room
         * @return builder
         * @deprecated 1-1 chat title will always the opponent user name
         */
        @Deprecated
        public ChatBuilder withTitle(String title) {
            return this;
        }

        /**
         * Set the subtitle of chat
         *
         * @param subtitle The subtitle of chat
         * @return builder
         * @deprecated to create sub title, please create your won toolbar
         */
        @Deprecated
        public ChatBuilder withSubtitle(String subtitle) {
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
        public ChatBuilder withOptions(JSONObject options) {
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
                    .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom));
        }
    }

    public static class ChatActivityBuilder {
        private String email;
        private String distinctId;
        private JSONObject options;
        private String message;
        private List<File> shareFiles;
        private boolean autoSendExtra;
        private List<QiscusComment> comments;
        private QiscusComment scrollToComment;

        private ChatActivityBuilder(String email) {
            this.email = email;
            autoSendExtra = true;
            shareFiles = new ArrayList<>();
        }

        /**
         * Set the title of of chat activity. Default id "Chat"
         *
         * @param title The title of chat room
         * @return builder
         * @deprecated 1-1 chat title will always the opponent user name
         */
        @Deprecated
        public ChatActivityBuilder withTitle(String title) {
            return this;
        }

        /**
         * Set the subtitle of chat activity
         *
         * @param subtitle The subtitle of chat
         * @return builder
         * @deprecated to create sub title, please create your won toolbar
         */
        @Deprecated
        public ChatActivityBuilder withSubtitle(String subtitle) {
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
        public ChatActivityBuilder withOptions(JSONObject options) {
            this.options = options;
            return this;
        }

        /**
         * If you want to share a message after the activity started
         *
         * @param message The message
         * @return builder
         */
        public ChatActivityBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * If you want to share a file message after the activity started
         *
         * @param shareFile The file
         * @return builder
         */
        public ChatActivityBuilder withShareFile(File shareFile) {
            shareFiles.add(shareFile);
            return this;
        }

        /**
         * If you want to automatically send extra message (text or file sharing) after the activity started
         *
         * @param autoSendExtra The flag, default is true
         * @return builder
         */
        public ChatActivityBuilder withAutoSendExtra(boolean autoSendExtra) {
            this.autoSendExtra = autoSendExtra;
            return this;
        }

        /**
         * If you want to forward comments after the activity started
         *
         * @param comments The list of comment
         * @return builder
         */
        public ChatActivityBuilder withForwardComments(List<QiscusComment> comments) {
            this.comments = comments;
            return this;
        }

        /**
         * If you want to auto scroll to specific comment after the activity started
         * for example click comment from searching page
         *
         * @param comment The comment
         * @return builder
         */
        public ChatActivityBuilder withScrollToComment(QiscusComment comment) {
            this.scrollToComment = comment;
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
                    .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom))
                    .map(qiscusChatRoom ->
                            QiscusChatActivity.generateIntent(context, qiscusChatRoom, message,
                                    shareFiles, autoSendExtra, comments, scrollToComment));
        }
    }

    public static class ChatFragmentBuilder {
        private String email;
        private String distinctId;
        private JSONObject options;
        private String message;
        private List<File> shareFiles;
        private boolean autoSendExtra;
        private List<QiscusComment> comments;
        private QiscusComment scrollToComment;

        private ChatFragmentBuilder(String email) {
            this.email = email;
            autoSendExtra = true;
            shareFiles = new ArrayList<>();
        }

        /**
         * Set the title of of chat. Default id "Chat"
         *
         * @param title The title of chat room
         * @return builder
         * @deprecated 1-1 chat title will always the opponent user name
         */
        @Deprecated
        public ChatFragmentBuilder withTitle(String title) {
            return this;
        }

        /**
         * Set the subtitle of chat
         *
         * @param subtitle The subtitle of chat
         * @return builder
         * @deprecated to create sub title, please create your won toolbar
         */
        @Deprecated
        public ChatFragmentBuilder withSubtitle(String subtitle) {
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
        public ChatFragmentBuilder withOptions(JSONObject options) {
            this.options = options;
            return this;
        }

        /**
         * If you want to share a message after the activity started
         *
         * @param message The message
         * @return builder
         */
        public ChatFragmentBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * If you want to share a file message after the activity started
         *
         * @param shareFile The file
         * @return builder
         */
        public ChatFragmentBuilder withShareFile(File shareFile) {
            shareFiles.add(shareFile);
            return this;
        }

        /**
         * If you want to automatically send extra message (text or file sharing) after the activity started
         *
         * @param autoSendExtra The flag, default is true
         * @return builder
         */
        public ChatFragmentBuilder withAutoSendExtra(boolean autoSendExtra) {
            this.autoSendExtra = autoSendExtra;
            return this;
        }

        /**
         * If you want to forward comments after the activity started
         *
         * @param comments The list of comment
         * @return builder
         */
        public ChatFragmentBuilder withForwardComments(List<QiscusComment> comments) {
            this.comments = comments;
            return this;
        }

        /**
         * If you want to auto scroll to specific comment after the activity started
         * for example click comment from searching page
         *
         * @param comment The comment
         * @return builder
         */
        public ChatFragmentBuilder withScrollToComment(QiscusComment comment) {
            this.scrollToComment = comment;
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
                    .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom))
                    .map(qiscusChatRoom ->
                            QiscusChatFragment.newInstance(qiscusChatRoom, message, shareFiles,
                                    autoSendExtra, comments, scrollToComment));
        }
    }

    public static class GroupChatBuilder {
        private Set<String> emails;
        private String name;
        private JSONObject options;
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
        public GroupChatBuilder withOptions(JSONObject options) {
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

    public static class DefinedIdGroupChatBuilder {
        private String uniqueId;
        private String name;
        private JSONObject options;
        private String avatarUrl;

        private DefinedIdGroupChatBuilder(String uniqueId) {
            this.uniqueId = uniqueId;
            this.name = uniqueId;
        }

        /**
         * If you need to set the room name
         *
         * @param name the room name
         * @return builder
         */
        public DefinedIdGroupChatBuilder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * If you need to set the avatar or picture of group chat room
         *
         * @param avatarUrl the picture url
         * @return builder
         */
        public DefinedIdGroupChatBuilder withAvatar(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        /**
         * If you need to save options or extra data to this room
         *
         * @param options The data need to save
         * @return builder
         */
        public DefinedIdGroupChatBuilder withOptions(JSONObject options) {
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
                    .getGroupChatRoom(uniqueId, name, avatarUrl, options)
                    .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom));
        }
    }

}

package com.qiscus.sdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.google.gson.Gson;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.local.QiscusDataBaseHelper;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.ui.QiscusChatActivity;
import com.qiscus.sdk.util.QiscusParser;
import com.qiscus.sdk.util.QiscusScheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import rx.Observable;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class Qiscus {
    private static Application APP_INSTANCE;
    private static volatile Handler APP_HANDLER;
    private static LocalDataManager LOCAL_DATA_MANAGER;
    private static QiscusChatConfig CHAT_CONFIG;

    private static String API_URL;

    public static void init(Application application, String qiscusApiUrl) {
        APP_INSTANCE = application;
        API_URL = qiscusApiUrl;
        APP_HANDLER = new Handler(APP_INSTANCE.getMainLooper());
        LOCAL_DATA_MANAGER = new LocalDataManager();
        CHAT_CONFIG = new QiscusChatConfig();
    }

    public static LoginBuilder with(String email, String password) {
        return new LoginBuilder(email, password);
    }

    public static Application getApps() {
        return APP_INSTANCE;
    }

    public static String getAppsName() {
        return APP_INSTANCE.getApplicationInfo().loadLabel(APP_INSTANCE.getPackageManager()).toString();
    }

    public static Handler getAppsHandler() {
        return APP_HANDLER;
    }

    public static String getApiUrl() {
        return API_URL;
    }

    public static boolean isLogged() {
        return LOCAL_DATA_MANAGER.isLogged();
    }

    public static QiscusAccount getQiscusAccount() {
        return LOCAL_DATA_MANAGER.getAccountInfo();
    }

    public static String getToken() {
        return LOCAL_DATA_MANAGER.getToken();
    }

    public static QiscusChatConfig getChatConfig() {
        return CHAT_CONFIG;
    }

    public static ChatActivityBuilder buildChatWith(String email) {
        return new ChatActivityBuilder(email);
    }

    public static void logout() {
        LOCAL_DATA_MANAGER.clearData();
        QiscusDataBaseHelper.getInstance().clear();
        QiscusCacheManager.getInstance().clearData();
    }

    private static class LocalDataManager {
        private final SharedPreferences sharedPreferences;
        private final Gson gson;
        private String token;

        LocalDataManager() {
            sharedPreferences = Qiscus.getApps().getSharedPreferences("qiscus.cfg", Context.MODE_PRIVATE);
            gson = QiscusParser.get().parser();
            token = isLogged() ? getAccountInfo().getToken() : "";
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
        }
    }

    public static class LoginBuilder {
        private String email;
        private String password;
        private String username;
        private String avatarUrl;

        private LoginBuilder(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public LoginBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public LoginBuilder withAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public void login(LoginListener listener) {
            login().compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                    .subscribe(listener::onSuccess, listener::onError);
        }

        public Observable<QiscusAccount> login() {
            return QiscusApi.getInstance()
                    .loginOrRegister(email, password, username, avatarUrl)
                    .doOnNext(qiscusAccount -> {
                        Qiscus.LOCAL_DATA_MANAGER.saveAccountInfo(qiscusAccount);
                    });
        }
    }

    public interface LoginListener {
        void onSuccess(QiscusAccount qiscusAccount);

        void onError(Throwable throwable);
    }

    public static class ChatActivityBuilder {
        private Set<String> emails;
        private String title;
        private String subtitle;
        private QiscusChatConfig chatConfig;

        private ChatActivityBuilder(String email) {
            emails = new HashSet<>();
            title = "Chat";
            subtitle = "";
            emails.add(email);
        }

        public ChatActivityBuilder addEmail(String email) {
            emails.add(email);
            return this;
        }

        public ChatActivityBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public ChatActivityBuilder withSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public ChatActivityBuilder withChatConfig(QiscusChatConfig qiscusChatConfig) {
            this.chatConfig = qiscusChatConfig;
            return this;
        }

        public void build(Context context, ChatActivityBuilderListener listener) {
            build(context).compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                    .subscribe(listener::onSuccess, listener::onError);
        }

        public Observable<Intent> build(Context context) {
            return QiscusApi.getInstance()
                    .getChatRoom(new ArrayList<>(emails))
                    .doOnNext(qiscusChatRoom -> {
                        qiscusChatRoom.setName(title);
                        qiscusChatRoom.setSubtitle(subtitle);
                    })
                    .map(qiscusChatRoom -> QiscusChatActivity.generateIntent(context, qiscusChatRoom));
        }
    }

    public interface ChatActivityBuilderListener {
        void onSuccess(Intent intent);

        void onError(Throwable throwable);
    }
}

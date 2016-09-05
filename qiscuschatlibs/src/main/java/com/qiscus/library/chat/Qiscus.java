package com.qiscus.library.chat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.google.gson.Gson;
import com.parse.Parse;
import com.qiscus.library.chat.data.model.QiscusAccount;
import com.qiscus.library.chat.data.model.QiscusConfig;
import com.qiscus.library.chat.util.Qson;

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

    public static void init(Application application) {
        APP_INSTANCE = application;
        APP_HANDLER = new Handler(APP_INSTANCE.getMainLooper());
        LOCAL_DATA_MANAGER = new LocalDataManager();

        Parse.initialize(APP_INSTANCE, QiscusConfig.PARSE_APP_ID, QiscusConfig.PARSE_CLIENT_KEY);
    }

    public static Application getApps() {
        return APP_INSTANCE;
    }

    public static Handler getAppsHandler() {
        return APP_HANDLER;
    }

    public static boolean isLogged() {
        return LOCAL_DATA_MANAGER.isLogged();
    }

    public static void setQiscusAccount(String email, String token, String fullname) {
        LOCAL_DATA_MANAGER.saveAccountInfo(new QiscusAccount(email, token, fullname));
    }

    public static void setQiscusAccount(QiscusAccount qiscusAccount) {
        LOCAL_DATA_MANAGER.saveAccountInfo(qiscusAccount);
    }

    public static QiscusAccount getQiscusAccount() {
        return LOCAL_DATA_MANAGER.getAccountInfo();
    }

    public static String getToken() {
        return LOCAL_DATA_MANAGER.getToken();
    }

    public static void logout() {
        LOCAL_DATA_MANAGER.clearData();
    }

    private static class LocalDataManager {
        private final SharedPreferences sharedPreferences;
        private final Gson gson;
        private String token;

        LocalDataManager() {
            sharedPreferences = Qiscus.getApps().getSharedPreferences("qiscus.cfg", Context.MODE_PRIVATE);
            gson = Qson.pluck().getParser();
            token = isLogged() ? getAccountInfo().getAuthenticationToken() : "";
        }

        private boolean isLogged() {
            return sharedPreferences.contains("cached_account");
        }

        private void saveAccountInfo(QiscusAccount qiscusAccount) {
            sharedPreferences.edit().putString("cached_account", gson.toJson(qiscusAccount)).apply();
            setToken(qiscusAccount.getAuthenticationToken());
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
}

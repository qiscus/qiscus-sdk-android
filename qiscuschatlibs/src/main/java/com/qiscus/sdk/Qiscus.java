package com.qiscus.sdk;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.google.gson.Gson;
import com.parse.Parse;
import com.qiscus.sdk.data.local.CacheManager;
import com.qiscus.sdk.data.local.QiscusDataBaseHelper;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.util.QiscusParser;

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

    private static String API_URL;
    private static String PUSHER_KEY;
    private static String PARSE_APP_ID = "w19bEjxkdmUKWfYNe5H3x8paGqTwzKl9dcwWG1Ap";
    private static String PARSE_CLIENT_KEY = "qLtml9adJ01QEEIlEpEyjKYjp4G4dXwOhBZg7mfX";

    public static void init(Application application, String qiscusApiUrl, String pusherKey) {
        APP_INSTANCE = application;
        API_URL = qiscusApiUrl;
        PUSHER_KEY = pusherKey;
        APP_HANDLER = new Handler(APP_INSTANCE.getMainLooper());
        LOCAL_DATA_MANAGER = new LocalDataManager();

        Parse.initialize(APP_INSTANCE, PARSE_APP_ID, PARSE_CLIENT_KEY);
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

    public static String getPusherKey() {
        return PUSHER_KEY;
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
        QiscusDataBaseHelper.getInstance().clear();
        CacheManager.getInstance().clearData();
    }

    private static class LocalDataManager {
        private final SharedPreferences sharedPreferences;
        private final Gson gson;
        private String token;

        LocalDataManager() {
            sharedPreferences = Qiscus.getApps().getSharedPreferences("qiscus.cfg", Context.MODE_PRIVATE);
            gson = QiscusParser.get().parser();
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

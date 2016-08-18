package com.qiscus.library.chat.sample.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.qiscus.library.chat.Qiscus;
import com.qiscus.library.chat.sample.data.model.AccountInfo;
import com.qiscus.library.chat.util.Qson;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public enum LocalDataManager {
    INSTANCE;

    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private String token;

    LocalDataManager() {
        sharedPreferences = Qiscus.getApps().getSharedPreferences("app.cfg", Context.MODE_PRIVATE);
        gson = Qson.pluck().getParser();
        token = isLogged() ? getAccountInfo().getAuthenticationToken() : "";
    }

    public static LocalDataManager getInstance() {
        return INSTANCE;
    }

    public boolean isLogged() {
        return sharedPreferences.contains("cached_account");
    }

    public void saveAccountInfo(AccountInfo accountInfo) {
        sharedPreferences.edit().putString("cached_account", gson.toJson(accountInfo)).apply();
        setToken(accountInfo.getAuthenticationToken());
    }

    public AccountInfo getAccountInfo() {
        return gson.fromJson(sharedPreferences.getString("cached_account", ""), AccountInfo.class);
    }

    public String getToken() {
        return token;
    }

    private void setToken(String token) {
        this.token = token;
    }

    public void clearData() {
        sharedPreferences.edit().clear().apply();
    }
}

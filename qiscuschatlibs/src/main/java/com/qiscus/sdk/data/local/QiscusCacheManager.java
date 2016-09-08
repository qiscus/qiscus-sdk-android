package com.qiscus.sdk.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.util.QiscusParser;

/**
 * Created on : May 25, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */

/**
 * Cache response from API here, so we can use it to speed up load data to the UI component
 */
public enum QiscusCacheManager {
    INSTANCE;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    QiscusCacheManager() {
        sharedPreferences = Qiscus.getApps().getSharedPreferences("qiscus.cache", Context.MODE_PRIVATE);
        gson = QiscusParser.get().parser();
    }

    public static QiscusCacheManager getInstance() {
        return INSTANCE;
    }

    public void cacheLastImagePath(String path) {
        sharedPreferences.edit()
                .putString("last_image_path", path)
                .apply();
    }

    public String getLastImagePath() {
        return sharedPreferences.getString("last_image_path", "");
    }

    public void clearData() {
        sharedPreferences.edit().clear().apply();
    }
}

package com.qiscus.sdk.chat.core.util;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Patterns;
import android.webkit.URLUtil;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.service.QiscusSyncJobService;
import com.qiscus.sdk.chat.core.service.QiscusSyncService;

import java.net.MalformedURLException;
import java.net.URL;

public class QiscusServiceUtil {

    private QiscusServiceUtil() {

    }

    public static boolean isMyServiceRunning() {
        Class<?> serviceClass;
        if (BuildVersionUtil.isOreoLower()) {
            serviceClass = QiscusSyncService.class;
        } else {
            serviceClass = QiscusSyncJobService.class;
        }
        ActivityManager manager = (ActivityManager) QiscusCore.getApps().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValidUrl(String urlString) {
        if (URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches()) {
            return true;
        } else {
            return false;
        }
    }
}

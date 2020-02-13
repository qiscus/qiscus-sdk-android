package com.qiscus.sdk.chat.core.util;

import android.app.ActivityManager;
import android.content.Context;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.service.QiscusSyncJobService;
import com.qiscus.sdk.chat.core.service.QiscusSyncService;

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
}

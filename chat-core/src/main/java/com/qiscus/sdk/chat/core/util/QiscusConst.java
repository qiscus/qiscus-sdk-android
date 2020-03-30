package com.qiscus.sdk.chat.core.util;

import android.app.Application;

public final class QiscusConst {
    private static Application apps;
    private static String appsName;

    public static Application getApps() {
        return apps;
    }

    public static void setApps(Application application) {
        QiscusConst.apps = application;
    }

    public static String getAppsName() {
        return appsName;
    }

    public static void setAppsName(String appsName) {
        QiscusConst.appsName = appsName;
    }
}

package com.qiscus.sdk.chat.core.util;

import com.qiscus.sdk.chat.core.BuildConfig;

class QiscusExternalStorageDirectoryUtil {

    private QiscusExternalStorageDirectoryUtil() {
    }
    public static String generateDirectory() {
        return BuildConfig.DIR_PATH;
    }
}

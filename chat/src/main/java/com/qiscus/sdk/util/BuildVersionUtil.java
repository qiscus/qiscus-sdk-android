package com.qiscus.sdk.util;

import android.os.Build;

/**
 * Created by adicatur on 11/22/17.
 */

public class BuildVersionUtil {

    public static boolean isOreoLower() {
        return Build.VERSION.SDK_INT < 26;
    }

    public static boolean isOreo() {
        return Build.VERSION.SDK_INT == 26;
    }

    public static boolean isNougatOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }


}

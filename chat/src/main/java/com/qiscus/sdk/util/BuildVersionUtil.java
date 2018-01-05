package com.qiscus.sdk.util;

import android.os.Build;

/**
 * Created on : November 22, 2017
 * Author     : adicatur
 * Name       : Catur Adi Nugroho
 * GitHub     : https://github.com/adicatur
 */
public class BuildVersionUtil {

    public static boolean isOreoLower() {
        return Build.VERSION.SDK_INT < 26;
    }

    public static boolean isOreoOrHigher() {
        return Build.VERSION.SDK_INT == 26;
    }

    public static boolean isNougatOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isAtLeastNMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    }
}

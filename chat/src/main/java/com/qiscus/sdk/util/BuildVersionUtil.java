package com.qiscus.sdk.util;

import android.os.Build;

import java.lang.reflect.Field;

/**
 * Created on : November 22, 2017
 * Author     : adicatur
 * Name       : Catur Adi Nugroho
 * GitHub     : https://github.com/adicatur
 */
public final class BuildVersionUtil {
    public static final String OS_VERSION_NAME;

    static {
        OS_VERSION_NAME = getOsVersionName();
    }

    private BuildVersionUtil() {

    }

    public static boolean isOreoLower() {
        return Build.VERSION.SDK_INT < 26;
    }

    public static boolean isOreoOrHigher() {
        return Build.VERSION.SDK_INT >= 26;
    }

    public static boolean isNougatOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isAtLeastNMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    }

    private static String getOsVersionName() {
        StringBuilder builder = new StringBuilder();
        builder.append(Build.VERSION.RELEASE);

        Field[] fields = Build.VERSION_CODES.class.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            int fieldValue = -1;

            try {
                fieldValue = field.getInt(new Object());
            } catch (IllegalArgumentException e) {
                //Ignore
            } catch (IllegalAccessException e) {
                //Ignore
            } catch (NullPointerException e) {
                //Ignore
            }

            if (fieldValue == Build.VERSION.SDK_INT) {
                builder.append(' ').append(fieldName);
                builder.append(" SDK ").append(fieldValue);
            }
        }
        return builder.toString();
    }
}

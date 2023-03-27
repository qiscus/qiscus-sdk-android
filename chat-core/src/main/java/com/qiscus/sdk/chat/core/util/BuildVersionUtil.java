package com.qiscus.sdk.chat.core.util;

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
    private static int OS_SDK_VERSION;

    static {
        OS_VERSION_NAME = getOsVersionName();
        OS_SDK_VERSION = Build.VERSION.SDK_INT;
    }

    private BuildVersionUtil() {

    }

    public static void setOsSdkVersion(int version) {
        OS_SDK_VERSION = version;
    }

    public static int getOsSdkVersion() {
        return OS_SDK_VERSION;
    }

    public static void resetVersion() {
        OS_SDK_VERSION = Build.VERSION.SDK_INT;
    }

    public static boolean isOreoLower() {
        return OS_SDK_VERSION < Build.VERSION_CODES.O;
    }

    public static boolean isOreoOrHigher() {
        return OS_SDK_VERSION >= Build.VERSION_CODES.O;
    }

    public static boolean isNougatOrHigher() {
        return OS_SDK_VERSION >= Build.VERSION_CODES.N;
    }

    public static boolean isAtLeastNMR1() {
        return OS_SDK_VERSION >= Build.VERSION_CODES.N_MR1;
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

    public static boolean isSame(int version) {
        return OS_SDK_VERSION == version;
    }
    public static boolean isUnderOrSame(int version) {
        return OS_SDK_VERSION <= version;
    }

    public static boolean isAboveOrSame(int version) {
        return OS_SDK_VERSION >= version;
    }

    public static boolean isUnder(int version) {
        return OS_SDK_VERSION < version;
    }

    public static boolean isAbove(int version) {
        return OS_SDK_VERSION > version;
    }

}

package com.qiscus.sdk.util;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.qiscus.sdk.Qiscus;

/**
 * Created on : May 31, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class AndroidUtilities {

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            Qiscus.getAppsHandler().post(runnable);
        } else {
            Qiscus.getAppsHandler().postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        Qiscus.getAppsHandler().removeCallbacks(runnable);
    }

    /**
     * Returns a localized string from the application's package's
     * default string table.
     *
     * @param resId Resource id for the string
     * @return The string data associated with the resource, stripped of styled
     *         text information.
     */
    @NonNull
    public static String getString(@StringRes int resId) {
        return Qiscus.getApps().getString(resId);
    }

    /**
     * Returns a localized formatted string from the application's package's
     * default string table, substituting the format arguments as defined in
     * {@link java.util.Formatter} and {@link String#format}.
     *
     * @param resId Resource id for the format string
     * @param formatArgs The format arguments that will be used for
     *                   substitution.
     * @return The string data associated with the resource, formatted and
     *         stripped of styled text information.
     */
    @NonNull
    public static String getString(@StringRes int resId, Object... formatArgs) {
        return Qiscus.getApps().getString(resId, formatArgs);
    }
}

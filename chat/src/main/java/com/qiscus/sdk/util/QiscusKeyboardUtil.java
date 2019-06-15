package com.qiscus.sdk.util;

import android.content.Context;
import androidx.annotation.RestrictTo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Aug, Thu 02 2018 11.41
 **/
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class QiscusKeyboardUtil {

    private QiscusKeyboardUtil() {
    }

    public static void showKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view.requestFocus() && inputMethodManager != null) {
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

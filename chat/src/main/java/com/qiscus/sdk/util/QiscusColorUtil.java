package com.qiscus.sdk.util;

import android.graphics.Color;
import android.support.annotation.RestrictTo;

import java.util.Random;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Aug, Thu 02 2018 11.30
 **/
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class QiscusColorUtil {

    private static final Random random = new Random();

    private QiscusColorUtil() {
    }

    public static int getRandomColor() {
        return Color.argb(100, random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}

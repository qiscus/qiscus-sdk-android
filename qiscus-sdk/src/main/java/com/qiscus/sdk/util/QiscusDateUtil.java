package com.qiscus.sdk.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class QiscusDateUtil {

    private static DateFormat fullDateFormat;
    private static DateFormat isoDateFormat;
    private static DateFormat hourDateFormat;

    static {
        fullDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        hourDateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
    }

    public static String toTodayOrDate(Date date) {
        String currentDateInString = fullDateFormat.format(new Date());
        String dateInString = fullDateFormat.format(date);
        return currentDateInString.equals(dateInString) ? "Today" : dateInString;
    }

    public static String toHour(Date date) {
        return hourDateFormat.format(date);
    }

    public static boolean isDateEqualIgnoreTime(Date lhs, Date rhs) {
        return toTodayOrDate(lhs).equals(toTodayOrDate(rhs));
    }

    public static Date parseIsoFormat(String date) throws ParseException {
        return isoDateFormat.parse(date);
    }
}

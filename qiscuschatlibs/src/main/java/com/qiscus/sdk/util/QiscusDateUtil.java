package com.qiscus.sdk.util;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class QiscusDateUtil {

    private static DateFormat fullDateFormat;
    private static DateFormat fullDateWithTimeFormat;
    private static DateFormat isoDateFormat;
    private static DateFormat hourDateFormat;

    static {
        fullDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        fullDateWithTimeFormat = new SimpleDateFormat("dd MMMM yyyy 'at' hh:mm a", Locale.US);
        isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        hourDateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
    }

    public static String toTodayOrDate(Date date) {
        String currentDateInString = fullDateFormat.format(new Date());
        String dateInString = fullDateFormat.format(date);
        return currentDateInString.equals(dateInString) ? "Today" : dateInString;
    }

    public static String toDateWithTime(Date date) {
        return fullDateWithTimeFormat.format(date);
    }

    public static String toHour(Date date) {
        return hourDateFormat.format(date);
    }

    public static String formatDurationInHourAndMinute(long durationInSecond) {
        long durationInMinute = durationInSecond / 60;
        long durationInHour = durationInMinute / 60;
        long remainingMinute = durationInMinute % 60;
        String durationInString;
        if (durationInHour <= 1 && remainingMinute <= 1) {
            durationInString = durationInHour + " hour " + remainingMinute + " min";
        } else if (durationInHour <= 1) {
            durationInString = durationInHour + " hour " + remainingMinute + " mins";
        } else if (remainingMinute <= 1) {
            durationInString = durationInHour + " hours " + remainingMinute + " min";
        } else {
            durationInString = durationInHour + " hours " + remainingMinute + " mins";
        }

        return durationInString;
    }

    public static String formatDurationInDayAndHour(long durationInSecond) {
        long durationInHour = durationInSecond / 3600;
        long durationInDay = durationInHour / 24;
        long remainingHour = durationInHour % 24;
        String durationInString;
        if (durationInDay <= 1 && remainingHour <= 1) {
            durationInString = durationInDay + " day " + remainingHour + " hour";
        } else if (durationInDay <= 1) {
            durationInString = durationInDay + " day " + remainingHour + " hours";
        } else if (remainingHour <= 1) {
            durationInString = durationInDay + " days " + remainingHour + " hour";
        } else {
            durationInString = durationInDay + " days " + remainingHour + " hours";
        }

        return durationInString;
    }

    public static long calculateDiffBetweenInDay(Date date1, Date date2) {
        long durationInMilliSecond = Math.abs(date1.getTime() - date2.getTime());
        return durationInMilliSecond / (1000 * 3600 * 24);
    }

    public static String getReadableDiffBetween(Date date1, Date date2) {
        if (calculateDiffBetweenInDay(date1, date2) <= 0) {
            if (date1.before(date2)) {
                return formatDurationInHourAndMinute((date2.getTime() - date1.getTime()) / 1000) + " remaining";
            } else {
                return formatDurationInHourAndMinute((date1.getTime() - date2.getTime()) / 1000) + " ago";
            }
        } else {
            if (date1.before(date2)) {
                return formatDurationInDayAndHour((date2.getTime() - date1.getTime()) / 1000) + " remaining";
            } else {
                return formatDurationInDayAndHour((date1.getTime() - date2.getTime()) / 1000) + " ago";
            }
        }
    }

    public static String getReadableDiffFromNow(Date date2) {
        return getReadableDiffBetween(Calendar.getInstance().getTime(), date2);
    }

    public static int[] getDateSplitNow() {
        Calendar calendar = Calendar.getInstance();
        return new int[]{calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)};
    }

    public static int[] getDateSplit(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new int[]{calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)};
    }

    public static Date mergeDate(int day, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    public static boolean isDateEqualIgnoreTime(Date lhs, Date rhs) {
        return toTodayOrDate(lhs).equals(toTodayOrDate(rhs));
    }

    public static Date parseIsoFormat(String date) throws ParseException {
        return isoDateFormat.parse(date);
    }
}

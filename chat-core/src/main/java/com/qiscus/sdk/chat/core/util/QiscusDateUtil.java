/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.chat.core.util;

import android.text.format.DateUtils;

import com.qiscus.sdk.chat.core.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class QiscusDateUtil {

    private static final DateFormat fullDateFormat;
    private static final DateFormat hourDateFormat;
    private static final DateFormat filterSdf;

    static {
        fullDateFormat = new SimpleDateFormat(QiscusTextUtil.getString(R.string.qiscus_date_format), Locale.getDefault());
        hourDateFormat = new SimpleDateFormat(QiscusTextUtil.getString(R.string.qiscus_hour_format), Locale.getDefault());
        filterSdf = new SimpleDateFormat(QiscusTextUtil.getString(R.string.qiscus_sdf_format), Locale.getDefault());
        filterSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private QiscusDateUtil() {

    }

    public static String toTodayOrDate(Date date) {
        String currentDateInString = fullDateFormat.format(new Date());
        String dateInString = fullDateFormat.format(date);
        return currentDateInString.equals(dateInString) ? QiscusTextUtil.getString(R.string.qiscus_today) : dateInString;
    }

    public static String toHour(Date date) {
        return hourDateFormat.format(date);
    }

    public static boolean isDateEqualIgnoreTime(Date lhs, Date rhs) {
        return toTodayOrDate(lhs).equals(toTodayOrDate(rhs));
    }

    public static String getRelativeTimeDiff(Date date) {
        String timeDiff = DateUtils.getRelativeTimeSpanString(date.getTime(),
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();

        if (timeDiff.contains("0 ")) {
            timeDiff = QiscusTextUtil.getString(R.string.qiscus_few_seconds_ago);
        }

        return timeDiff;
    }

    public static String toFullDateFormat(Date date) {
        return QiscusTextUtil.getString(R.string.qiscus_date_and_time, toTodayOrDate(date), toHour(date));
    }

    public static Date getDateTimeSdf(String dateTimeSdf) {
        try {
            return filterSdf.parse(dateTimeSdf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static boolean isBeforeADaySdf(long dateTime) {
        long dayTime = 86400000;
        Date c = Calendar.getInstance().getTime();
        long time = dateTime - c.getTime();
        return time <= dayTime;
    }

    public static boolean isPassingDateTimeSdf(long dateTime) {
        Date c = Calendar.getInstance().getTime();
        return c.getTime() > dateTime;
    }

}

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

package com.qiscus.sdk.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class QiscusDateUtil {

    private static DateFormat fullDateFormat;
    private static DateFormat isoDateFormat;
    private static DateFormat hourDateFormat;

    static {
        fullDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        hourDateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
    }

    private QiscusDateUtil() {

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

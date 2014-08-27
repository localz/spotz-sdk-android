package com.localz.spotz.api.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    public static final SimpleDateFormat ISO8601_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'" +
            "", Locale.US);

    public static String dateToIso8601Date(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        ISO8601_DATEFORMAT.setTimeZone(tz);
        return ISO8601_DATEFORMAT.format(date);
    }
}

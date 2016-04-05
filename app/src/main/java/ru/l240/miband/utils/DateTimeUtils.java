package ru.l240.miband.utils;

import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import ru.l240.miband.MiApplication;


public class DateTimeUtils {
    public static String formatDateTime(Date date) {
        return DateUtils.formatDateTime(MiApplication.getContext(), date.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
    }

    public static String formatDate(Date date) {
        return DateUtils.formatDateTime(MiApplication.getContext(), date.getTime(), DateUtils.FORMAT_SHOW_DATE);
//        long dateMillis = date.getTime();
//        if (isToday(dateMillis)) {
//            return "Today";
//        }
//        if (isYesterday(dateMillis)) {
//            return "Yesterday";
//        }
//        DateFormat.getDateInstance(DateFormat.SHORT).format(date);
    }

}

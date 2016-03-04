package ru.l240.miband.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Alexander Popov on 17.06.2015.
 */
public class DateUtils {

    public static Date startOfTheMinute(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date startOfTheHour(Date date) {
        if (date == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        return c.getTime();
    }

    public static Date startOfTheDay(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date endOfTheDay(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static Date startOfTheMonth(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public static Date endOfTheMonth(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return endOfTheDay(calendar.getTime());
    }

    public static Date startOfTheYear(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date endOfTheYear(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        return endOfTheDay(calendar.getTime());
    }

    public static Date addSeconds(Date date, int sec) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, sec);
        return calendar.getTime();
    }

    public static Date addMinutes(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    public static Date addMonths(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    public static Date addYears(Date date, int years) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    public static int getMinutes(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

    public static Date setMinutes(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    public static Date setSeconds(Date date, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    public static int getHours(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static Date setHours(Date date, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return calendar.getTime();
    }

    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static Date setYear(Date date, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    public static boolean between(Date date, Date dateFrom, Date dateTo) {
        return date.getTime() >= dateFrom.getTime() && date.getTime() <= dateTo.getTime();
    }

    public static double dateDifferenceDays(Date date1, Date date2) {
        return ((double) (date2.getTime() - date1.getTime())) / (double) (24 * 60 * 60 * 1000);
    }

    public static Integer dateDifferenceDaysEx(Date date1, Date date2) {
        if (date1 == null || date2 == null)
            return null;
        return (int) ((date2.getTime() - date1.getTime()) / (24 * 60 * 60 * 1000));
    }

    public static boolean isSameDay(Date date1, Date date2) {
        return startOfTheDay(date1).equals(startOfTheDay(date2));
    }

    public static String formatDate(Date date) {
        if (date == null) return null;
        return new SimpleDateFormat("dd.MM.yyyy").format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return null;
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }

    public static String formatTime(Date date) {
        if (date == null) return null;
        return new SimpleDateFormat("HH:mm").format(date);
    }

    public static Date createDate(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return startOfTheDay(cal.getTime());
    }

    /**
     * returns 0 if date-part equals, +1 if date1 bigger thean date2, and -1 otherwise
     */
    public static int compareDates(Date date1, Date date2) {
        if (date1 == null && date2 == null)
            return 0;
        if (date1 == null)
            return -1;
        if (date2 == null)
            return 1;
        long time1 = startOfTheDay(date1).getTime();
        long time2 = startOfTheDay(date2).getTime();
        if (time1 == time2)
            return 0;
        if (time1 > time2)
            return 1;
        return -1;
    }

    public static Date min(Date date1, Date date2) {
        int result = compareDates(date1, date2);
        if (result == 0)
            return date1;
        else if (date1 == null)
            return date2;
        else if (date2 == null)
            return date1;
        else return result > 0 ? date2 : date1;
    }

    public static Date max(Date date1, Date date2) {
        int result = compareDates(date1, date2);
        if (result == 0)
            return date1;
        else if (date1 == null)
            return date2;
        else if (date2 == null)
            return date1;
        else return result > 0 ? date1 : date2;
    }

    public static Date parseDate(String value, Date aDefault) {
        return parseDate(value, "dd.MM.yyyy", aDefault);
    }

    public static Date parseDate(String value, String format, Date aDefault) {
        if (value == null)
            return aDefault;
        try {
            return new SimpleDateFormat(format).parse(value);
        } catch (ParseException e) {
            return aDefault;
        }
    }
}

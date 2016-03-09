package ru.l240.miband.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * @author Alexander Popov on 11.05.15.
 */
public class MedUtils {

    public static final String LOGIN_PREF = "login";
    public static final String PASS_PREF = "pass";
    public static final String COOKIE_PREF = "cookie";
    public static final String SCHEDULER_PREF = "scheduler";
    public static final String SCHEDULER_MES_PREF = "mes";
    public static final String SCHEDULER_JOURNAL_PREF = "journal";
    public static final String SCHEDULER_MESSAGES_PREF = "message";
    public static final String SCHEDULER_FEEDBACK_ANSWERS_PREF = "feedback_answers";
    public static final String SCHEDULER_EXEC_PREF = "exec";
    public static final String SCHEDULER_EXEC_DELETE_PREF = "exec_delete";
    public static final String FIT_PREF = "fit_pref";

    public SimpleDateFormat dfDB() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }


    public static Date getDateByInt(int day) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);
        return cal.getTime();
    }

    public static Date getTimeByHourOfDay(int hour) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, hour);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        return cal.getTime();
    }

    public static String formatDateDialog(int year, int month, int day) {
        return "" + (day < 10 ? "0" + day : day) + "/" + ((month + 1) < 10 ? "0" + (month + 1) : (month + 1)) + "/" + year;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }


}

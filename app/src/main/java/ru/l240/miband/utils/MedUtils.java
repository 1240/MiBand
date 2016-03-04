package ru.l240.miband.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import ru.fors.remsmed.core.MedContract;
import ru.fors.remsmed.core.dto.contact.Message;
import ru.fors.remsmed.db.DBHelper;

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

    public static class MsgComparator implements Comparator {

        @Override
        public int compare(Object msg1, Object msg2) {
            return ((Message) msg1).getId().compareTo(((Message) msg2).getId());
        }
    }

    public static class SyncMsgComparator implements Comparator {

        @Override
        public int compare(Object msg1, Object msg2) {
            return ((Message) msg2).getId().compareTo(((Message) msg1).getId());
        }
    }

    public static class MsgDateComparator implements Comparator {

        @Override
        public int compare(Object msg1, Object msg2) {
            return ((Message) msg2).getDate().compareTo(((Message) msg1).getDate());
        }
    }

    public static Integer precriptionBadge(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        Integer allPrescrExecutionsCount = context.getContentResolver()
                .query(MedContract.PrescrExecution.CONTENT_URI,
                        MedContract.PrescrExecution.DEFAULT_PROJECTION,
                        MedContract.PrescrExecution.KEY_EXEC_PLAN_DATE + " BETWEEN '" + new MedUtils().dfDB().format(new Date()) + " 00:00:00' AND '" + new MedUtils().dfDB().format(new Date()) + " 99:99:99'",
                        null,
                        MedContract.PrescrExecution.DEFAULT_SORT).getCount();
        Integer allPrescrCount = dbHelper.getPrescriptionsCount(new Date(), new Date())
                + dbHelper.getPrescriptionsForDayNoTime(new Date()).size();
        return allPrescrCount - allPrescrExecutionsCount;
    }
}

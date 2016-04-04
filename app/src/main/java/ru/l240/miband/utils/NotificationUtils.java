package ru.l240.miband.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;

import ru.l240.miband.MainActivity;
import ru.l240.miband.R;


/**
 * @author Alexander Popov on 10.07.2015.
 */
public class NotificationUtils {

    public static final Integer MIN_2 = 2;
    public static final Integer MIN_1 = 1;
    public static final Integer MIN_5 = 5;
    private static final String TAG = NotificationUtils.class.getSimpleName();
    private static NotificationUtils instance;

    private static Context context;
    private NotificationManager manager;
    private int lastNotifId = 0;
    private HashMap<Integer, Notification> notifications;


    private NotificationUtils(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifications = new HashMap<Integer, Notification>();
    }

    /**
     */
    public static NotificationUtils getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationUtils(context);
        } else {
            instance.context = context;
        }
        return instance;
    }

    public void createAlarmNotify(Date date, int repeating) {
        Intent intent = new Intent(context, AlarmNotificationService.class);
        int lastAlarmId = PrefUtils.getLastAlarmId(context);
        lastAlarmId++;
        PendingIntent pintent = PendingIntent.getService(context, lastAlarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime(), repeating * 60000, pintent);
        PrefUtils.saveAlarmId(context, lastAlarmId);
        Log.d(TAG, "createAlarmNotify");
    }

    public void cancelAllAlarmNotify() {
        Intent intent = new Intent(context, AlarmNotificationService.class);
        int lastAlarmId = PrefUtils.getLastAlarmId(context);
        for (int i = 0; i < lastAlarmId; i++) {
            PendingIntent pintent = PendingIntent.getService(context, lastAlarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            pintent.cancel();
            alarmManager.cancel(pintent);
        }
        PrefUtils.saveAlarmId(context, 0);
        Log.d(TAG, "cancelAllAlarmNotify");
    }

}

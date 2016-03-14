package ru.l240.miband.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import ru.l240.miband.MainActivity;
import ru.l240.miband.R;


/**
 * @author Alexander Popov on 10.07.2015.
 */
public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();
    public static final Integer MIN_2 = 120000;
    public static final Integer MIN_1 = 60000;
    public static final Integer MIN_5 = 300000;
    private static NotificationUtils instance;

    private static Context context;
    private static int lastAlarmId = 0;
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

    public int createInfoNotification(String message, Date when) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.android_avatar) //ic_launcher_nuffield //ic_launcher_remsmed
                .setAutoCancel(true)
                .setTicker(message)
                .setContentText(message)
                .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setWhen(when.getTime())
                .setShowWhen(true)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setDefaults(Notification.DEFAULT_ALL);

        Notification notification = nb.getNotification();
        manager.notify(lastNotifId, notification);
        notifications.put(lastNotifId, notification);
        return lastNotifId++;
    }

    public void cancelInfoNotification(Integer id) {
        manager.cancel(id);
    }

    public void cancelAllNotifications() {
        manager.cancelAll();
    }

    public void createAlarmNotify(Date date, Integer min) throws ParseException {
        Intent intent = new Intent(context, AlarmNotificationService.class);
        lastAlarmId++;
        PendingIntent pintent = PendingIntent.getService(context, lastAlarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime(), min, pintent);
    }

    public void cancelAllAlarmNotify() {
        Intent intent = new Intent(context, AlarmNotificationService.class);
        for (int i = 0; i < lastAlarmId; i++) {
            PendingIntent pintent = PendingIntent.getService(context, lastAlarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pintent);
        }
    }

}

package ru.l240.miband.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ru.fors.remsmed.MainActivity;
import ru.fors.remsmed.R;
import ru.fors.remsmed.core.dto.prescriptions.PrescriptionItem;
import ru.fors.remsmed.db.DBHelper;

/**
 * @author Alexander Popov on 10.07.2015.
 */
public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();

    private static NotificationUtils instance;

    private static Context context;
    private NotificationManager manager;
    private int lastNotifId = 0;
    private static int lastAlarmId = 0;
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
                .setSmallIcon(R.drawable.ic_launcher_nuffield) //ic_launcher_nuffield //ic_launcher_remsmed
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


    public List<PrescriptionItem> getEarlestNotify(Date date) {
        Date compareDate = null;
        DBHelper dbHelper = new DBHelper(context);
        List<PrescriptionItem> prescriptionsForDay = dbHelper.getPrescriptionsForDay(date);
        List<PrescriptionItem> result = new ArrayList<>();

        if (prescriptionsForDay.isEmpty()) {
            date = DateUtils.addDays(date, 1);
            prescriptionsForDay = dbHelper.getPrescriptionsForDay(date);
            if (prescriptionsForDay.isEmpty()) return result;
        }

        for (PrescriptionItem item : prescriptionsForDay) {
            try {
                Date execDate = new SimpleDateFormat("dd.MM.yyyy HH")
                        .parse(new SimpleDateFormat("dd.MM.yyyy ").format(date)
                                + item.getTemplate().getHour());
                if (dbHelper.getAllPrescrExecutionsByPrescId(item.getTemplate().getPrescrId(), date, item.getTemplate().getExecNum()) == null &&
                        !execDate.before(date)) {
                    if (compareDate == null)
                        compareDate = execDate;
                    if (compareDate.equals(execDate)) {
                        date = new SimpleDateFormat("dd.MM.yyyy HH").parse(new SimpleDateFormat("dd.MM.yyyy ").format(execDate) + item.getTemplate().getHour());
                        item.getTemplate().setHour(item.getTemplate().getHour());
                        result.add(item);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void createNotify() throws ParseException {
        NotificationUtils notificationUtils = NotificationUtils.getInstance(context);
        notificationUtils.cancelAllNotifications();
        List<PrescriptionItem> earlestNotify = getEarlestNotify(DateUtils.addMinutes(new Date(), -5));
        for (PrescriptionItem prescriptionItem : earlestNotify) {
            Integer hour = prescriptionItem.getTemplate().getHour();
            Date execDate =  (new Date().before(DateUtils.setHours(new Date(), hour))) ? new Date() :  DateUtils.addDays(new Date(), 1);
            Date date = new SimpleDateFormat("dd.MM.yyyy HH").parse(new SimpleDateFormat("dd.MM.yyyy ").format(execDate) + hour);
            notificationUtils.createInfoNotification(prescriptionItem.getDescription(), date);
        }
    }

    public void createAlarmNotify() throws ParseException {
        Intent intent = new Intent(context, AlarmNotificationService.class);
        List<PrescriptionItem> earlestNotify = getEarlestNotify(DateUtils.addMinutes(new Date(), +5));
        if (earlestNotify.isEmpty()) {
            earlestNotify = getEarlestNotify(DateUtils.startOfTheDay(DateUtils.addDays(new Date(), 1)));
        }
        if (!earlestNotify.isEmpty()) {
            lastAlarmId++;
            PendingIntent pintent = PendingIntent.getService(context, lastAlarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Integer hour = earlestNotify.get(0).getTemplate().getHour();
            Date execDate =  (new Date().before(DateUtils.setHours(new Date(), hour))) ? new Date() :  DateUtils.addDays(new Date(), 1);
            Date date = new SimpleDateFormat("dd.MM.yyyy HH").parse(new SimpleDateFormat("dd.MM.yyyy ").format(execDate) + hour);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), pintent);
        }
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

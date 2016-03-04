package ru.l240.miband.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.text.ParseException;

/**
 * @author Alexander Popov on 21.07.2015.
 */
public class AlarmNotificationService extends Service{
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationUtils utils = NotificationUtils.getInstance(getApplicationContext());
        try {
            utils.createNotify();
            utils.createAlarmNotify();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}

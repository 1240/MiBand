package ru.l240.miband.utils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

import ru.l240.miband.BleCallback;
import ru.l240.miband.BleSingleton;

/**
 * @author Alexander Popov on 21.07.2015.
 */
public class AlarmNotificationService extends IntentService {

    public static final String TAG = AlarmNotificationService.class.getSimpleName();

    public AlarmNotificationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            final BleSingleton bleSingleton = BleSingleton.getInstance(getApplicationContext());
            final BleCallback callback = new BleCallback() {
                @Override
                protected void callback(String data) {
                    try {
                        bleSingleton.measure();
                    } catch (InterruptedException e) {
                        NotificationUtils.getInstance(AlarmNotificationService.this).createAlarmNotify(DateUtils.addMinutes(new Date(), 5));
                        Log.d(TAG, "error");
                        e.printStackTrace();
                    }
                }

                @Override
                protected void callbackHR(String data) {
                    bleSingleton.disconnect(this);
                }
            };
            bleSingleton.addCallback(callback);
            bleSingleton.init();
            bleSingleton.connect();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "error");
            NotificationUtils.getInstance(this).createAlarmNotify(DateUtils.addMinutes(new Date(), 5));
        }
    }


}

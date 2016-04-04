package ru.l240.miband.utils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.Collections;
import java.util.Date;

import io.realm.Realm;
import ru.l240.miband.BleCallback;
import ru.l240.miband.BleSingleton;
import ru.l240.miband.models.UserMeasurement;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.retrofit.RequestTaskAddMeasurement;

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
//                        NotificationUtils.getInstance(AlarmNotificationService.this).createAlarmNotify(DateUtils.addMinutes(new Date(), NotificationUtils.MIN_5), NotificationUtils.MIN_5);
                        Log.d(TAG, "error");
                        e.printStackTrace();
                    }
                }

                @Override
                protected void callbackHR(String data) {
                    UserMeasurement measurement = new UserMeasurement();
                    measurement.setMeasurementId(3);
                    measurement.setMeasurementDate(new Date());
                    measurement.setStrValue(String.valueOf(data));
                    if (MedUtils.isNetworkConnected(getApplicationContext())) {
                        RequestTaskAddMeasurement addMeasurement = new RequestTaskAddMeasurement(getApplicationContext(), false, Collections.singletonList(measurement)) {
                            @Override
                            protected void onPostExecute(Boolean success) {
                                super.onPostExecute(success);
                                bleSingleton.disconnect();
                            }
                        };
                        addMeasurement.execute();
                    } else {
                        RealmHelper.save(Realm.getInstance(getApplicationContext()), measurement);
                        bleSingleton.disconnect();
                    }
                }
            };
            bleSingleton.setCallback(callback);
            bleSingleton.init();
            bleSingleton.connect();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "error");
//            NotificationUtils.getInstance(this).createAlarmNotify(DateUtils.addMinutes(new Date(), NotificationUtils.MIN_5), NotificationUtils.MIN_5);
        }
    }
}

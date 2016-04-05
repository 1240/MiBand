package ru.l240.miband.utils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.Collections;
import java.util.Date;

import io.realm.Realm;
import ru.l240.miband.BleCallback;
import ru.l240.miband.BleSingleton;
import ru.l240.miband.MiApplication;
import ru.l240.miband.gadgetbridge.impl.GBDeviceService;
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
        String address = PrefUtils.getAddress(getApplicationContext());
        MiApplication.deviceService().start();
        MiApplication.deviceService().requestDeviceInfo();
        MiApplication.deviceService().connect(address);
    }
}

package ru.l240.miband.utils;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import io.realm.Realm;
import ru.l240.miband.BleCallback;
import ru.l240.miband.BleSingleton;
import ru.l240.miband.GBException;
import ru.l240.miband.MiApplication;
import ru.l240.miband.SettingsActivity;
import ru.l240.miband.gadgetbridge.impl.GBDevice;
import ru.l240.miband.gadgetbridge.impl.GBDeviceService;
import ru.l240.miband.gadgetbridge.model.DeviceType;
import ru.l240.miband.gadgetbridge.service.DeviceSupport;
import ru.l240.miband.gadgetbridge.service.DeviceSupportFactory;
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
//        MiApplication.deviceService().start();
//        MiApplication.deviceService().requestDeviceInfo();
//        MiApplication.deviceService().connect(address, true);
        BluetoothAdapter bluetoothAdapter
                = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices
                = bluetoothAdapter.getBondedDevices();
        BluetoothDevice bleDevice = null;
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(address))
                bleDevice = device;
        }
        if (bleDevice != null) {
            try {
                GBDevice mi = new GBDevice(address, "MI", DeviceType.MIBAND);
                DeviceSupport deviceSupport = new DeviceSupportFactory(this).createDeviceSupport(mi);
                ru.l240.miband.models.Log log = new ru.l240.miband.models.Log();
                log.setDate(new Date());
                log.setText("Try connect to MI1S");
                RealmHelper.save(Realm.getInstance(getApplicationContext()), log);
                Intent intentSA = new Intent(SettingsActivity.TAG);
                intentSA.putExtra("logText", log.getText());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentSA);
                deviceSupport.connect();
                deviceSupport.useAutoConnect();
                int timeLeft = 30;
                while (!deviceSupport.isConnected()) {
                    if (deviceSupport.getDevice().getState().equals(GBDevice.State.CONNECTING)) {
                        break;
                    }
                    SystemClock.sleep(1000);
                    timeLeft--;
                    if (timeLeft < 0) {
                        Log.d(TAG, "Device not nearby");
                        ru.l240.miband.models.Log log1 = new ru.l240.miband.models.Log();
                        log1.setDate(new Date());
                        log1.setText("Device not nearby");
                        Intent intent1 = new Intent(SettingsActivity.TAG);
                        intent1.putExtra("logText", log1.getText());
                        RealmHelper.save(Realm.getInstance(getApplicationContext()), log1);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
                        deviceSupport.dispose();
                        return;
                    }
                }
                timeLeft = 30;
                while (!deviceSupport.isConnected()) {
                    SystemClock.sleep(1000);
                    timeLeft--;
                    if (timeLeft < 0) {
                        Log.d(TAG, "Cannot connect device... But status is connecting");
                        ru.l240.miband.models.Log log2 = new ru.l240.miband.models.Log();
                        log2.setDate(new Date());
                        log2.setText("Cannot connect device... But status is connecting");
                        Intent intent2 = new Intent(SettingsActivity.TAG);
                        intent2.putExtra("logText", log2.getText());
                        RealmHelper.save(Realm.getInstance(getApplicationContext()), log2);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent2);
                        deviceSupport.dispose();
                        return;
                    }
                }
                deviceSupport.onHeartRateTest();
                SystemClock.sleep(50000);
                deviceSupport.dispose();
                stopSelf();
            } catch (GBException e) {
                e.printStackTrace();
            }
        }
        stopSelf();
    }
}

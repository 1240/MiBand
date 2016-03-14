package ru.l240.miband.utils;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import ru.l240.miband.models.UserMeasurement;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.retrofit.RequestTaskAddMeasurement;

/**
 * @author Alexander Popov on 21.07.2015.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AlarmNotificationService extends Service {

    // Test HR
    public static final byte COMMAND_SET_HR_SLEEP = 0x0;
    public static final byte COMMAND_SET__HR_CONTINUOUS = 0x1;
    public static final byte COMMAND_SET_HR_MANUAL = 0x2;
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public static final String TAG = AlarmNotificationService.class.getSimpleName();
    public static final String ADDRESS = "C8:0F:10:32:11:17";
    static final byte[] startHeartMeasurementManual = new byte[]{0x15, COMMAND_SET_HR_MANUAL, 1};
    static final byte[] stopHeartMeasurementManual = new byte[]{0x15, COMMAND_SET_HR_MANUAL, 0};
    static final byte[] stopHeartMeasurementContinuous = new byte[]{0x15, COMMAND_SET__HR_CONTINUOUS, 0};
    static final byte[] stopHeartMeasurementSleep = new byte[]{0x15, COMMAND_SET_HR_SLEEP, 0};
    private static final UUID UUID_MILI_SERVICE = UUID
            .fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_pair = UUID
            .fromString("0000ff0f-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_CONTROL_POINT = UUID
            .fromString("0000ff05-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_USER_INFO = UUID
            .fromString("0000ff04-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_REALTIME_STEPS = UUID
            .fromString("0000ff06-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_ACTIVITY = UUID
            .fromString("0000ff07-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_LE_PARAMS = UUID
            .fromString("0000ff09-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_DEVICE_NAME = UUID
            .fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_BATTERY = UUID
            .fromString("0000ff0c-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_SENSOR_DATA = UUID
            .fromString("0000ff0e-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_TEST = UUID
            .fromString("0000ff05-0000-1000-8000-00805f9b34fb");
    private static final UUID hRService = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_NOTIF = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_HR_MES = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothMi;
    private BluetoothGatt mGatt;
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        int state = 0;

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                pair();
                BluetoothGattCharacteristic characteristicNotif = mGatt.getService(hRService).getCharacteristic(UUID_NOTIF);
                boolean b1 = mGatt.setCharacteristicNotification(characteristicNotif, true);
                BluetoothGattDescriptor descriptor = mGatt.getService(hRService).getCharacteristic(UUID_NOTIF).getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                boolean b3 = mGatt.writeDescriptor(descriptor);
            }

        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                Log.d(TAG, "HAVE PROBLEMS IN AUTH");
            } else if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic characteristicNotif2 = mGatt.getService(hRService).getCharacteristic(UUID_NOTIF);
                characteristicNotif2.setValue(new byte[]{0x1, 0x0});
                boolean b2 = mGatt.writeCharacteristic(characteristicNotif2);
                Log.d(TAG, String.valueOf(b2));
            }
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            byte[] b = characteristic.getValue();
            UUID uuid = characteristic.getUuid();
            Log.i(uuid.toString(), "state: " + state
                    + " value:" + Arrays.toString(b));

            if (UUID_NOTIF.equals(uuid)) {
                byte[] value = characteristic.getValue();
                if (value.length == 2 && value[0] == 6) {
                    int hrValue = (value[1] & 0xff);
                    Log.d(TAG, String.valueOf(hrValue));

                } else {
                    Log.d(TAG, "RECEIVED DATA WITH LENGTH: " + value.length);
                    for (byte bb : value) {
                        Log.d(TAG, "DATA: " + String.format("0x%2x", bb));
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            UUID characteristicUUID = characteristic.getUuid();
            if (UUID_NOTIF.equals(characteristicUUID)) {
                byte[] value = characteristic.getValue();
                if (value.length == 2 && value[0] == 6) {
                    int hrValue = (value[1] & 0xff);
                    Log.d(TAG, String.valueOf(hrValue));
                    UserMeasurement measurement = new UserMeasurement();
                    measurement.setMeasurementId(3);
                    measurement.setMeasurementDate(new Date());
                    measurement.setStrValue(String.valueOf(hrValue));
                    if (MedUtils.isNetworkConnected(getApplicationContext())) {
                        RequestTaskAddMeasurement addMeasurement = new RequestTaskAddMeasurement(getApplicationContext(), false, Collections.singletonList(measurement));
                        addMeasurement.execute();
                    } else {
                        RealmHelper.save(Realm.getInstance(getApplicationContext()), measurement);
                    }
                    try {
                        updateAlarm(hrValue);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (mGatt != null) {
                        mGatt.disconnect();
                        mGatt.close();
                        mGatt = null;
                    }
                } else {
                    Log.d(TAG, "RECEIVED DATA WITH LENGTH: " + value.length);
                    for (byte b : value) {
                        Log.d(TAG, "DATA: " + String.format("0x%2x", b));
                    }
                }
            } else {
                byte[] value = characteristic.getValue();
                Log.d(TAG, "Unhandled characteristic changed: " + characteristicUUID);
                Log.d(TAG, "RECEIVED DATA WITH LENGTH: " + value.length);
                for (byte b : value) {
                    Log.d(TAG, "DATA: " + String.format("0x%2x", b));
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationUtils utils = NotificationUtils.getInstance(getApplicationContext());
        try {
            mBluetoothManager = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            mBluetoothMi = mBluetoothAdapter.getRemoteDevice(ADDRESS);
            if (mBluetoothMi != null) {
                mGatt = mBluetoothMi.connectGatt(this, true, mGattCallback);
                mGatt.connect();
            }
            Log.d(TAG, "refreshing");
            int time = 0;
            while (mGatt == null || mGatt.getService(hRService) == null) {
                if (time >= 10)
                    throw new NullPointerException("MGatt is null");
                Thread.sleep(1000L);
                time++;
            }
            BluetoothGattCharacteristic characteristicCP = mGatt.getService(hRService).getCharacteristic(UUID_HR_MES);
            characteristicCP.setValue(startHeartMeasurementManual);
            boolean b = false;
            while (!b) {
                b = mGatt.writeCharacteristic(characteristicCP);
                Thread.sleep(1000L);
            }
            Log.d(TAG, "Измеряю пульс...");

        } catch (NullPointerException e) {
//            NotificationUtils.getInstance(getApplicationContext()).createInfoNotification("Потеря связи с браслетом! Проверьте Bluetooth и заряд браслета.", new Date());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private BluetoothGattService getMiliService() {
        return mGatt.getService(UUID_MILI_SERVICE);

    }

    private void pair() {
        BluetoothGattCharacteristic chrt = getMiliService().getCharacteristic(
                UUID_CHAR_pair);

        chrt.setValue(new byte[]{2});

        mGatt.writeCharacteristic(chrt);
        Log.d(TAG, "Браслет найден. Синхронизиоруюсь...");
    }

    private void updateAlarm(Integer value) throws ParseException {
        if (value >= 60 && value <= 90) {
            NotificationUtils.getInstance(this).cancelAllAlarmNotify();
            NotificationUtils.getInstance(this).createAlarmNotify(DateUtils.addMinutes(new Date(), 1), NotificationUtils.MIN_5);
            return;
        }
        if ((value >= 91 && value <= 120) || (value >= 46 && value <= 59)) {
            NotificationUtils.getInstance(this).cancelAllAlarmNotify();
            NotificationUtils.getInstance(this).createAlarmNotify(DateUtils.addMinutes(new Date(), 1), NotificationUtils.MIN_2);
            return;
        }
        if (value >= 121 || value <= 45) {
            NotificationUtils.getInstance(this).cancelAllAlarmNotify();
            NotificationUtils.getInstance(this).createAlarmNotify(DateUtils.addMinutes(new Date(), 1), NotificationUtils.MIN_1);
            return;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

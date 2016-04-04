package ru.l240.miband;

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
import android.util.Log;

import java.util.Date;
import java.util.UUID;

import ru.l240.miband.utils.DateUtils;
import ru.l240.miband.utils.NotificationUtils;

/**
 * Created by l24o on 21.03.16.
 */
public class BleSingleton {

    public static final String TAG = BleSingleton.class.getSimpleName();
    public static final byte COMMAND_SET_HR_SLEEP = 0x0;
    public static final byte COMMAND_SET__HR_CONTINUOUS = 0x1;
    public static final byte COMMAND_SET_HR_MANUAL = 0x2;
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
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
    private static final UUID UUID_ALERT = UUID
            .fromString("00001802-0000-1000-8000-00805f9b34fb");
    private static final UUID hRService = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_NOTIF = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_HR_MES = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");
    private static BleSingleton mInstance;
    private BleCallback bleCallback;
    private BluetoothDevice mBluetoothMi;
    private BluetoothGatt mGatt;
    private Context context;
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        int state = 0;

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic characteristicNotif = mGatt.getService(hRService).getCharacteristic(UUID_NOTIF);
                boolean b1 = mGatt.setCharacteristicNotification(characteristicNotif, true);
                BluetoothGattDescriptor descriptor = mGatt.getService(hRService).getCharacteristic(UUID_NOTIF).getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                boolean b3 = mGatt.writeDescriptor(descriptor);
//                pair();
//                callback("Синхронизируюсь...");

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
            /*if (characteristic.getUuid().equals(UUID_CHAR_pair)) {

            }*/
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
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
                    callbackHR(String.valueOf(hrValue));
                    updateAlarm(hrValue);
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

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                Log.d(TAG, "HAVE PROBLEMS IN AUTH");
                throw new NullPointerException();
            } else if (status == BluetoothGatt.GATT_SUCCESS) {
                callback("Синхронизация завершена!");
            }
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };

    private BleSingleton(Context context) {
        this.context = context;
    }

    public static BleSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BleSingleton(context);
        }
        return mInstance;
    }

    private BluetoothGattService getMiliService() {
        return mGatt.getService(UUID_MILI_SERVICE);

    }

    private void pair() {
        BluetoothGattCharacteristic chrt = getMiliService().getCharacteristic(
                UUID_CHAR_pair);
        chrt.setValue(new byte[]{2});
        boolean b1 = mGatt.writeCharacteristic(chrt);
    }

    public void init() {
        BluetoothManager mBluetoothManager = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE));
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothMi = mBluetoothAdapter.getRemoteDevice(ADDRESS);
    }

    public void connect() {
        if (mBluetoothMi != null) {
            mGatt = mBluetoothMi.connectGatt(context, false, mGattCallback);
            mGatt.connect();
        }
    }

    public void vibrate() {
        BluetoothGattCharacteristic characteristic1 = mGatt.getService(UUID_ALERT).getCharacteristic(UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb"));
        characteristic1.setValue(new byte[]{0x01});
        mGatt.writeCharacteristic(characteristic1);
    }

    public void updateAlarm(Integer value) {

        if (value >= 60 && value <= 90) {
            NotificationUtils.getInstance(context).cancelAllAlarmNotify();
            NotificationUtils.getInstance(context).createAlarmNotify(DateUtils.addMinutes(new Date(), NotificationUtils.MIN_2), NotificationUtils.MIN_2);
            return;
        }
        if ((value >= 91 && value <= 120) || (value >= 46 && value <= 59)) {
            NotificationUtils.getInstance(context).cancelAllAlarmNotify();
            NotificationUtils.getInstance(context).createAlarmNotify(DateUtils.addMinutes(new Date(), NotificationUtils.MIN_5), NotificationUtils.MIN_5);
            return;
        }
        if (value >= 121 || value <= 45) {
            NotificationUtils.getInstance(context).cancelAllAlarmNotify();
            NotificationUtils.getInstance(context).createAlarmNotify(DateUtils.addMinutes(new Date(), NotificationUtils.MIN_2), NotificationUtils.MIN_2);
            return;
        }
    }

    public void measure() throws InterruptedException {
        Log.d(TAG, "measure");
        int time = 0;
        while (mGatt == null || mGatt.getService(hRService) == null) {
            if (time >= 10)
                throw new NullPointerException("MGatt is null");
            Thread.sleep(1000L);
            time++;
        }
        BluetoothGattCharacteristic characteristicCP = null;
        while (characteristicCP == null) {
            characteristicCP = mGatt.getService(hRService).getCharacteristic(UUID_HR_MES);
            Thread.sleep(1000L);
        }
        characteristicCP.setValue(startHeartMeasurementManual);
        boolean b = false;
        while (!b) {
            b = mGatt.writeCharacteristic(characteristicCP);
            Thread.sleep(1000L);
        }
        Log.d(TAG, "Измеряю пульс...");
    }

    private void callback(String data) {
        bleCallback.callback(data);
    }

    private void callbackHR(String data) {
        bleCallback.callbackHR(data);
    }


    public void setCallback(BleCallback callback) {
        bleCallback = callback;
    }

    public void disconnect() {
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
    }
}

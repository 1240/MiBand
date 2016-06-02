package ru.l240.miband.gadgetbridge.service.devices.miband;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import ru.l240.miband.MiApplication;
import ru.l240.miband.R;
import ru.l240.miband.SettingsActivity;
import ru.l240.miband.gadgetbridge.devices.MiBandCoordinator;
import ru.l240.miband.gadgetbridge.devices.miband.MiBandDateConverter;
import ru.l240.miband.gadgetbridge.devices.miband.MiBandFWHelper;
import ru.l240.miband.gadgetbridge.devices.miband.MiBandService;
import ru.l240.miband.gadgetbridge.devices.miband.VibrationProfile;
import ru.l240.miband.gadgetbridge.impl.GBDevice;
import ru.l240.miband.gadgetbridge.model.Alarm;
import ru.l240.miband.gadgetbridge.model.CalendarEvents;
import ru.l240.miband.gadgetbridge.model.DeviceService;
import ru.l240.miband.gadgetbridge.model.GenericItem;
import ru.l240.miband.gadgetbridge.model.NotificationSpec;
import ru.l240.miband.gadgetbridge.model.ServiceCommand;
import ru.l240.miband.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import ru.l240.miband.gadgetbridge.service.btle.BtLEAction;
import ru.l240.miband.gadgetbridge.service.btle.GattCharacteristic;
import ru.l240.miband.gadgetbridge.service.btle.GattService;
import ru.l240.miband.gadgetbridge.service.btle.TransactionBuilder;
import ru.l240.miband.gadgetbridge.service.btle.actions.AbortTransactionAction;
import ru.l240.miband.gadgetbridge.service.btle.actions.ConditionalWriteAction;
import ru.l240.miband.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import ru.l240.miband.gadgetbridge.service.btle.actions.WriteAction;
import ru.l240.miband.gadgetbridge.service.deviceevents.GBDeviceEventBatteryInfo;
import ru.l240.miband.gadgetbridge.service.deviceevents.GBDeviceEventVersionInfo;
import ru.l240.miband.gadgetbridge.service.devices.miband.operations.FetchActivityOperation;
import ru.l240.miband.gadgetbridge.service.devices.miband.operations.UpdateFirmwareOperation;
import ru.l240.miband.models.UserMeasurement;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.retrofit.RequestTaskAddMeasurement;
import ru.l240.miband.utils.DateTimeUtils;
import ru.l240.miband.utils.DateUtils;
import ru.l240.miband.utils.MedUtils;
import ru.l240.miband.utils.NotificationUtils;

import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.DEFAULT_VALUE_FLASH_COLOUR;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.DEFAULT_VALUE_FLASH_COUNT;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.DEFAULT_VALUE_FLASH_DURATION;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.DEFAULT_VALUE_FLASH_ORIGINAL_COLOUR;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.DEFAULT_VALUE_VIBRATION_COUNT;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.DEFAULT_VALUE_VIBRATION_DURATION;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.DEFAULT_VALUE_VIBRATION_PAUSE;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.DEFAULT_VALUE_VIBRATION_PROFILE;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.FLASH_COLOUR;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.FLASH_COUNT;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.FLASH_DURATION;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.FLASH_ORIGINAL_COLOUR;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.ORIGIN_GENERIC;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.ORIGIN_INCOMING_CALL;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.PREF_MIBAND_RESERVE_ALARM_FOR_CALENDAR;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.VIBRATION_COUNT;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.VIBRATION_DURATION;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.VIBRATION_PAUSE;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.VIBRATION_PROFILE;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.getNotificationPrefIntValue;
import static ru.l240.miband.gadgetbridge.devices.miband.MiBandConst.getNotificationPrefStringValue;


public class MiBandSupport extends AbstractBTLEDeviceSupport {

    /**
     * This is just for temporary testing of Mi1A double firmware update.
     * DO NOT SET TO TRUE UNLESS YOU KNOW WHAT YOU'RE DOING!
     */
    public static final boolean MI_1A_HR_FW_UPDATE_TEST_MODE_ENABLED = false;
    public static final String TAG = MiBandSupport.class.getSimpleName();
    public static final String HEART_RATE_ACTION = "ru.l240.miband.gadgetbridge.service.devices.miband.heartrate";
    static final byte[] reboot = new byte[]{MiBandService.COMMAND_REBOOT};
    static final byte[] startHeartMeasurementManual = new byte[]{0x15, MiBandService.COMMAND_SET_HR_MANUAL, 1};
    static final byte[] stopHeartMeasurementManual = new byte[]{0x15, MiBandService.COMMAND_SET_HR_MANUAL, 0};
    static final byte[] startHeartMeasurementContinuous = new byte[]{0x15, MiBandService.COMMAND_SET__HR_CONTINUOUS, 1};
    static final byte[] stopHeartMeasurementContinuous = new byte[]{0x15, MiBandService.COMMAND_SET__HR_CONTINUOUS, 0};
    static final byte[] startHeartMeasurementSleep = new byte[]{0x15, MiBandService.COMMAND_SET_HR_SLEEP, 1};
    static final byte[] stopHeartMeasurementSleep = new byte[]{0x15, MiBandService.COMMAND_SET_HR_SLEEP, 0};
    static final byte[] startRealTimeStepsNotifications = new byte[]{MiBandService.COMMAND_SET_REALTIME_STEPS_NOTIFICATION, 1};
    static final byte[] stopRealTimeStepsNotifications = new byte[]{MiBandService.COMMAND_SET_REALTIME_STEPS_NOTIFICATION, 0};
    private final GBDeviceEventVersionInfo versionCmd = new GBDeviceEventVersionInfo();
    private final GBDeviceEventBatteryInfo batteryCmd = new GBDeviceEventBatteryInfo();
    private volatile boolean telephoneRinging;
    private volatile boolean isLocatingDevice;
    private DeviceInfo mDeviceInfo;

    public MiBandSupport() {
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addSupportedService(MiBandService.UUID_SERVICE_MIBAND_SERVICE);
        addSupportedService(MiBandService.UUID_SERVICE_HEART_RATE);
        addSupportedService(GattService.UUID_SERVICE_IMMEDIATE_ALERT);
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));
        enableNotifications(builder, true)
                .setLowLatency(builder)
                .readDate(builder) // without reading the data, we get sporadic connection problems, especially directly after turning on BT
                .pair(builder)
                .requestDeviceInfo(builder)
                .sendUserInfo(builder)
//                .checkAuthenticationNeeded(builder, getDevice())
//                .setWearLocation(builder)
//                .setHeartrateSleepSupport(builder)
//                .setFitnessGoal(builder)
                .enableFurtherNotifications(builder, true)
                .setCurrentTime(builder)
                .requestBatteryInfo(builder)
//                .setHighLatency(builder)
                .setInitialized(builder);
        return builder;
    }

    private MiBandSupport readDate(TransactionBuilder builder) {
        builder.read(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_DATE_TIME));
        return this;
    }

    private MiBandSupport checkAuthenticationNeeded(TransactionBuilder builder, GBDevice device) {
        builder.add(new CheckAuthenticationNeededAction(device));
        return this;
    }

    /**
     * Last action of initialization sequence. Sets the device to initialized.
     * It is only invoked if all other actions were successfully run, so the device
     * must be initialized, then.
     *
     * @param builder
     */
    private void setInitialized(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
    }

    // TODO: tear down the notifications on quit
    private MiBandSupport enableNotifications(TransactionBuilder builder, boolean enable) {
        builder.notify(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_NOTIFICATION), enable);
        return this;
    }

    private MiBandSupport enableFurtherNotifications(TransactionBuilder builder, boolean enable) {
        builder.notify(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_REALTIME_STEPS), enable)
                .notify(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_ACTIVITY_DATA), enable)
                .notify(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_BATTERY), enable)
                .notify(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_SENSOR_DATA), enable);
        // cannot use supportsHeartrate() here because we don't have that information yet
        BluetoothGattCharacteristic heartrateCharacteristic = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT);
        if (heartrateCharacteristic != null) {
            builder.notify(heartrateCharacteristic, enable);
        }

        return this;
    }

    @Override
    public boolean useAutoConnect() {
        return true;
    }

    @Override
    public void pair() {
        for (int i = 0; i < 5; i++) {
            if (connect()) {
                return;
            }
        }
    }

    public DeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }

    private MiBandSupport sendDefaultNotification(TransactionBuilder builder, short repeat, BtLEAction extraAction) {
        Log.d(TAG, "Sending notification to MiBand: (" + repeat + " times)");
        NotificationStrategy strategy = getNotificationStrategy();
        for (short i = 0; i < repeat; i++) {
            strategy.sendDefaultNotification(builder, extraAction);
        }
        return this;
    }

    /**
     * Adds a custom notification to the given transaction builder
     *
     * @param vibrationProfile specifies how and how often the Band shall vibrate.
     * @param flashTimes
     * @param flashColour
     * @param originalColour
     * @param flashDuration
     * @param extraAction      an extra action to be executed after every vibration and flash sequence. Allows to abort the repetition, for example.
     * @param builder
     */
    private MiBandSupport sendCustomNotification(VibrationProfile vibrationProfile, int flashTimes, int flashColour, int originalColour, long flashDuration, BtLEAction extraAction, TransactionBuilder builder) {
        getNotificationStrategy().sendCustomNotification(vibrationProfile, flashTimes, flashColour, originalColour, flashDuration, extraAction, builder);
        Log.d(TAG, "Sending notification to MiBand");
        return this;
    }

    private NotificationStrategy getNotificationStrategy() {
        if (mDeviceInfo == null) {
            // not initialized yet?
            return new NoNotificationStrategy();
        }
        if (mDeviceInfo.getFirmwareVersion() < MiBandFWHelper.FW_16779790) {
            return new V1NotificationStrategy(this);
        } else {
            //use the new alert characteristic
            return new V2NotificationStrategy(this);
        }
    }

    /**
     * Part of device initialization process. Do not call manually.
     *
     * @param builder
     * @return
     */
    private MiBandSupport sendUserInfo(TransactionBuilder builder) {
        Log.d(TAG, "Writing User Info!");
        // Use a custom action instead of just builder.write() because mDeviceInfo
        // is set by handleDeviceInfo *after* this action is created.
        builder.add(new BtLEAction(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_USER_INFO)) {
            @Override
            public boolean expectsResult() {
                return true;
            }

            @Override
            public boolean run(BluetoothGatt gatt) {
                // at this point, mDeviceInfo should be set
                while (mDeviceInfo == null) {
                        SystemClock.sleep(1000);
                }
                return new WriteAction(getCharacteristic(),
                        MiBandCoordinator.getAnyUserInfo(getDevice().getAddress()).getData(mDeviceInfo)
                ).run(gatt);
            }
        });
        return this;
    }

    private MiBandSupport requestBatteryInfo(TransactionBuilder builder) {
        Log.d(TAG, "Requesting Battery Info!");
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_BATTERY);
        builder.read(characteristic);
        return this;
    }

    private MiBandSupport requestDeviceInfo(TransactionBuilder builder) {
        Log.d(TAG, "Requesting Device Info!");
        BluetoothGattCharacteristic deviceInfo = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_DEVICE_INFO);
        builder.read(deviceInfo);
        BluetoothGattCharacteristic deviceName = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_GAP_DEVICE_NAME);
        builder.read(deviceName);
        return this;
    }

    /**
     * Part of device initialization process. Do not call manually.
     *
     * @param transaction
     * @return
     */
    private MiBandSupport pair(TransactionBuilder transaction) {
        Log.d(TAG, "Attempting to pair MI device...");
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_PAIR);
        if (characteristic != null) {
            transaction.write(characteristic, new byte[]{2});
        } else {
            Log.d(TAG, "Unable to pair MI device -- characteristic not available");
        }
        return this;
    }

    /**
     * Part of device initialization process. Do not call manually.
     *
     * @param transaction
     * @return
     */

    private MiBandSupport setFitnessGoal(TransactionBuilder transaction) {
        Log.d(TAG, "Attempting to set Fitness Goal...");
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_CONTROL_POINT);
        if (characteristic != null) {
            int fitnessGoal = MiBandCoordinator.getFitnessGoal(getDevice().getAddress());
            transaction.write(characteristic, new byte[]{
                    MiBandService.COMMAND_SET_FITNESS_GOAL,
                    0,
                    (byte) (fitnessGoal & 0xff),
                    (byte) ((fitnessGoal >>> 8) & 0xff)
            });
        } else {
            Log.d(TAG, "Unable to set Fitness Goal");
        }
        return this;
    }

    /**
     * Part of device initialization process. Do not call manually.
     *
     * @param transaction
     * @return
     */
    private MiBandSupport setWearLocation(TransactionBuilder transaction) {
        Log.d(TAG, "Attempting to set wear location...");
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_CONTROL_POINT);
        if (characteristic != null) {
            int location = MiBandCoordinator.getWearLocation(getDevice().getAddress());
            transaction.write(characteristic, new byte[]{
                    MiBandService.COMMAND_SET_WEAR_LOCATION,
                    (byte) location
            });
        } else {
            Log.d(TAG, "Unable to set Wear Location");
        }
        return this;
    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {
        try {
            TransactionBuilder builder = performInitialized("enable heart rate sleep support: " + enable);
            setHeartrateSleepSupport(builder);
            builder.queue(getQueue());
        } catch (IOException e) {
//            GB.toast(getContext(), "Error toggling heart rate sleep support: " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
        }
    }

    /**
     * Part of device initialization process. Do not call manually.
     *
     * @param builder
     */
    private MiBandSupport setHeartrateSleepSupport(TransactionBuilder builder) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT);
        if (characteristic != null) {
            builder.add(new ConditionalWriteAction(characteristic) {
                @Override
                protected byte[] checkCondition() {
                    if (!supportsHeartRate()) {
                        return null;
                    }
                    if (MiBandCoordinator.getHeartrateSleepSupport(getDevice().getAddress())) {
                        Log.d(TAG, "Enabling heartrate sleep support...");
                        return startHeartMeasurementSleep;
                    } else {
                        Log.d(TAG, "Disabling heartrate sleep support...");
                        return stopHeartMeasurementSleep;
                    }
                }
            });
        }
        return this;
    }

    private void performDefaultNotification(String task, short repeat, BtLEAction extraAction) {
        try {
            TransactionBuilder builder = performInitialized(task);
            sendDefaultNotification(builder, repeat, extraAction);
            builder.queue(getQueue());
        } catch (IOException ex) {
            Log.d(TAG, "Unable to send notification to MI device", ex);
        }
    }

    private void performPreferredNotification(String task, String notificationOrigin, BtLEAction extraAction) {
        try {
            TransactionBuilder builder = performInitialized(task);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            int vibrateDuration = getPreferredVibrateDuration(notificationOrigin, prefs);
            int vibratePause = getPreferredVibratePause(notificationOrigin, prefs);
            short vibrateTimes = getPreferredVibrateCount(notificationOrigin, prefs);
            VibrationProfile profile = getPreferredVibrateProfile(notificationOrigin, prefs, vibrateTimes);

            int flashTimes = getPreferredFlashCount(notificationOrigin, prefs);
            int flashColour = getPreferredFlashColour(notificationOrigin, prefs);
            int originalColour = getPreferredOriginalColour(notificationOrigin, prefs);
            int flashDuration = getPreferredFlashDuration(notificationOrigin, prefs);

            sendCustomNotification(profile, flashTimes, flashColour, originalColour, flashDuration, extraAction, builder);
//            sendCustomNotification(vibrateDuration, vibrateTimes, vibratePause, flashTimes, flashColour, originalColour, flashDuration, builder);
            builder.queue(getQueue());
        } catch (IOException ex) {
            Log.d(TAG, "Unable to send notification to MI device", ex);
        }
    }

    private int getPreferredFlashDuration(String notificationOrigin, SharedPreferences prefs) {
        return getNotificationPrefIntValue(FLASH_DURATION, notificationOrigin, prefs, DEFAULT_VALUE_FLASH_DURATION);
    }

    private int getPreferredOriginalColour(String notificationOrigin, SharedPreferences prefs) {
        return getNotificationPrefIntValue(FLASH_ORIGINAL_COLOUR, notificationOrigin, prefs, DEFAULT_VALUE_FLASH_ORIGINAL_COLOUR);
    }

    private int getPreferredFlashColour(String notificationOrigin, SharedPreferences prefs) {
        return getNotificationPrefIntValue(FLASH_COLOUR, notificationOrigin, prefs, DEFAULT_VALUE_FLASH_COLOUR);
    }

    private int getPreferredFlashCount(String notificationOrigin, SharedPreferences prefs) {
        return getNotificationPrefIntValue(FLASH_COUNT, notificationOrigin, prefs, DEFAULT_VALUE_FLASH_COUNT);
    }

    private int getPreferredVibratePause(String notificationOrigin, SharedPreferences prefs) {
        return getNotificationPrefIntValue(VIBRATION_PAUSE, notificationOrigin, prefs, DEFAULT_VALUE_VIBRATION_PAUSE);
    }

    private short getPreferredVibrateCount(String notificationOrigin, SharedPreferences prefs) {
        return (short) Math.min(Short.MAX_VALUE, getNotificationPrefIntValue(VIBRATION_COUNT, notificationOrigin, prefs, DEFAULT_VALUE_VIBRATION_COUNT));
    }

    private int getPreferredVibrateDuration(String notificationOrigin, SharedPreferences prefs) {
        return getNotificationPrefIntValue(VIBRATION_DURATION, notificationOrigin, prefs, DEFAULT_VALUE_VIBRATION_DURATION);
    }

    private VibrationProfile getPreferredVibrateProfile(String notificationOrigin, SharedPreferences prefs, short repeat) {
        String profileId = getNotificationPrefStringValue(VIBRATION_PROFILE, notificationOrigin, prefs, DEFAULT_VALUE_VIBRATION_PROFILE);
        return VibrationProfile.getProfile(profileId, repeat);
    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {
        try {
            BluetoothGattCharacteristic characteristic = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_CONTROL_POINT);
            TransactionBuilder builder = performInitialized("Set alarm");
            boolean anyAlarmEnabled = false;
            for (Alarm alarm : alarms) {
                anyAlarmEnabled |= alarm.isEnabled();
                queueAlarm(alarm, builder, characteristic);
            }
            builder.queue(getQueue());
            if (anyAlarmEnabled) {
//                GB.toast(getContext(), getContext().getString(R.string.user_feedback_miband_set_alarms_ok), Toast.LENGTH_SHORT, GB.INFO);
            } else {
//                GB.toast(getContext(), getContext().getString(R.string.user_feedback_all_alarms_disabled), Toast.LENGTH_SHORT, GB.INFO);
            }
        } catch (IOException ex) {
//            GB.toast(getContext(), getContext().getString(R.string.user_feedback_miband_set_alarms_failed), Toast.LENGTH_LONG, GB.ERROR, ex);
        }
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        // FIXME: these ORIGIN contants do not really make sense anymore
        switch (notificationSpec.type) {
            default:
                performPreferredNotification("generic notification received", ORIGIN_GENERIC, null);
        }
    }

    @Override
    public void onSetTime() {
        try {
            TransactionBuilder builder = performInitialized("Set date and time");
            setCurrentTime(builder);
            builder.queue(getQueue());
        } catch (IOException ex) {
            Log.d(TAG, "Unable to set time on MI device", ex);
        }
        //TODO: once we have a common strategy for sending events (e.g. EventHandler), remove this call from here. Meanwhile it does no harm.
        sendCalendarEvents();
    }

    /**
     * Sets the current time to the Mi device using the given builder.
     *
     * @param builder
     */
    private MiBandSupport setCurrentTime(TransactionBuilder builder) {
        Calendar now = GregorianCalendar.getInstance();
        now.setTime(new Date());
        Date date = now.getTime();
        Log.d(TAG, "Sending current time to Mi Band: " + DateTimeUtils.formatDate(date) + " (" + new SimpleDateFormat().format(date) + ")");
        byte[] nowBytes = MiBandDateConverter.calendarToRawBytes(now);
        byte[] time = new byte[]{
                nowBytes[0],
                nowBytes[1],
                nowBytes[2],
                nowBytes[3],
                nowBytes[4],
                nowBytes[5],
                (byte) 0x0f,
                (byte) 0x0f,
                (byte) 0x0f,
                (byte) 0x0f,
                (byte) 0x0f,
                (byte) 0x0f
        };
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_DATE_TIME);
        if (characteristic != null) {
            builder.write(characteristic, time);
        } else {
            Log.d(TAG, "Unable to set time -- characteristic not available");
        }
        return this;
    }

    @Override
    public void onSetCallState(String number, String name, ServiceCommand command) {
        if (ServiceCommand.CALL_INCOMING.equals(command)) {
            telephoneRinging = true;
            AbortTransactionAction abortAction = new AbortTransactionAction() {
                @Override
                protected boolean shouldAbort() {
                    return !isTelephoneRinging();
                }
            };
            performPreferredNotification("incoming call", ORIGIN_INCOMING_CALL, abortAction);
        } else if (ServiceCommand.CALL_START.equals(command) || ServiceCommand.CALL_END.equals(command)) {
            telephoneRinging = false;
        }
    }

    private boolean isTelephoneRinging() {
        // don't synchronize, this is not really important
        return telephoneRinging;
    }

    @Override
    public void onSetMusicInfo(String artist, String album, String track, int duration, int trackCount, int trackNr) {
        // not supported
    }

    @Override
    public void onReboot() {
        try {
            TransactionBuilder builder = performInitialized("Reboot");
            builder.write(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_CONTROL_POINT), reboot);
            builder.queue(getQueue());
        } catch (IOException ex) {
            Log.d(TAG, "Unable to reboot MI", ex);
        }
    }

    @Override
    public void onHeartRateTest() {
        try {
            TransactionBuilder builder = performInitialized("HeartRateTest");
            ru.l240.miband.models.Log log = new ru.l240.miband.models.Log();
            log.setDate(new Date());
            log.setText("Try to get Heart Rate");
            Intent intent = new Intent(SettingsActivity.TAG);
            intent.putExtra("logText", log.getText());
            RealmHelper.save(Realm.getInstance(getContext()), log);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
            builder.write(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementContinuous);
            builder.write(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementManual);
            builder.write(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementSleep);
            builder.write(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), startHeartMeasurementManual);
            builder.queue(getQueue());
        } catch (IOException ex) {
            Log.d(TAG, "Unable to read HearRate in  MI1S", ex);
        }
    }

    public boolean supportsHeartRate() {
        return getDeviceInfo() != null && getDeviceInfo().supportsHeartrate();
    }

    @Override
    public void onFindDevice(boolean start) {
        isLocatingDevice = start;

        if (start) {
            AbortTransactionAction abortAction = new AbortTransactionAction() {
                @Override
                protected boolean shouldAbort() {
                    return !isLocatingDevice;
                }
            };
            performDefaultNotification("locating device", (short) 255, abortAction);
        }
    }

    @Override
    public void onFetchActivityData() {
        try {
            new FetchActivityOperation(this).perform();
        } catch (IOException ex) {
            Log.d(TAG, "Unable to fetch MI activity data", ex);
        }
    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
        try {
            BluetoothGattCharacteristic controlPoint = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_CONTROL_POINT);
            if (enable) {
                TransactionBuilder builder = performInitialized("Read realtime steps");
                builder.read(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_REALTIME_STEPS)).queue(getQueue());
            }
            performInitialized(enable ? "Enabling realtime steps notifications" : "Disabling realtime steps notifications")
                    .write(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_LE_PARAMS), enable ? getLowLatency() : getHighLatency())
                    .write(controlPoint, enable ? startRealTimeStepsNotifications : stopRealTimeStepsNotifications).queue(getQueue());
        } catch (IOException e) {
            Log.d(TAG, "Unable to change realtime steps notification to: " + enable, e);
        }
    }

    private byte[] getHighLatency() {
        int minConnectionInterval = 460;
        int maxConnectionInterval = 500;
        int latency = 0;
        int timeout = 500;
        int advertisementInterval = 0;

        return getLatency(minConnectionInterval, maxConnectionInterval, latency, timeout, advertisementInterval);
    }

    public MiBandSupport setHighLatency(TransactionBuilder builder) {
        builder.write(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_LE_PARAMS), getHighLatency());
        return this;
    }

    private byte[] getLatency(int minConnectionInterval, int maxConnectionInterval, int latency, int timeout, int advertisementInterval) {
        byte result[] = new byte[12];
        result[0] = (byte) (minConnectionInterval & 0xff);
        result[1] = (byte) (0xff & minConnectionInterval >> 8);
        result[2] = (byte) (maxConnectionInterval & 0xff);
        result[3] = (byte) (0xff & maxConnectionInterval >> 8);
        result[4] = (byte) (latency & 0xff);
        result[5] = (byte) (0xff & latency >> 8);
        result[6] = (byte) (timeout & 0xff);
        result[7] = (byte) (0xff & timeout >> 8);
        result[8] = 0;
        result[9] = 0;
        result[10] = (byte) (advertisementInterval & 0xff);
        result[11] = (byte) (0xff & advertisementInterval >> 8);

        return result;
    }

    private byte[] getLowLatency() {
        int minConnectionInterval = 39;
        int maxConnectionInterval = 49;
        int latency = 0;
        int timeout = 500;
        int advertisementInterval = 0;

        return getLatency(minConnectionInterval, maxConnectionInterval, latency, timeout, advertisementInterval);
    }

    public MiBandSupport setLowLatency(TransactionBuilder builder) {
        builder.write(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_LE_PARAMS), getLowLatency());
        return this;
    }

    @Override
    public void onInstallApp(Uri uri) {
        try {
            new UpdateFirmwareOperation(uri, this).perform();
        } catch (IOException ex) {
//            GB.toast(getContext(), "Firmware cannot be installed: " + ex.getMessage(), Toast.LENGTH_LONG, GB.ERROR, ex);
        }
    }

    @Override
    public void onAppInfoReq() {
        // not supported
    }

    @Override
    public void onAppStart(UUID uuid, boolean start) {
        // not supported
    }

    @Override
    public void onAppDelete(UUID uuid) {
        // not supported
    }

    @Override
    public void onAppConfiguration(UUID uuid, String config) {
        // not supported
    }

    @Override
    public void onScreenshotReq() {
        // not supported
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        UUID characteristicUUID = characteristic.getUuid();
        if (MiBandService.UUID_CHARACTERISTIC_BATTERY.equals(characteristicUUID)) {
            handleBatteryInfo(characteristic.getValue(), BluetoothGatt.GATT_SUCCESS);
        } else if (MiBandService.UUID_CHARACTERISTIC_NOTIFICATION.equals(characteristicUUID)) {
            handleNotificationNotif(characteristic.getValue());
        } else if (MiBandService.UUID_CHARACTERISTIC_REALTIME_STEPS.equals(characteristicUUID)) {
            handleRealtimeSteps(characteristic.getValue());
        } else if (MiBandService.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT.equals(characteristicUUID)) {
            logHeartrate(characteristic.getValue());
        } else {
            Log.d(TAG, "Unhandled characteristic changed: " + characteristicUUID);
            logMessageContent(characteristic.getValue());
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        UUID characteristicUUID = characteristic.getUuid();
        if (MiBandService.UUID_CHARACTERISTIC_DEVICE_INFO.equals(characteristicUUID)) {
            handleDeviceInfo(characteristic.getValue(), status);
        } else if (GattCharacteristic.UUID_CHARACTERISTIC_GAP_DEVICE_NAME.equals(characteristicUUID)) {
            handleDeviceName(characteristic.getValue(), status);
        } else if (MiBandService.UUID_CHARACTERISTIC_REALTIME_STEPS.equals(characteristicUUID)) {
            handleRealtimeSteps(characteristic.getValue());
        } else if (MiBandService.UUID_CHARACTERISTIC_BATTERY.equals(characteristicUUID)) {
            handleBatteryInfo(characteristic.getValue(), status);
        } else if (MiBandService.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT.equals(characteristicUUID)) {
            logHeartrate(characteristic.getValue());
        } else if (MiBandService.UUID_CHARACTERISTIC_DATE_TIME.equals(characteristicUUID)) {
            logDate(characteristic.getValue());
        } else {
            Log.d(TAG, "Unhandled characteristic read: " + characteristicUUID);
            logMessageContent(characteristic.getValue());
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        UUID characteristicUUID = characteristic.getUuid();
        if (MiBandService.UUID_CHARACTERISTIC_PAIR.equals(characteristicUUID)) {
            handlePairResult(characteristic.getValue(), status);
        } else if (MiBandService.UUID_CHARACTERISTIC_USER_INFO.equals(characteristicUUID)) {
            handleUserInfoResult(characteristic.getValue(), status);
        } else if (MiBandService.UUID_CHARACTERISTIC_CONTROL_POINT.equals(characteristicUUID)) {
            handleControlPointResult(characteristic.getValue(), status);
        }
    }

    /**
     * Utility method that may be used to log incoming messages when we don't know how to deal with them yet.
     *
     * @param value
     */
    public void logMessageContent(byte[] value) {
        Log.d(TAG, "RECEIVED DATA WITH LENGTH: " + value.length);
        for (byte b : value) {
            Log.d(TAG, "DATA: " + String.format("0x%2x", b));
        }
    }

    public void logDate(byte[] value) {
        GregorianCalendar calendar = MiBandDateConverter.rawBytesToCalendar(value);
        Log.d(TAG, "Got Mi Band Date: " + DateTimeUtils.formatDateTime(calendar.getTime()));
    }

    public void logHeartrate(byte[] value) {
        Log.d(TAG, "Got heartrate:");
        if (value.length == 2 && value[0] == 6) {
            int hrValue = (value[1] & 0xff);
            if (hrValue >= 60 && hrValue <= 90) {
                NotificationUtils.getInstance(getContext()).cancelAllAlarmNotify();
                NotificationUtils.getInstance(getContext()).createAlarmNotify(DateUtils.addMinutes(new Date(), NotificationUtils.MIN_5), NotificationUtils.MIN_5);
            } else if ((hrValue >= 91 && hrValue <= 120) || (hrValue >= 46 && hrValue <= 59)) {
                NotificationUtils.getInstance(getContext()).cancelAllAlarmNotify();
                NotificationUtils.getInstance(getContext()).createAlarmNotify(DateUtils.addMinutes(new Date(), NotificationUtils.MIN_2), NotificationUtils.MIN_2);
            } else if (hrValue >= 121 || hrValue <= 45) {
                NotificationUtils.getInstance(getContext()).cancelAllAlarmNotify();
                NotificationUtils.getInstance(getContext()).createAlarmNotify(DateUtils.addMinutes(new Date(), NotificationUtils.MIN_1), NotificationUtils.MIN_1);
            }
            Intent intent = new Intent();
            intent.setAction(HEART_RATE_ACTION);
            intent.putExtra("heartrate", String.valueOf(hrValue));
            ru.l240.miband.models.Log log = new ru.l240.miband.models.Log();
            log.setDate(new Date());
            log.setText("Got heartrate: " + String.valueOf(hrValue));
            Intent intentSA = new Intent(SettingsActivity.TAG);
            intentSA.putExtra("logText", log.getText());
            RealmHelper.save(Realm.getInstance(getContext()), log);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intentSA);
            getContext().sendBroadcast(intentSA);
            UserMeasurement measurement = new UserMeasurement();
            measurement.setMeasurementId(3);
            measurement.setMeasurementDate(new Date());
            measurement.setStrValue(String.valueOf(hrValue));
            if (MedUtils.isNetworkConnected(getContext())) {
                RequestTaskAddMeasurement addMeasurement = new RequestTaskAddMeasurement(getContext(), false, Collections.singletonList(measurement)) {
                    @Override
                    protected void onPostExecute(Boolean success) {
                        super.onPostExecute(success);
                        MiApplication.deviceService().disconnect();
//                        bleSingleton.disconnect();
                    }
                };
                addMeasurement.execute();
            } else {
                RealmHelper.save(Realm.getInstance(getContext()), measurement);
            }
            Intent intent1 = new Intent(GBDevice.ACTION_DEVICE_CHANGED);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent1);
//            GB.toast(getContext(), "Heart Rate measured: " + hrValue, Toast.LENGTH_LONG, GB.INFO);
        } else {
            logMessageContent(value);
        }
    }


    private void handleRealtimeSteps(byte[] value) {
        int steps = 0xff & value[0] | (0xff & value[1]) << 8;
        Log.d(TAG, "realtime steps: " + steps);
        Intent intent = new Intent(DeviceService.ACTION_REALTIME_STEPS)
                .putExtra(DeviceService.EXTRA_REALTIME_STEPS, steps)
                .putExtra(DeviceService.EXTRA_TIMESTAMP, System.currentTimeMillis());
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        Prefs.putInt("STEPS", steps);
    }

    /**
     * React to unsolicited messages sent by the Mi Band to the MiBandService.UUID_CHARACTERISTIC_NOTIFICATION
     * characteristic,
     * These messages appear to be always 1 byte long, with values that are listed in MiBandService.
     * It is not excluded that there are further values which are still unknown.
     * <p>
     * Upon receiving known values that request further action by GB, the appropriate method is called.
     *
     * @param value
     */
    private void handleNotificationNotif(byte[] value) {
        if (value.length != 1) {
            Log.d(TAG, "Notifications should be 1 byte long.");
            Log.d(TAG, "RECEIVED DATA WITH LENGTH: " + value.length);
            for (byte b : value) {
                Log.d(TAG, "DATA: " + String.format("0x%2x", b));
            }
            return;
        }
        switch (value[0]) {
            case MiBandService.NOTIFY_AUTHENTICATION_FAILED:
                // we get first FAILED, then NOTIFY_STATUS_MOTOR_AUTH (0x13)
                // which means, we need to authenticate by tapping
                getDevice().setState(GBDevice.State.AUTHENTICATION_REQUIRED);
                getDevice().sendDeviceUpdateIntent(getContext());
//                GB.toast(getContext(), "Band needs pairing", Toast.LENGTH_LONG, GB.ERROR);
                break;
            case MiBandService.NOTIFY_AUTHENTICATION_SUCCESS: // fall through -- not sure which one we get
            case MiBandService.NOTIFY_RESET_AUTHENTICATION_SUCCESS: // for Mi 1A
            case MiBandService.NOTIFY_STATUS_MOTOR_AUTH_SUCCESS:
                Log.d(TAG, "Band successfully authenticated");
                // maybe we can perform the rest of the initialization from here
                doInitialize();
                break;

            case MiBandService.NOTIFY_STATUS_MOTOR_AUTH:
                Log.d(TAG, "Band needs authentication (MOTOR_AUTH)");
                /*getDevice().setState(GBDevice.State.AUTHENTICATING);
                getDevice().sendDeviceUpdateIntent(getContext());*/
                break;

            default:
                for (byte b : value) {
                    Log.d(TAG, "DATA: " + String.format("0x%2x", b));
                }
        }
    }

    private void doInitialize() {
        try {
            TransactionBuilder builder = performInitialized("just initializing after authentication");
            builder.queue(getQueue());
        } catch (IOException ex) {
            Log.d(TAG, "Unable to initialize device after authentication", ex);
        }
    }

    private void handleDeviceInfo(byte[] value, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mDeviceInfo = new DeviceInfo(value);
            mDeviceInfo.setTest1AHRMode(MI_1A_HR_FW_UPDATE_TEST_MODE_ENABLED);
            if (getDeviceInfo().supportsHeartrate()) {
                getDevice().addDeviceInfo(new GenericItem(
                        getContext().getString(R.string.DEVINFO_HR_VER),
                        MiBandFWHelper.formatFirmwareVersion(mDeviceInfo.getHeartrateFirmwareVersion())));
            }
            Log.d(TAG, "Device info: " + mDeviceInfo);
            versionCmd.hwVersion = mDeviceInfo.getHwVersion();
            versionCmd.fwVersion = MiBandFWHelper.formatFirmwareVersion(mDeviceInfo.getFirmwareVersion());
            handleGBDeviceEvent(versionCmd);
        }
    }

    private void handleDeviceName(byte[] value, int status) {
//        if (status == BluetoothGatt.GATT_SUCCESS) {
//            versionCmd.hwVersion = new String(value);
//            handleGBDeviceEvent(versionCmd);
//        }
    }

    /**
     * Convert an alarm from the GB internal structure to a Mi Band message and put on the specified
     * builder queue as a write message for the passed characteristic
     *
     * @param alarm
     * @param builder
     * @param characteristic
     */
    private void queueAlarm(Alarm alarm, TransactionBuilder builder, BluetoothGattCharacteristic characteristic) {
        byte[] alarmCalBytes = MiBandDateConverter.calendarToRawBytes(alarm.getAlarmCal());

        byte[] alarmMessage = new byte[]{
                MiBandService.COMMAND_SET_TIMER,
                (byte) alarm.getIndex(),
                (byte) (alarm.isEnabled() ? 1 : 0),
                alarmCalBytes[0],
                alarmCalBytes[1],
                alarmCalBytes[2],
                alarmCalBytes[3],
                alarmCalBytes[4],
                alarmCalBytes[5],
                (byte) (alarm.isSmartWakeup() ? 30 : 0),
                (byte) alarm.getRepetitionMask()
        };
        builder.write(characteristic, alarmMessage);
    }

    private void handleControlPointResult(byte[] value, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "Could not write to the control point.");
        }
        Log.d(TAG, "handleControlPoint write status:" + status + "; length: " + (value != null ? value.length : "(null)"));

        if (value != null) {
            for (byte b : value) {
                Log.d(TAG, "handleControlPoint WROTE DATA:" + String.format("0x%8x", b));
            }
        } else {
            Log.d(TAG, "handleControlPoint WROTE null");
        }
    }

    private void handleBatteryInfo(byte[] value, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            BatteryInfo info = new BatteryInfo(value);
            batteryCmd.level = ((short) info.getLevelInPercent());
            batteryCmd.state = info.getState();
            batteryCmd.lastChargeTime = info.getLastChargeTime();
            batteryCmd.numCharges = info.getNumCharges();
//            handleGBDeviceEvent(batteryCmd);
            Prefs.putInt("BATTERY", batteryCmd.level);
        }
    }

    private void handleUserInfoResult(byte[] value, int status) {
        // successfully transferred user info means we're initialized
// commented out, because we have SetDeviceStateAction which sets initialized
// state on every successful initialization.
//        if (status == BluetoothGatt.GATT_SUCCESS) {
//            setConnectionState(State.INITIALIZED);
//        }
    }

    private void setConnectionState(GBDevice.State newState) {
        getDevice().setState(newState);
        getDevice().sendDeviceUpdateIntent(getContext());
    }

    private void handlePairResult(byte[] pairResult, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "Pairing MI device failed: " + status);
            return;
        }

        String value = null;
        if (pairResult != null) {
            if (pairResult.length == 1) {
                try {
                    if (pairResult[0] == 2) {
                        Log.d(TAG, "Successfully paired  MI device");
                        return;
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "Error identifying pairing result", ex);
                    return;
                }
            }
            value = Arrays.toString(pairResult);
        }
        Log.d(TAG, "MI Band pairing result: " + value);
    }

    /**
     * Fetch the events from the android device calendars and set the alarms on the miband.
     */
    private void sendCalendarEvents() {
        try {
            TransactionBuilder builder = performInitialized("Send upcoming events");
            BluetoothGattCharacteristic characteristic = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_CONTROL_POINT);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            int availableSlots = Integer.parseInt(prefs.getString(PREF_MIBAND_RESERVE_ALARM_FOR_CALENDAR, "0"));

            if (availableSlots > 0) {
                CalendarEvents upcomingEvents = new CalendarEvents();
                List<CalendarEvents.CalendarEvent> mEvents = upcomingEvents.getCalendarEventList(getContext());

                int iteration = 0;
                for (CalendarEvents.CalendarEvent mEvt : mEvents) {
                    if (iteration >= availableSlots || iteration > 2) {
                        break;
                    }
                    int slotToUse = 2 - iteration;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(mEvt.getBegin());
                    byte[] calBytes = MiBandDateConverter.calendarToRawBytes(calendar);

                    byte[] alarmMessage = new byte[]{
                            MiBandService.COMMAND_SET_TIMER,
                            (byte) slotToUse,
                            (byte) 1,
                            calBytes[0],
                            calBytes[1],
                            calBytes[2],
                            calBytes[3],
                            calBytes[4],
                            calBytes[5],
                            (byte) 0,
                            (byte) 0
                    };
                    builder.write(characteristic, alarmMessage);
                    iteration++;
                }
                builder.queue(getQueue());
            }
        } catch (IOException ex) {
            Log.d(TAG, "Unable to send Events to MI device", ex);
        }
    }


}

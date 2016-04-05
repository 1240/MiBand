package ru.l240.miband.gadgetbridge.devices;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.l240.miband.MiApplication;
import ru.l240.miband.gadgetbridge.devices.miband.MiBandConst;
import ru.l240.miband.gadgetbridge.devices.miband.MiBandSampleProvider;
import ru.l240.miband.gadgetbridge.devices.miband.MiBandService;
import ru.l240.miband.gadgetbridge.devices.miband.UserInfo;
import ru.l240.miband.gadgetbridge.impl.GBDevice;
import ru.l240.miband.gadgetbridge.impl.GBDeviceCandidate;
import ru.l240.miband.gadgetbridge.model.ActivityUser;
import ru.l240.miband.gadgetbridge.model.DeviceType;


public class MiBandCoordinator extends AbstractDeviceCoordinator {
    private static final Logger LOG = LoggerFactory.getLogger(MiBandCoordinator.class);
    private final MiBandSampleProvider sampleProvider;

    public MiBandCoordinator() {
        sampleProvider = new MiBandSampleProvider();
    }

    @Override
    public boolean supports(GBDeviceCandidate candidate) {
        String macAddress = candidate.getMacAddress().toUpperCase();
        return macAddress.startsWith(MiBandService.MAC_ADDRESS_FILTER_1_1A)
                || macAddress.startsWith(MiBandService.MAC_ADDRESS_FILTER_1S);
    }

    @Override
    public boolean supports(GBDevice device) {
        return getDeviceType().equals(device.getType());
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.MIBAND;
    }

    @Override
    public Class<? extends Activity> getPairingActivity() {
        return null;
    }

    @Override
    public Class<? extends Activity> getPrimaryActivity() {
        return null;
    }

    @Override
    public SampleProvider getSampleProvider() {
        return sampleProvider;
    }

    @Override
    public boolean supportsActivityDataFetching() {
        return true;
    }

    @Override
    public boolean supportsScreenshots() {
        return false;
    }

    @Override
    public boolean supportsAlarmConfiguration() {
        return true;
    }

    public static boolean hasValidUserInfo() {
        String dummyMacAddress = MiBandService.MAC_ADDRESS_FILTER_1_1A + ":00:00:00";
        try {
            UserInfo userInfo = getConfiguredUserInfo(dummyMacAddress);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Returns the configured user info, or, if that is not available or invalid,
     * a default user info.
     *
     * @param miBandAddress
     */
    public static UserInfo getAnyUserInfo(String miBandAddress) {
        try {
            return getConfiguredUserInfo(miBandAddress);
        } catch (Exception ex) {
            LOG.error("Error creating user info from settings, using default user instead: " + ex);
            return UserInfo.getDefault(miBandAddress);
        }
    }

    /**
     * Returns the user info from the user configured data in the preferences.
     *
     * @param miBandAddress
     * @throws IllegalArgumentException when the user info can not be created
     */
    public static UserInfo getConfiguredUserInfo(String miBandAddress) throws IllegalArgumentException {
        ActivityUser activityUser = new ActivityUser();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MiApplication.getContext());

        UserInfo info = UserInfo.create(
                miBandAddress,
                prefs.getString(MiBandConst.PREF_USER_ALIAS, null),
                activityUser.getActivityUserGender(),
                activityUser.getActivityUserAge(),
                activityUser.getActivityUserHeightCm(),
                activityUser.getActivityUserWeightKg(),
                0
        );
        return info;
    }

    public static int getWearLocation(String miBandAddress) throws IllegalArgumentException {
        int location = 0; //left hand
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MiApplication.getContext());
        if ("right".equals(prefs.getString(MiBandConst.PREF_MIBAND_WEARSIDE, "left"))) {
            location = 1; // right hand
        }
        return location;
    }

    public static boolean getHeartrateSleepSupport(String miBandAddress) throws IllegalArgumentException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MiApplication.getContext());
        return prefs.getBoolean(MiBandConst.PREF_MIBAND_USE_HR_FOR_SLEEP_DETECTION, false);
    }

    public static int getFitnessGoal(String miBandAddress) throws IllegalArgumentException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MiApplication.getContext());
        return Integer.parseInt(prefs.getString(MiBandConst.PREF_MIBAND_FITNESS_GOAL, "10000"));
    }

    public static int getReservedAlarmSlots(String miBandAddress) throws IllegalArgumentException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MiApplication.getContext());
        return Integer.parseInt(prefs.getString(MiBandConst.PREF_MIBAND_RESERVE_ALARM_FOR_CALENDAR, "0"));
    }
}

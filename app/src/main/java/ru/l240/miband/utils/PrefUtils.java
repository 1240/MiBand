package ru.l240.miband.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Alexander Popov created on 29.02.2016.
 */
public class PrefUtils {

    public static final String PREF_NAME = "MiBand";
    public static final String PREF_ADDR = "address_mi_ble";
    public static final String PREF_ALARMID = "alarm_id";

    public static void saveAddress(Context mContext, String address) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_ADDR, address);
        editor.commit();
    }

    public static final String getAddress(Context mContext) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_ADDR, "");
    }

    public static void saveAlarmId(Context mContext, int alarmLastId) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_ALARMID, alarmLastId);
        editor.commit();
    }

    public static final int getLastAlarmId(Context mContext) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_ALARMID, 0);
    }

}

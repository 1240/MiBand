package ru.l240.miband.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Alexander Popov created on 29.02.2016.
 */
public class PrefUtils {

    public static final String PREF_NAME = "MiBand";
    public static final String PREF_ADDR = "address_mi_ble";

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

}

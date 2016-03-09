package ru.l240.miband.utils.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.l240.miband.models.UserMeasurement;
import ru.l240.miband.retrofit.ApiFac;
import ru.l240.miband.retrofit.ApiService;
import ru.l240.miband.utils.MedUtils;

/**
 * @author Alexander Popov on 22.05.15.
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context mContext, Intent intent) {
        Log.d("app", "Network connectivity change");
        if (intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                Log.i("app", "Network " + ni.getTypeName() + " connected");
                SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
                String mes = preferences.getString(MedUtils.SCHEDULER_MES_PREF, "");
                String journal = preferences.getString(MedUtils.SCHEDULER_JOURNAL_PREF, "");
                String message = preferences.getString(MedUtils.SCHEDULER_MESSAGES_PREF, "");
                String feedbackAnswer = preferences.getString(MedUtils.SCHEDULER_FEEDBACK_ANSWERS_PREF, "");
                String exec = preferences.getString(MedUtils.SCHEDULER_EXEC_PREF, "");
                String execDel = preferences.getString(MedUtils.SCHEDULER_EXEC_DELETE_PREF, "");
                //long contactId = preferences.getLong("ContactId", 0);
                final ApiService service = ApiFac.getApiService();
                SharedPreferences cookiePreferences = mContext.getSharedPreferences(MedUtils.COOKIE_PREF, 0);
                final String cookie = cookiePreferences.getString(MedUtils.COOKIE_PREF, "");
                ContentResolver contentResolver = mContext.getContentResolver();
                if (!mes.isEmpty()) {
                    /*List<UserMeasurement> sync = new MedDTO<UserMeasurement>()
                            .getSync(new UserMeasurement(),
                                    MedContract.UserMeasurement.CONTENT_URI,
                                    MedContract.UserMeasurement.DEFAULT_PROJECTION,
                                    mContext);
                    */
                }
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d("app", "There's no network connectivity");
            }
        }
    }

    protected void updateLV() {

    }
}


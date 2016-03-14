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
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import ru.l240.miband.models.UserMeasurement;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.retrofit.ApiFac;
import ru.l240.miband.retrofit.ApiService;
import ru.l240.miband.retrofit.RequestTaskAddMeasurement;
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
                List<UserMeasurement> syncAll = RealmHelper.getAll(Realm.getInstance(mContext), UserMeasurement.class);
                if (syncAll.isEmpty()) {
                    RequestTaskAddMeasurement addMeasurement = new RequestTaskAddMeasurement(mContext, true, syncAll);
                    addMeasurement.execute();
                }
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d("app", "There's no network connectivity");
            }
        }
    }

    protected void updateLV() {

    }
}


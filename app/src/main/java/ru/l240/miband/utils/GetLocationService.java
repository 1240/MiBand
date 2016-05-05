package ru.l240.miband.utils;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import ru.l240.miband.SettingsActivity;
import ru.l240.miband.models.UserMeasurement;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.retrofit.RequestTaskAddMeasurement;

/**
 * @author Alexander Popov on 21.07.2015.
 */
public class GetLocationService extends IntentService {

    public static final String TAG = GetLocationService.class.getSimpleName();

    public GetLocationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ru.l240.miband.models.Log log = new ru.l240.miband.models.Log();
        log.setDate(new Date());
        log.setText("Try get location");
        RealmHelper.save(Realm.getInstance(getApplicationContext()), log);
        Intent intentSA = new Intent(SettingsActivity.TAG);
        intentSA.putExtra("logText", log.getText());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentSA);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        if ((latitude <= -90 && latitude >= 90)
                && (longitude <= -180 && longitude >= 180))
            return;
        final List<UserMeasurement> measurements = new ArrayList<>();
        UserMeasurement lat = new UserMeasurement();
        UserMeasurement lon = new UserMeasurement();
        lon.setMeasurementId(42);
        lat.setMeasurementId(41);
        lon.setStrValue(String.valueOf(longitude));
        lat.setStrValue(String.valueOf(latitude));
        lon.setMeasurementDate(new Date());
        lat.setMeasurementDate(new Date());
        measurements.add(lat);
        measurements.add(lon);
        ru.l240.miband.models.Log log2 = new ru.l240.miband.models.Log();
        log2.setDate(new Date());
        log2.setText(String.format("Location: latitude - %s, longitude - %s", latitude, longitude));
        RealmHelper.save(Realm.getInstance(getApplicationContext()), log2);
        Intent intent1 = new Intent(SettingsActivity.TAG);
        intent1.putExtra("logText", log2.getText());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RequestTaskAddMeasurement addMeasurement = new RequestTaskAddMeasurement(getApplicationContext(), false, measurements);
                addMeasurement.execute();
            }
        });
        thread.start();
    }
}

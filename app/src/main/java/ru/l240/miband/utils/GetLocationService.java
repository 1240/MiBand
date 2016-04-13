package ru.l240.miband.utils;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.l240.miband.models.UserMeasurement;
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

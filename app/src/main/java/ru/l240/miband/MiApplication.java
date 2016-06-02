package ru.l240.miband;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import com.pixplicity.easyprefs.library.Prefs;

import ru.l240.miband.gadgetbridge.impl.GBDeviceService;
import ru.l240.miband.gadgetbridge.model.DeviceService;
import ru.l240.miband.utils.errorcollectors.ErrorReporter;

/**
 * Created by l24o on 04.04.16.
 */
public class MiApplication extends Application {

    private static MiApplication context;
    private static DeviceService deviceService;

    public MiApplication() {
        context = this;
    }

    public static Context getContext() {
        return context;
    }

    public static DeviceService deviceService() {
        return deviceService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ErrorReporter.bindReporter(getApplicationContext());
        deviceService = createDeviceService();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

    protected DeviceService createDeviceService() {
        return new GBDeviceService(this);
    }

}

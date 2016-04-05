package ru.l240.miband;

import android.app.Application;
import android.content.Context;

import ru.l240.miband.gadgetbridge.impl.GBDeviceService;
import ru.l240.miband.gadgetbridge.model.DeviceService;

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
        deviceService = createDeviceService();
    }

    protected DeviceService createDeviceService() {
        return new GBDeviceService(this);
    }

}

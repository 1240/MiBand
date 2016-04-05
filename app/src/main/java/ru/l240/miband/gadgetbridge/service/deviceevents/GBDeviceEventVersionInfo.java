package ru.l240.miband.gadgetbridge.service.deviceevents;


import ru.l240.miband.MiApplication;
import ru.l240.miband.R;

public class GBDeviceEventVersionInfo extends GBDeviceEvent {
    public String fwVersion = MiApplication.getContext().getString(R.string.n_a);
    public String hwVersion = MiApplication.getContext().getString(R.string.n_a);
}

package ru.l240.miband.gadgetbridge.service.deviceevents;


import ru.l240.miband.gadgetbridge.impl.GBDeviceApp;

public class GBDeviceEventAppInfo extends GBDeviceEvent {
    public GBDeviceApp apps[];
    public byte freeSlot = -1;
}

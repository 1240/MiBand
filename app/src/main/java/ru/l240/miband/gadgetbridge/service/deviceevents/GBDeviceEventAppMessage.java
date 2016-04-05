package ru.l240.miband.gadgetbridge.service.deviceevents;

import java.util.UUID;

public class GBDeviceEventAppMessage extends GBDeviceEvent {
    public UUID appUUID;
    public int id;
    public String message;
}

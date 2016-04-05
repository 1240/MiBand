package ru.l240.miband.gadgetbridge.service.deviceevents;

public class GBDeviceEventNotificationControl extends GBDeviceEvent {
    public int handle;
    public String reply;
    public Event event = Event.UNKNOWN;

    public enum Event {
        UNKNOWN,
        DISMISS,
        DISMISS_ALL,
        OPEN,
        MUTE,
        REPLY,
    }
}

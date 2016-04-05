package ru.l240.miband.gadgetbridge.devices;


import ru.l240.miband.gadgetbridge.impl.GBDevice;

public abstract class AbstractDeviceCoordinator implements DeviceCoordinator {
    public boolean allowFetchActivityData(GBDevice device) {
        return device.isInitialized() && !device.isBusy() && supportsActivityDataFetching();
    }
}

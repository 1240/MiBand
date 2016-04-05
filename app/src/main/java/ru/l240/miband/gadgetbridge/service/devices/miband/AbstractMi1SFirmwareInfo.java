package ru.l240.miband.gadgetbridge.service.devices.miband;

import android.support.annotation.NonNull;

import ru.l240.miband.gadgetbridge.devices.miband.MiBandConst;
import ru.l240.miband.gadgetbridge.impl.GBDevice;


public abstract class AbstractMi1SFirmwareInfo extends AbstractMiFirmwareInfo {

    public AbstractMi1SFirmwareInfo(@NonNull byte[] wholeFirmwareBytes) {
        super(wholeFirmwareBytes);
    }

    @Override
    public boolean isGenerallyCompatibleWith(GBDevice device) {
        return MiBandConst.MI_1S.equals(device.getHardwareVersion());
    }

    @Override
    public boolean isSingleMiBandFirmware() {
        return false;
    }
}

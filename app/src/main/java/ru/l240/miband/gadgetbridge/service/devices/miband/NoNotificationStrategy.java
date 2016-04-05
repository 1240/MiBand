package ru.l240.miband.gadgetbridge.service.devices.miband;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.l240.miband.gadgetbridge.devices.miband.VibrationProfile;
import ru.l240.miband.gadgetbridge.service.btle.BtLEAction;
import ru.l240.miband.gadgetbridge.service.btle.TransactionBuilder;


/**
 * Does not do anything.
 */
public class NoNotificationStrategy implements NotificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(NoNotificationStrategy.class);

    @Override
    public void sendDefaultNotification(TransactionBuilder builder, BtLEAction extraAction) {
        LOG.info("dummy notification stragegy: default notification");
    }

    @Override
    public void sendCustomNotification(VibrationProfile vibrationProfile, int flashTimes, int flashColour, int originalColour, long flashDuration, BtLEAction extraAction, TransactionBuilder builder) {
        LOG.info("dummy notification stragegy: custom notification");
    }
}

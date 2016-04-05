package ru.l240.miband.gadgetbridge.service.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import ru.l240.miband.gadgetbridge.model.NotificationSpec;
import ru.l240.miband.gadgetbridge.model.ServiceCommand;
import ru.l240.miband.gadgetbridge.service.AbstractDeviceSupport;
import ru.l240.miband.gadgetbridge.service.deviceevents.GBDeviceEvent;
import ru.l240.miband.gadgetbridge.service.deviceevents.GBDeviceEventSendBytes;


public abstract class AbstractSerialDeviceSupport extends AbstractDeviceSupport {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDeviceSupport.class);

    protected GBDeviceProtocol gbDeviceProtocol;
    protected GBDeviceIoThread gbDeviceIOThread;

    /**
     * Factory method to create the device specific GBDeviceProtocol instance to be used.
     */
    protected abstract GBDeviceProtocol createDeviceProtocol();

    /**
     * Factory method to create the device specific GBDeviceIoThread instance to be used.
     */
    protected abstract GBDeviceIoThread createDeviceIOThread();

    @Override
    public void dispose() {
        // currently only one thread allowed
        if (gbDeviceIOThread != null) {
            gbDeviceIOThread.quit();
            try {
                gbDeviceIOThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gbDeviceIOThread = null;
        }
    }

    @Override
    public void pair() {
        // Default implementation does no manual pairing, use the Android
        // pairing dialog instead.
    }

    /**
     * Lazily creates and returns the GBDeviceProtocol instance to be used.
     */
    public synchronized GBDeviceProtocol getDeviceProtocol() {
        if (gbDeviceProtocol == null) {
            gbDeviceProtocol = createDeviceProtocol();
        }
        return gbDeviceProtocol;
    }

    /**
     * Lazily creates and returns the GBDeviceIoThread instance to be used.
     */
    public synchronized GBDeviceIoThread getDeviceIOThread() {
        if (gbDeviceIOThread == null) {
            gbDeviceIOThread = createDeviceIOThread();
        }
        return gbDeviceIOThread;
    }

    /**
     * Sends the given message to the device. This implementation delegates the
     * writing to the {@link #getDeviceIOThread device io thread}
     *
     * @param bytes the message to send to the device
     */
    protected void sendToDevice(byte[] bytes) {
        if (bytes != null && gbDeviceIOThread != null) {
            gbDeviceIOThread.write(bytes);
        }
    }

    public void handleGBDeviceEvent(GBDeviceEventSendBytes sendBytes) {
        sendToDevice(sendBytes.encodedBytes);
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        byte[] bytes = gbDeviceProtocol.encodeNotification(notificationSpec);
        sendToDevice(bytes);
    }

    @Override
    public void onSetTime() {
        byte[] bytes = gbDeviceProtocol.encodeSetTime();
        sendToDevice(bytes);
    }

    @Override
    public void onSetCallState(String number, String name, ServiceCommand command) {
        byte[] bytes = gbDeviceProtocol.encodeSetCallState(number, name, command);
        sendToDevice(bytes);
    }

    @Override
    public void onSetMusicInfo(String artist, String album, String track, int duration, int trackCount, int trackNr) {
        byte[] bytes = gbDeviceProtocol.encodeSetMusicInfo(artist, album, track, duration, trackCount, trackNr);
        sendToDevice(bytes);
    }

    @Override
    public void onAppInfoReq() {
        byte[] bytes = gbDeviceProtocol.encodeAppInfoReq();
        sendToDevice(bytes);
    }

    @Override
    public void onAppStart(UUID uuid, boolean start) {
        byte[] bytes = gbDeviceProtocol.encodeAppStart(uuid, start);
        sendToDevice(bytes);
    }

    @Override
    public void onAppDelete(UUID uuid) {
        byte[] bytes = gbDeviceProtocol.encodeAppDelete(uuid);
        sendToDevice(bytes);
    }

    @Override
    public void onFetchActivityData() {
        byte[] bytes = gbDeviceProtocol.encodeSynchronizeActivityData();
        sendToDevice(bytes);
    }

    @Override
    public void onReboot() {
        byte[] bytes = gbDeviceProtocol.encodeReboot();
        sendToDevice(bytes);
    }

    @Override
    public void onFindDevice(boolean start) {
        byte[] bytes = gbDeviceProtocol.encodeFindDevice(start);
        sendToDevice(bytes);
    }

    @Override
    public void onScreenshotReq() {
        byte[] bytes = gbDeviceProtocol.encodeScreenshotReq();
        sendToDevice(bytes);
    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
        byte[] bytes = gbDeviceProtocol.encodeEnableRealtimeSteps(enable);
        sendToDevice(bytes);
    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {
        byte[] bytes = gbDeviceProtocol.encodeEnableHeartRateSleepSupport(enable);
        sendToDevice(bytes);
    }
}

package ru.l240.miband.gadgetbridge.devices;

import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.UUID;

import ru.l240.miband.gadgetbridge.model.Alarm;
import ru.l240.miband.gadgetbridge.model.NotificationSpec;
import ru.l240.miband.gadgetbridge.model.ServiceCommand;

public interface EventHandler {
    void onNotification(NotificationSpec notificationSpec);

    void onSetTime();

    void onSetAlarms(ArrayList<? extends Alarm> alarms);

    void onSetCallState(@Nullable String number, @Nullable String name, ServiceCommand command);

    void onSetMusicInfo(String artist, String album, String track, int duration, int trackCount, int trackNr);

    void onEnableRealtimeSteps(boolean enable);

    void onInstallApp(Uri uri);

    void onAppInfoReq();

    void onAppStart(UUID uuid, boolean start);

    void onAppDelete(UUID uuid);

    void onAppConfiguration(UUID appUuid, String config);

    void onFetchActivityData();

    void onReboot();

    void onHeartRateTest();

    void onFindDevice(boolean start);

    void onScreenshotReq();

    void onEnableHeartRateSleepSupport(boolean enable);
}

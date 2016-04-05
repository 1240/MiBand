package ru.l240.miband.gadgetbridge.model;

public interface SummaryOfDay {
    byte getProvider();

    int getSteps();

    int getDayStartWakeupTime();

    int getDayEndFallAsleepTime();

}

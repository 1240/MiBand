package ru.l240.miband;

/**
 * Created by l24o on 21.03.16.
 */
public abstract class BleCallback {

    protected abstract void callback(String data);

    protected abstract void callbackHR(String data);

}

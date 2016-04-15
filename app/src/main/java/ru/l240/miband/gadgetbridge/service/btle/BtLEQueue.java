package ru.l240.miband.gadgetbridge.service.btle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import ru.l240.miband.MiApplication;
import ru.l240.miband.gadgetbridge.impl.GBDevice;


/**
 * One queue/thread per connectable device.
 */
public final class BtLEQueue {
    public static final String TAG = BtLEQueue.class.getSimpleName();
    private static final long MIN_MILLIS_BEFORE_RECONNECT = 1000 * 60 * 5; // 5 minutes
    private final Object mGattMonitor = new Object();
    private final GBDevice mGbDevice;
    private final BluetoothAdapter mBluetoothAdapter;
    private final BlockingQueue<Transaction> mTransactions = new LinkedBlockingQueue<>();
    private final Context mContext;
    private final InternalGattCallback internalGattCallback;
    private BluetoothGatt mBluetoothGatt;
    /**
     * When an automatic reconnect was attempted after a connection breakdown (error)
     */
    private long lastReconnectTime = System.currentTimeMillis();
    private volatile boolean mDisposed;
    private volatile boolean mCrashed;
    private volatile boolean mAbortTransaction;
    private CountDownLatch mWaitForActionResultLatch;
    private CountDownLatch mConnectionLatch;
    private BluetoothGattCharacteristic mWaitCharacteristic;
    private Thread dispatchThread = new Thread("GadgetBridge GATT Dispatcher") {

        @Override
        public void run() {
            Log.d(TAG, "Queue Dispatch Thread started.");

            while (!mDisposed && !mCrashed) {
                try {
                    Transaction transaction = mTransactions.take();

                    /*if (!isConnected()) {
                        Log.d(TAG, "not connected, waiting for connection...");
                        // TODO: request connection and initialization from the outside and wait until finished
                        internalGattCallback.reset();

                        // wait until the connection succeeds before running the actions
                        // Note that no automatic connection is performed. This has to be triggered
                        // on the outside typically by the DeviceSupport. The reason is that
                        // devices have different kinds of initializations and this class has no
                        // idea about them.
                        mConnectionLatch = new CountDownLatch(1);
                        mConnectionLatch.await();
                        mConnectionLatch = null;
                    }*/

                    internalGattCallback.setTransactionGattCallback(transaction.getGattCallback());
                    mAbortTransaction = false;
                    // Run all actions of the transaction until one doesn't succeed
                    for (BtLEAction action : transaction.getActions()) {
                        if (mAbortTransaction) { // got disconnected
                            Log.d(TAG, "Aborting running transaction");
                            break;
                        }
                        mWaitCharacteristic = action.getCharacteristic();
                        mWaitForActionResultLatch = new CountDownLatch(1);

                        Log.d(TAG, "About to run action: " + action);

                        if (action.run(mBluetoothGatt)) {
                            // check again, maybe due to some condition, action did not need to write, so we can't wait
                            boolean waitForResult = action.expectsResult();
                            if (waitForResult) {
                                mWaitForActionResultLatch.await();
                                mWaitForActionResultLatch = null;
                                if (mAbortTransaction) {
                                    break;
                                }
                            }
                        } else {
                            Log.d(TAG, "Action returned false: " + action);
                            break; // abort the transaction
                        }
                    }
                } catch (InterruptedException ignored) {
                    mConnectionLatch = null;
                    Log.d(TAG, "Thread interrupted");
                } catch (Throwable ex) {
                    Log.d(TAG, "Queue Dispatch Thread died: " + ex.getMessage(), ex);
                    mCrashed = true;
                    mConnectionLatch = null;
                } finally {
                    mWaitForActionResultLatch = null;
                    mWaitCharacteristic = null;
                }
            }
            Log.d(TAG, "Queue Dispatch Thread terminated.");
        }
    };

    public BtLEQueue(BluetoothAdapter bluetoothAdapter, GBDevice gbDevice, GattCallback externalGattCallback, Context context) {
        mBluetoothAdapter = bluetoothAdapter;
        mGbDevice = gbDevice;
        internalGattCallback = new InternalGattCallback(externalGattCallback);
        mContext = context;

        dispatchThread.start();
    }

    protected boolean isConnected() {
        return mGbDevice.isConnected();
    }

    public boolean connect() {
        if (isConnected()) {
            Log.d(TAG, "Ingoring connect() because already connected.");
            return false;
        }
        synchronized (mGattMonitor) {
            if (mBluetoothGatt != null) {
                // Tribal knowledge says you're better off not reusing existing BlueoothGatt connections,
                // so create a new one.
                Log.d(TAG, "connect() requested -- disconnecting previous connection: " + mGbDevice.getName());
                disconnect();
            }
        }
        Log.d(TAG, "Attempting to connect to " + mGbDevice.getName());
        mBluetoothAdapter.cancelDiscovery();
        BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(mGbDevice.getAddress());
        synchronized (mGattMonitor) {
            mBluetoothGatt = remoteDevice.connectGatt(mContext, true, internalGattCallback);
            mBluetoothGatt.connect();
        }
        boolean result = mBluetoothGatt != null;
        if (result) {
            setDeviceConnectionState(GBDevice.State.CONNECTING);
        }
        return result;
    }

    private void setDeviceConnectionState(GBDevice.State newState) {
        Log.d(TAG, "new device connection state: " + newState);
        mGbDevice.setState(newState);
        mGbDevice.sendDeviceUpdateIntent(mContext);
        if (mConnectionLatch != null && newState == GBDevice.State.CONNECTED) {
            mConnectionLatch.countDown();
        }
    }

    public void disconnect() {
        synchronized (mGattMonitor) {
            Log.d(TAG, "disconnect()");
            BluetoothGatt gatt = mBluetoothGatt;
            if (gatt != null) {
                mBluetoothGatt = null;
                Log.d(TAG, "Disconnecting BtLEQueue from GATT device");
                gatt.disconnect();
                gatt.close();
                setDeviceConnectionState(GBDevice.State.NOT_CONNECTED);
            }
        }
    }

    private void handleDisconnected(int status) {
        Log.d(TAG, "handleDisconnected: " + status);
        internalGattCallback.reset();
        mTransactions.clear();
        mAbortTransaction = true;
        if (mWaitForActionResultLatch != null) {
            mWaitForActionResultLatch.countDown();
        }
        setDeviceConnectionState(GBDevice.State.NOT_CONNECTED);

        // either we've been disconnected because the device is out of range
        // or because of an explicit @{link #disconnect())
        // To support automatic reconnection, we keep the mBluetoothGatt instance
        // alive (we do not close() it). Unfortunately we sometimes have problems
        // reconnecting automatically, so we try to fix this by re-creating mBluetoothGatt.
        // Not sure if this actually works without re-initializing the device...
        if (status != 0) {
            if (!maybeReconnect()) {
                disconnect(); // ensure that we start over cleanly next time
            }
        }
    }

    /**
     * Depending on certain criteria, connects to the BluetoothGatt.
     *
     * @return true if a reconnection attempt was made, or false otherwise
     */
    private boolean maybeReconnect() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastReconnectTime >= MIN_MILLIS_BEFORE_RECONNECT) {
            Log.d(TAG, "Automatic reconnection attempt...");
            lastReconnectTime = currentTime;
            return connect();
        }
        return false;
    }

    public void dispose() {
        if (mDisposed) {
            return;
        }
        mDisposed = true;
//        try {
        disconnect();
        dispatchThread.interrupt();
        dispatchThread = null;
//            dispatchThread.join();
//        } catch (InterruptedException ex) {
//            Log.d(TAG,"Exception while disposing BtLEQueue", ex);
//        }
    }

    /**
     * Adds a transaction to the end of the queue.
     *
     * @param transaction
     */
    public void add(Transaction transaction) {
        Log.d(TAG, "about to add: " + transaction);
        if (!transaction.isEmpty()) {
            mTransactions.add(transaction);
        }
    }

    public void clear() {
        mTransactions.clear();
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothGatt is null => no services available.");
            return Collections.emptyList();
        }
        return mBluetoothGatt.getServices();
    }

    private boolean checkCorrectGattInstance(BluetoothGatt gatt, String where) {
        if (gatt != mBluetoothGatt && mBluetoothGatt != null) {
            Log.d(TAG, "Ignoring event from wrong BluetoothGatt instance: " + where + "; " + gatt);
            return false;
        }
        return true;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final class InternalGattCallback extends BluetoothGattCallback {
        private final GattCallback mExternalGattCallback;
        private
        @Nullable
        GattCallback mTransactionGattCallback;

        public InternalGattCallback(GattCallback externalGattCallback) {
            mExternalGattCallback = externalGattCallback;
        }

        public void setTransactionGattCallback(@Nullable GattCallback callback) {
            mTransactionGattCallback = callback;
        }

        private GattCallback getCallbackToUse() {
            if (mTransactionGattCallback != null) {
                return mTransactionGattCallback;
            }
            return mExternalGattCallback;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "connection state change, newState: " + newState + getStatusString(status));

            if (!checkCorrectGattInstance(gatt, "connection state event")) {
                return;
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "connection state event with error status " + status);
            }

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(TAG, "Connected to GATT server.");
                    setDeviceConnectionState(GBDevice.State.CONNECTED);
                    // Attempts to discover services after successful connection.
                    Log.d(TAG, "Attempting to start service discovery:" +
                            gatt.discoverServices());
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d(TAG, "Disconnected from GATT server.");
//                    handleDisconnected(status);
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    Log.d(TAG, "Connecting to GATT server...");
                    setDeviceConnectionState(GBDevice.State.CONNECTING);
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (!checkCorrectGattInstance(gatt, "services discovered: " + getStatusString(status))) {
                return;
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (getCallbackToUse() != null) {
                    // only propagate the successful event
                    getCallbackToUse().onServicesDiscovered(gatt);
                }
            } else {
                Log.d(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "characteristic write: " + characteristic.getUuid() + getStatusString(status));
            if (!checkCorrectGattInstance(gatt, "characteristic write")) {
                return;
            }
            if (getCallbackToUse() != null) {
                getCallbackToUse().onCharacteristicWrite(gatt, characteristic, status);
            }
            checkWaitingCharacteristic(characteristic, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "characteristic read: " + characteristic.getUuid() + getStatusString(status));
            if (!checkCorrectGattInstance(gatt, "characteristic read")) {
                return;
            }
            if (getCallbackToUse() != null) {
                try {
                    getCallbackToUse().onCharacteristicRead(gatt, characteristic, status);
                } catch (Throwable ex) {
                    Log.d(TAG, "onCharacteristicRead: " + ex.getMessage(), ex);
                }
            }
            checkWaitingCharacteristic(characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "descriptor read: " + descriptor.getUuid() + getStatusString(status));
            if (!checkCorrectGattInstance(gatt, "descriptor read")) {
                return;
            }
            if (getCallbackToUse() != null) {
                try {
                    getCallbackToUse().onDescriptorRead(gatt, descriptor, status);
                } catch (Throwable ex) {
                    Log.d(TAG, "onDescriptorRead: " + ex.getMessage(), ex);
                }
            }
            checkWaitingCharacteristic(descriptor.getCharacteristic(), status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "descriptor write: " + descriptor.getUuid() + getStatusString(status));
            if (!checkCorrectGattInstance(gatt, "descriptor write")) {
                return;
            }
            if (getCallbackToUse() != null) {
                try {
                    getCallbackToUse().onDescriptorWrite(gatt, descriptor, status);
                } catch (Throwable ex) {
                    Log.d(TAG, "onDescriptorWrite: " + ex.getMessage(), ex);
                }
            }
            checkWaitingCharacteristic(descriptor.getCharacteristic(), status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            String content = "";
            for (byte b : characteristic.getValue()) {
                content += String.format(" 0x%1x", b);
            }
            Log.d(TAG, "characteristic changed: " + characteristic.getUuid() + " value: " + content);

            if (!checkCorrectGattInstance(gatt, "characteristic changed")) {
                return;
            }
            if (getCallbackToUse() != null) {
                try {
                    getCallbackToUse().onCharacteristicChanged(gatt, characteristic);
                } catch (Throwable ex) {
                    Log.d(TAG, "onCharaceristicChanged: " + ex.getMessage(), ex);
                }
            } else {
                Log.d(TAG, "No gattcallback registered, ignoring characteristic change");
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "remote rssi: " + rssi + getStatusString(status));
            if (!checkCorrectGattInstance(gatt, "remote rssi")) {
                return;
            }
            if (getCallbackToUse() != null) {
                try {
                    getCallbackToUse().onReadRemoteRssi(gatt, rssi, status);
                } catch (Throwable ex) {
                    Log.d(TAG, "onReadRemoteRssi: " + ex.getMessage(), ex);
                }
            }
        }

        private void checkWaitingCharacteristic(BluetoothGattCharacteristic characteristic, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "failed btle action, aborting transaction: " + characteristic.getUuid() + getStatusString(status));
                mAbortTransaction = true;
            }
            if (characteristic != null && BtLEQueue.this.mWaitCharacteristic != null && characteristic.getUuid().equals(BtLEQueue.this.mWaitCharacteristic.getUuid())) {
                if (mWaitForActionResultLatch != null) {
                    mWaitForActionResultLatch.countDown();
                }
            } else {
                if (BtLEQueue.this.mWaitCharacteristic != null) {
                    Log.d(TAG, "checkWaitingCharacteristic: mismatched characteristic received: " + ((characteristic != null && characteristic.getUuid() != null) ? characteristic.getUuid().toString() : "(null)"));
                }
            }
        }

        private String getStatusString(int status) {
            return status == BluetoothGatt.GATT_SUCCESS ? " (success)" : " (failed: " + status + ")";
        }

        public void reset() {
            Log.d(TAG, "internal gatt callback set to null");
            //mTransactionGattCallback = null;
        }
    }
}

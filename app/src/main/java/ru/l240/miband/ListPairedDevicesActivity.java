package ru.l240.miband;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ru.l240.miband.gadgetbridge.impl.GBDevice;
import ru.l240.miband.gadgetbridge.model.DeviceType;
import ru.l240.miband.gadgetbridge.service.DeviceSupport;
import ru.l240.miband.gadgetbridge.service.DeviceSupportFactory;
import ru.l240.miband.utils.NotificationUtils;
import ru.l240.miband.utils.PrefUtils;

public class ListPairedDevicesActivity extends ListActivity {

    private List<Object> pDevices;
    private ProgressDialog dialog;
    private DeviceSupport deviceSupport;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GBDevice.ACTION_DEVICE_CHANGED)) {
                dialog.setMessage(deviceSupport.getDevice().getStateString());
                if (deviceSupport.isConnected()) {
                    dialog.dismiss();
                    DialogFragment dialogFragment = new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Select this device?")
                                    .setMessage("After selecting device, the timer will be set to scan pulse and the application will close.")
                                    .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            PrefUtils.saveAddress(getApplicationContext(), deviceSupport.getDevice().getAddress());
                                            NotificationUtils.getInstance(getApplicationContext()).createAlarmNotify(new Date(), NotificationUtils.MIN_1);
                                            unregisterReceiver(mReceiver);
                                            ListPairedDevicesActivity.this.finish();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //
                                        }
                                    });
                            return builder.create();
                        }
                    };
                    dialogFragment.show(getFragmentManager(), "DialogFragment");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (adapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ArrayAdapter<String> btArrayAdapter
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);

        BluetoothAdapter bluetoothAdapter
                = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices
                = bluetoothAdapter.getBondedDevices();
        pDevices = Arrays.asList(pairedDevices.toArray());
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceBTName = device.getName();
                String deviceBTMajorClass
                        = getBTMajorDeviceClass(device
                        .getBluetoothClass()
                        .getMajorDeviceClass());
                btArrayAdapter.add(deviceBTName + "\n"
                        + deviceBTMajorClass);
            }
        }
        setListAdapter(btArrayAdapter);

    }

    private String getBTMajorDeviceClass(int major) {
        switch (major) {
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return "AUDIO_VIDEO";
            case BluetoothClass.Device.Major.COMPUTER:
                return "COMPUTER";
            case BluetoothClass.Device.Major.HEALTH:
                return "HEALTH";
            case BluetoothClass.Device.Major.IMAGING:
                return "IMAGING";
            case BluetoothClass.Device.Major.MISC:
                return "MISC";
            case BluetoothClass.Device.Major.NETWORKING:
                return "NETWORKING";
            case BluetoothClass.Device.Major.PERIPHERAL:
                return "PERIPHERAL";
            case BluetoothClass.Device.Major.PHONE:
                return "PHONE";
            case BluetoothClass.Device.Major.TOY:
                return "TOY";
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                return "UNCATEGORIZED";
            case BluetoothClass.Device.Major.WEARABLE:
                return "AUDIO_VIDEO";
            default:
                return "unknown!";
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        String address = ((BluetoothDevice) pDevices.get(position)).getAddress();
        dialog = new ProgressDialog(ListPairedDevicesActivity.this);
        dialog.setTitle("Try to get pulse...");
        dialog.setMessage("Initialize");
        dialog.show();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(GBDevice.ACTION_DEVICE_CHANGED));
        GBDevice mi = new GBDevice(address, "MI", DeviceType.MIBAND);
        try {
            deviceSupport = new DeviceSupportFactory(this).createDeviceSupport(mi);
            deviceSupport.connect();
        } catch (GBException e) {
            e.printStackTrace();
        }

        /*DialogFragment dialogFragment = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select this device?")
                        .setMessage("After selecting device, the timer will be set to scan pulse and the application will close.")
                        .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PrefUtils.saveAddress(getApplicationContext(), ((BluetoothDevice) pDevices.get(position)).getAddress());
                                NotificationUtils.getInstance(getApplicationContext()).createAlarmNotify(new Date(), NotificationUtils.MIN_1);
                                ListPairedDevicesActivity.this.finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //
                            }
                        });
                return builder.create();
            }
        };
        dialogFragment.show(getFragmentManager(), "DialogFragment");*/
    }
}

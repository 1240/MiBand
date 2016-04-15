package ru.l240.miband;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

import io.realm.Realm;
import ru.l240.miband.models.Profile;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.utils.NotificationUtils;
import ru.l240.miband.utils.PrefUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar pb;
    private TextView tv;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver receiver;
    private BleSingleton bleSingleton;
    private BleCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        bleSingleton = BleSingleton.getInstance(this);

        if (RealmHelper.getAll(Realm.getInstance(this), Profile.class).isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
//        PrefUtils.saveAddress(getApplicationContext(), "C8:0F:10:32:11:17");
        if (PrefUtils.getAddress(getApplicationContext()).isEmpty()) {
            Intent intent = new Intent(this, ListPairedDevicesActivity.class);
            startActivity(intent);
            return;
        }
//        BluetoothManager mBluetoothManager = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE));
//        mBluetoothAdapter = mBluetoothManager.getAdapter();
//        if (mBluetoothAdapter == null) {
//            tv.setText(R.string.not_support);
//        } else {
//            if (!mBluetoothAdapter.isEnabled()) {
//                receiver = new BroadcastReceiver() {
//                    @Override
//                    public void onReceive(Context context, Intent intent) {
//                        if (mBluetoothAdapter.isEnabled()) {
//                            tv.setText(R.string.finding_ble);
//                        } else {
//                            tv.setText(R.string.psl_ble_on);
//                        }
//                    }
//                };
//                this.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
//                tv.setText(R.string.psl_ble_on);
//            } else {
//                bleSingleton.init();
//            }
//        }
//        pb = (ProgressBar) findViewById(R.id.progressBar);
//        tv = (TextView) findViewById(R.id.tvMainActivitySearch);
//        callback = new BleCallback() {
//            @Override
//            public void callback(final String data) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        tv.setText(data);
//                        pb.setVisibility(View.INVISIBLE);
//                    }
//                });
//
//            }
//
//            @Override
//            protected void callbackHR(final String data) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        tv.setText(data);
//                        pb.setVisibility(View.INVISIBLE);
//                    }
//                });
//            }
//        };
//        bleSingleton.setCallback(callback);
//          MiApplication.deviceService().onHeartRateTest();
    }


    @Override
    protected void onResume() {
        super.onResume();
//        bleSingleton.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        bleSingleton.disconnect();
//        if (receiver != null)
//            unregisterReceiver(receiver);
    }

    public void refresh(View view) {
        NotificationUtils.getInstance(this).cancelAllAlarmNotify();
        NotificationUtils.getInstance(this).cancelAllLocation();
        NotificationUtils.getInstance(this).createAlarmNotify(new Date(), NotificationUtils.MIN_1);
        NotificationUtils.getInstance(this).createLocationService(new Date(), NotificationUtils.MIN_5);
        Snackbar snackbar = Snackbar.make(view, "The timer is set successfully", Snackbar.LENGTH_SHORT);
        snackbar.show();

    }

    public void exit(View view) {
        DialogFragment dialogFragment = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Logout")
                        .setMessage("All you local data will be cleaned. And you can select different device. Are you sure to logout?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RealmHelper.clearAll(Realm.getInstance(getApplicationContext()));
                                PrefUtils.saveAddress(getApplicationContext(), "");
                                NotificationUtils notificationUtils = NotificationUtils.getInstance(MainActivity.this);
                                notificationUtils.cancelAllAlarmNotify();
                                notificationUtils.cancelAllLocation();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        });
                return builder.create();
            }
        };
        dialogFragment.show(getSupportFragmentManager(), "exitDF");

    }
/*
    private void update() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb.setVisibility(View.INVISIBLE);
            }
        });
    }*/

}
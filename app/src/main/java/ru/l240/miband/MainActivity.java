package ru.l240.miband;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

import io.realm.Realm;
import ru.l240.miband.models.Profile;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.utils.DateUtils;
import ru.l240.miband.utils.NotificationUtils;
import ru.l240.miband.utils.PrefUtils;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
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
        if (PrefUtils.getAddress(getApplicationContext()).isEmpty()) {
            Intent intent = new Intent(this, DeviceScanActivity.class);
            startActivity(intent);
            return;
        }
        tv = (TextView) findViewById(R.id.tvMainActivitySearch);
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
        tv.setText("Таймер установлен.");
        NotificationUtils.getInstance(this).cancelAllAlarmNotify();
        NotificationUtils.getInstance(this).createAlarmNotify(DateUtils.addSeconds(new Date(), 5), NotificationUtils.MIN_5);
        /*try {
            bleSingleton.measure();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
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
package ru.l240.miband;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.l240.miband.utils.PrefUtils;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar pb;
    private TextView tv;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        tv = (TextView) findViewById(R.id.tvMainActivitySearch);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // делаем повеселее
        mSwipeRefreshLayout.setColorScheme(R.color.s1, R.color.s2, R.color.s3, R.color.s4);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (device != null && device.getName().contains("MI1S")) {
            System.out.println(device.getAddress());
            tv.setText("Браслет найден, синхронизируюсь...");
            PrefUtils.saveAddress(MainActivity.this, device.getAddress());

        }
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "refreshing");
    }
}

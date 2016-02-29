package ru.l240.miband;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {

    private ProgressBar pb;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        tv = (TextView) findViewById(R.id.tvMainActivitySearch);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (device != null && device.getName().contains("MI")) {
            System.out.println(device.getAddress());
            scanLeDevice(false);
            Intent intent = new Intent(getApplicationContext(), MiOverviewActivity.class);
            intent.putExtra("address", );
            startActivity(intent);
        }
    }
}

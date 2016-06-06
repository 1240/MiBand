package ru.l240.miband;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONObject;

import java.util.Date;

import io.realm.Realm;
import retrofit2.Call;
import ru.l240.miband.gadgetbridge.model.DeviceService;
import ru.l240.miband.gadgetbridge.service.devices.miband.MiBandSupport;
import ru.l240.miband.models.Profile;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.retrofit.ApiFac;
import ru.l240.miband.retrofit.ApiService;
import ru.l240.miband.retrofit.RetrofitCallback;
import ru.l240.miband.utils.MedUtils;
import ru.l240.miband.utils.NotificationUtils;
import ru.l240.miband.utils.PrefUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar pb;
    private TextView tv;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver receiver;
    private BroadcastReceiver receiverSteps;
    private BleSingleton bleSingleton;
    private BleCallback callback;
    private TextView tvS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (RealmHelper.getAll(Realm.getInstance(this), Profile.class).isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        RealmHelper.clearOldLog(Realm.getInstance(MainActivity.this));
    }


    @Override
    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
        if (!PrefUtils.getAddress(getApplicationContext()).isEmpty()) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String heartrate = intent.getStringExtra("heartrate");
                    View parentLayout = findViewById(R.id.rlMain);
                    Snackbar snackbar = Snackbar.make(parentLayout, String.format("Pulse: %s dpm", heartrate), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            };
            receiverSteps = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int STEPS = intent.getIntExtra(DeviceService.EXTRA_REALTIME_STEPS, 0);
                    tvS.setText(String.valueOf(STEPS));
                }
            };
            registerReceiver(receiver, new IntentFilter(MiBandSupport.HEART_RATE_ACTION));
            registerReceiver(receiverSteps, new IntentFilter(DeviceService.ACTION_REALTIME_STEPS));
        }

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiService service = ApiFac.getApiService();
                SharedPreferences preferences = getSharedPreferences(MedUtils.COOKIE_PREF, 0);
                String cookie = preferences.getString(MedUtils.COOKIE_PREF, "");
                Call<JSONObject> jsonObjectCall = service.doAlert(cookie);
                Snackbar snackbar = Snackbar.make(v, R.string.alert_sent, Snackbar.LENGTH_SHORT);
                snackbar.show();
                jsonObjectCall.enqueue(new RetrofitCallback<JSONObject>() {

                });
            }
        });
        Button button = (Button) findViewById(R.id.button);
        button.setBackgroundColor(getResources().getColor(PrefUtils.getAddress(getApplicationContext()).isEmpty() ? R.color.red : R.color.main_color));
        button.setText(PrefUtils.getAddress(getApplicationContext()).isEmpty() ? R.string.band_not_connected : R.string.refresh_timer);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_settings);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        TextView tvB = (TextView) findViewById(R.id.tvBattery);
        tvS = (TextView) findViewById(R.id.tvSteps);
        if (PrefUtils.getAddress(getApplicationContext()).isEmpty()) {
            tvB.setVisibility(View.GONE);
            tvS.setVisibility(View.GONE);
        } else {
            tvB.setText(String.valueOf(Prefs.getInt("BATTERY", 0)));
            tvS.setText(String.valueOf(Prefs.getInt("STEPS", 0)));
        }



    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (receiver != null)
                unregisterReceiver(receiver);
                unregisterReceiver(receiverSteps);
        } catch (Exception e) {
            //;
        }
    }

    public void refresh(View view) {
        if (PrefUtils.getAddress(getApplicationContext()).isEmpty()) {
            Snackbar snackbar = Snackbar.make(view, R.string.choose_band, Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }
        NotificationUtils.getInstance(this).cancelAllAlarmNotify();
        NotificationUtils.getInstance(this).cancelAllLocation();
        NotificationUtils.getInstance(this).createAlarmNotify(new Date(), NotificationUtils.MIN_1);
        NotificationUtils.getInstance(this).createLocationService(new Date(), 15);
        Snackbar snackbar = Snackbar.make(view, R.string.get_pulse_rate, Snackbar.LENGTH_SHORT);
        snackbar.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (!PrefUtils.getAddress(getApplicationContext()).isEmpty()) {
            menu.findItem(R.id.menu_devices).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent();
                i.setClass(MainActivity.this, SettingsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menu_devices:
                Intent intent = new Intent(getApplicationContext(), ListPairedDevicesActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;
import ru.l240.miband.gadgetbridge.service.devices.miband.MiBandSupport;
import ru.l240.miband.models.JournalItem;
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
    private BleSingleton bleSingleton;
    private BleCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (RealmHelper.getAll(Realm.getInstance(this), Profile.class).isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        if (PrefUtils.getAddress(getApplicationContext()).isEmpty()) {
            Intent intent = new Intent(this, ListPairedDevicesActivity.class);
            startActivity(intent);
            return;
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String heartrate = intent.getStringExtra("heartrate");
                View parentLayout = findViewById(R.id.rlMain);
                Snackbar snackbar = Snackbar.make(parentLayout, String.format("Pulse: %s dpm", heartrate), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        };
        registerReceiver(receiver, new IntentFilter(MiBandSupport.HEART_RATE_ACTION));
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiService service = ApiFac.getApiService();
                SharedPreferences preferences = getSharedPreferences(MedUtils.COOKIE_PREF, 0);
                String cookie = preferences.getString(MedUtils.COOKIE_PREF, "");
//                JournalItem item = new JournalItem();
//                item.setDate(new Date());
//                item.setMessage("Alert!");
//                Call<List<JournalItem>> journalRecords = service.postAddJournalRecords(Collections.singletonList(item), cookie);
                Call<JSONObject> jsonObjectCall = service.doAlert(cookie);
                Snackbar snackbar = Snackbar.make(v, "Alert sending successfully.", Snackbar.LENGTH_SHORT);
                snackbar.show();
                jsonObjectCall.enqueue(new RetrofitCallback<JSONObject>() {

                });
            }
        });
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_settings);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        RealmHelper.clearOldLog(Realm.getInstance(MainActivity.this));
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (receiver != null)
                unregisterReceiver(receiver);
        } catch (Exception e) {
            //;
        }
    }

    public void refresh(View view) {
        NotificationUtils.getInstance(this).cancelAllAlarmNotify();
        NotificationUtils.getInstance(this).cancelAllLocation();
        NotificationUtils.getInstance(this).createAlarmNotify(new Date(), NotificationUtils.MIN_1);
        NotificationUtils.getInstance(this).createLocationService(new Date(), 15);
        Snackbar snackbar = Snackbar.make(view, "The timer is set successfully", Snackbar.LENGTH_SHORT);
        snackbar.show();

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
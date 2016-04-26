package ru.l240.miband;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
                JournalItem item = new JournalItem();
                item.setDate(new Date());
                item.setMessage("Alert!");
                Call<List<JournalItem>> journalRecords = service.postAddJournalRecords(Collections.singletonList(item), cookie);
                Snackbar snackbar = Snackbar.make(v, "Alert sending successfully.", Snackbar.LENGTH_SHORT);
                snackbar.show();
                journalRecords.enqueue(new RetrofitCallback<List<JournalItem>>() {
                    @Override
                    public void onResponse(Call<List<JournalItem>> call, Response<List<JournalItem>> response) {
                        super.onResponse(call, response);
                    }

                    @Override
                    public void onFailure(Call<List<JournalItem>> call, Throwable t) {
                        super.onFailure(call, t);
                    }
                });
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null)
            unregisterReceiver(receiver);
    }

    public void refresh(View view) {
        NotificationUtils.getInstance(this).cancelAllAlarmNotify();
        NotificationUtils.getInstance(this).cancelAllLocation();
        NotificationUtils.getInstance(this).createAlarmNotify(new Date(), NotificationUtils.MIN_1);
        NotificationUtils.getInstance(this).createLocationService(new Date(), NotificationUtils.MIN_1);
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
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        });
                return builder.create();
            }
        };
        dialogFragment.show(getSupportFragmentManager(), "exitDF");

    }
}
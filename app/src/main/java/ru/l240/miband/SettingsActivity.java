package ru.l240.miband;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import ru.l240.miband.models.Log;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.utils.DateUtils;
import ru.l240.miband.utils.DividerItemDecoration;
import ru.l240.miband.utils.PrefUtils;

public class SettingsActivity extends AppCompatActivity {

    public static final String TAG = SettingsActivity.class.getSimpleName();
    private RVAdapter adapter;
    private BroadcastReceiver mReceiver;
    private LinearLayoutManager layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        RecyclerView lvLog = (RecyclerView) findViewById(R.id.lvLog);
        lvLog.addItemDecoration(new DividerItemDecoration(SettingsActivity.this, DividerItemDecoration.VERTICAL_LIST));
        List<Log> all = RealmHelper.getAll(Realm.getInstance(SettingsActivity.this), Log.class);
        adapter = new RVAdapter(all);
        lvLog.setAdapter(adapter);
        lvLog.setHasFixedSize(false);
        layout = new LinearLayoutManager(getApplicationContext());
        layout.setReverseLayout(true);
        layout.setStackFromEnd(true);
        lvLog.setLayoutManager(layout);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String logText = intent.getExtras().getString("logText");
                Log log = new Log();
                log.setText(logText);
                log.setDate(new Date());
                adapter.addItem(log);

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(SettingsActivity.TAG));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("ValidFragment")
    public void exit(View view) {
        DialogFragment dialogFragment = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this)
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.VHolder> {

        private final List<Log> items;

        public RVAdapter(List<Log> items) {
            this.items = new ArrayList<>(items);
        }

        @Override
        public VHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);
            return new VHolder(v);
        }

        @Override
        public void onBindViewHolder(VHolder holder, int position) {
            Log item = items.get(position);
            holder.text.setText(item.getText());
            holder.time.setText(DateUtils.formatDateTime(item.getDate()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void addItem(Log item) {
            items.add(item);
            notifyDataSetChanged();
            layout.scrollToPosition(items.size() - 1);
        }

        public class VHolder extends RecyclerView.ViewHolder {

            private TextView text;
            private TextView time;

            public VHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.tvText);
                time = (TextView) itemView.findViewById(R.id.tvtime);

            }
        }
    }
}

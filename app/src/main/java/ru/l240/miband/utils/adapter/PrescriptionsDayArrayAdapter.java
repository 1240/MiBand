package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.SwitchCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit.Call;
import retrofit.Response;
import ru.fors.remsmed.R;
import ru.fors.remsmed.core.MedContract;
import ru.fors.remsmed.core.dto.MedDTO;
import ru.fors.remsmed.core.dto.prescriptions.PrescrExecution;
import ru.fors.remsmed.core.dto.prescriptions.PrescriptionItem;
import ru.fors.remsmed.core.retrofitloaders.api.ApiFactory;
import ru.fors.remsmed.core.retrofitloaders.api.MedRetrofitService;
import ru.fors.remsmed.core.retrofitloaders.api.RetrofitCallback;
import ru.fors.remsmed.db.DBHelper;
import ru.fors.remsmed.db.RequestTaskReauthorize;
import ru.fors.remsmed.utils.MedUtils;
import ru.fors.remsmed.utils.NotificationUtils;

/**
 * @author Alexander Popov on 20.04.15.
 */
public class PrescriptionsDayArrayAdapter extends MedArrayAdapter {

    private final List<PrescriptionItem> values;
    private final Date date;
    private final BadgeView badgeViewActive;
    private final BadgeView badgeView;
    private Boolean isNoTime;
    private Long execId;

    public PrescriptionsDayArrayAdapter(Context context, List<PrescriptionItem> values, Date date, BadgeView badgeViewActive, BadgeView badgeView) {
        super(context, R.layout.prescriprions_day_listitem, values);
        this.values = values;
        this.date = date;
        this.badgeView = badgeView;
        this.badgeViewActive = badgeViewActive;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.prescriprions_day_listitem, parent, false);
        final PrescriptionItem item = values.get(position);
        Boolean labelNoTime = item.getDescription().contains(getContext().getString(R.string.no_time_prescriptions));
        isNoTime = item.getTemplate() == null;
        TextView message = (TextView) rowView.findViewById(R.id.tvPrescriptionsDayMessage);
        TextView time = (TextView) rowView.findViewById(R.id.tvPrescriptionsDayTime);
        final SwitchCompat aSwitch = (SwitchCompat) rowView.findViewById(R.id.switchPrescriptionsDay);
        message.setText(item.getDescription());

        aSwitch.setOnClickListener(v -> aSwitch.setClickable(false));
        if (labelNoTime) {
            time.setVisibility(View.GONE);
            message.setGravity(Gravity.CENTER_HORIZONTAL);
            rowView.setPadding(10, 5, 10, 5);
            aSwitch.setVisibility(View.GONE);
            rowView.setBackgroundColor(getContext().getResources().getColor(R.color.datebar_background_color_));
            return rowView;
        }
        if (!isNoTime) {
            time.setText(item.getTemplate().getHour() + ":00");
        } else {
            time.setVisibility(View.GONE);
        }
        final DBHelper dbHelper = new DBHelper(getContext());
        final PrescrExecution execution = isNoTime ?
                new PrescrExecution().fromCursorOne(getContext().getContentResolver()
                        .query(MedContract.PrescrExecution.CONTENT_URI,
                                MedContract.PrescrExecution.DEFAULT_PROJECTION,
                                MedContract.PrescrExecution.KEY_EXEC_PRESCRIPTION_ID + "= ?"
                                        + " AND (" + MedContract.PrescrExecution.KEY_EXEC_PLAN_DATE + " BETWEEN '" + new MedUtils().dfDB().format(date)
                                        + " 00:00:00' AND '" + new MedUtils().dfDB().format(date) + " 99:99:99')",
                                new String[]{item.getId().toString()},
                                MedContract.PrescrExecution.DEFAULT_SORT))
                : dbHelper.getAllPrescrExecutionsByPrescId(item.getTemplate().getPrescrId(), date, item.getTemplate().getHour());


        try {
            final Date execDate = isNoTime ? date : new SimpleDateFormat("dd.MM.yyyy HH").parse(new SimpleDateFormat("dd.MM.yyyy ").format(date) + item.getTemplate().getHour());
            if (execution != null) {
                aSwitch.setChecked(true);
                aSwitch.setEnabled(true);
            }
            final View finalRowView = rowView;
            aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isNoTime = item.getTemplate() == null;
                    if (isChecked) {
                        final PrescrExecution execution = new PrescrExecution();
                        execution.setPlanExecDate(execDate);
                        execution.setRealExecDate(new Date());
                        execution.setPrescriptionItem(new PrescriptionItem());
                        execution.setCaseTherapyId(isNoTime ? item.getId() : item.getTemplate().getPrescrId());
                        execution.setExecHour(isNoTime ? null : item.getTemplate().getHour());
                        execution.setIsDeleted(false);
                        if (MedUtils.isNetworkConnected(getContext())) {
                            MedRetrofitService service = ApiFactory.getMedService();
                            SharedPreferences preferences = getContext().getSharedPreferences(MedUtils.COOKIE_PREF, 0);
                            String cookie = preferences.getString(MedUtils.COOKIE_PREF, "");
                            Call<List<MedDTO>> listCall = service.postDoPrescriptionExecutions(Collections.singletonList(execution), cookie);
                            listCall.enqueue(new RetrofitCallback<List<MedDTO>>() {
                                @Override
                                public void onResponse(Response<List<MedDTO>> response) {
                                    if (response.isSuccess()) {
                                        for (MedDTO medO : response.body()) {
                                            execution.setId(medO.getId());
                                            getContext().getContentResolver()
                                                    .insert(MedContract.PrescrExecution.CONTENT_URI,
                                                            execution.toContentValues());
                                        }
                                    } else {
                                        aSwitch.setChecked(false);
                                        RequestTaskReauthorize requestTaskReauthorize = new RequestTaskReauthorize(getContext());
                                        requestTaskReauthorize.execute();
                                    }
                                    aSwitch.setClickable(true);
                                    int badgeText = MedUtils.precriptionBadge(getContext());
                                    badgeView.setText(String.valueOf(badgeText));
                                    badgeViewActive.setText(String.valueOf(badgeText));
                                    badgeViewActive.show();
                                    super.onResponse(response);
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    super.onFailure(t);
                                    aSwitch.setChecked(false);
                                    aSwitch.setClickable(true);
                                }
                            });

                        } else {
                            execution.setId(new MedDTO<PrescrExecution>().getSyncId(MedContract.PrescrExecution.CONTENT_URI,
                                    getContext()));
                            getContext().getContentResolver().insert(MedContract.PrescrExecution.CONTENT_URI,
                                    execution.toContentValues());
                            SharedPreferences preferences = getContext().getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(MedUtils.SCHEDULER_EXEC_PREF, "need");
                            editor.commit();
                            int badgeText = MedUtils.precriptionBadge(getContext());
                            badgeView.setText(String.valueOf(badgeText));
                            badgeViewActive.setText(String.valueOf(badgeText));
                            badgeViewActive.show();
                        }
                        ((TextView) finalRowView.findViewById(R.id.tvPrescriptionsDayMessage))
                                .setTextColor(getContext().getResources().getColor(R.color.system_font));
                    } else {
                        PrescrExecution newExecution;
                        if (execution == null) {
                            newExecution = dbHelper.getPrescrExecutionsById(execId);
                        } else {
                            newExecution = execution;
                        }
                        if (newExecution == null) {
                            return;
                        }
                        if (newExecution.getId() != null && newExecution.getId() < 0) { //todo
                            dbHelper.deletePrescrExecutionById(newExecution.getId());
                            return;
                        } else if (newExecution.getId() == null) {
                            return;
                        }
                        if (MedUtils.isNetworkConnected(getContext())) {
                            MedRetrofitService service = ApiFactory.getMedService();
                            SharedPreferences preferences = getContext().getSharedPreferences(MedUtils.COOKIE_PREF, 0);
                            String cookie = preferences.getString(MedUtils.COOKIE_PREF, "");
                            Call<List<MedDTO>> listCall = service.postUndoPrescriptionExecutions(Collections.singletonList(new MedDTO(newExecution.getId())), cookie);
                            listCall.enqueue(new RetrofitCallback<List<MedDTO>>() {

                                @Override
                                public void onFailure(Throwable t) {
                                    super.onFailure(t);
                                    aSwitch.setChecked(true);
                                    aSwitch.setClickable(true);
                                }

                                @Override
                                public void onResponse(Response<List<MedDTO>> response) {
                                    if (response.isSuccess()) {
                                        for (MedDTO medO : response.body()) {
                                            getContext().getContentResolver()
                                                    .delete(MedContract.PrescrExecution.CONTENT_URI,
                                                            MedContract.PrescrExecution._ID + " = ?",
                                                            new String[]{medO.getId().toString()});
                                        }
                                    } else {
                                        RequestTaskReauthorize requestTaskReauthorize = new RequestTaskReauthorize(getContext());
                                        requestTaskReauthorize.execute();
                                        aSwitch.setChecked(true);
                                    }
                                    aSwitch.setClickable(true);
                                    int badgeText = MedUtils.precriptionBadge(getContext());
                                    badgeView.setText(String.valueOf(badgeText));
                                    badgeViewActive.setText(String.valueOf(badgeText));
                                    badgeViewActive.show();
                                    super.onResponse(response);
                                }
                            });
                        } else {
                            newExecution.setIsDeleted(true);
                            getContext().getContentResolver().update(MedContract.PrescrExecution.CONTENT_URI,
                                    newExecution.toContentValues(),
                                    MedContract.PrescrExecution._ID + " = ?",
                                    new String[]{newExecution.getId().toString()});
                            SharedPreferences preferences = getContext().getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(MedUtils.SCHEDULER_EXEC_DELETE_PREF, "need");
                            editor.commit();
                            int badgeText = MedUtils.precriptionBadge(getContext());
                            badgeView.setText(String.valueOf(badgeText));
                            badgeViewActive.setText(String.valueOf(badgeText));
                            badgeViewActive.show();
                        }
                        if (isNoTime) {
                            if (execDate.before(new Date())) {
                                ((TextView) finalRowView.findViewById(R.id.tvPrescriptionsDayMessage))
                                        .setTextColor(getContext().getResources().getColor(R.color.out_of_date));
                            } else {
                                ((TextView) finalRowView.findViewById(R.id.tvPrescriptionsDayMessage))
                                        .setTextColor(getContext().getResources().getColor(R.color.system_font));
                            }
                        }

                    }
                    NotificationUtils utils = NotificationUtils.getInstance(getContext());
                    utils.cancelAllNotifications();
                    utils.cancelAllAlarmNotify();
                    try {
                        utils.createAlarmNotify();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            });
            rowView = paint(rowView, position);

            if ((!isNoTime) && (execution == null) && execDate.before(new Date())) {
                //rowView.setBackgroundColor(getContext().getResources().getColor(R.color.out_of_date));
                ((TextView) rowView.findViewById(R.id.tvPrescriptionsDayMessage))
                        .setTextColor(getContext().getResources().getColor(R.color.out_of_date));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rowView;
    }

}

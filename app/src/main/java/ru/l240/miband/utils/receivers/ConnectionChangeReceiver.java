package ru.l240.miband.utils.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Response;
import ru.fors.remsmed.core.MedContract;
import ru.fors.remsmed.core.dto.MedDTO;
import ru.fors.remsmed.core.dto.contact.Message;
import ru.fors.remsmed.core.dto.feedback.FeedbackAnswer;
import ru.fors.remsmed.core.dto.journal.JournalItem;
import ru.fors.remsmed.core.dto.measurements.UserMeasurement;
import ru.fors.remsmed.core.dto.prescriptions.PrescrExecution;
import ru.fors.remsmed.core.retrofitloaders.api.ApiFactory;
import ru.fors.remsmed.core.retrofitloaders.api.MedRetrofitService;
import ru.fors.remsmed.core.retrofitloaders.api.RetrofitCallback;
import ru.fors.remsmed.db.RequestTaskAddMeasurement;
import ru.fors.remsmed.db.RequestTaskAddMessage;
import ru.fors.remsmed.db.RequestTaskReauthorize;
import ru.fors.remsmed.fragments.HomeFragment;
import ru.fors.remsmed.utils.MedUtils;

/**
 * @author Alexander Popov on 22.05.15.
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context mContext, Intent intent) {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(HomeFragment.FILTER));
        Log.d("app", "Network connectivity change");
        if (intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                Log.i("app", "Network " + ni.getTypeName() + " connected");
                SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
                String mes = preferences.getString(MedUtils.SCHEDULER_MES_PREF, "");
                String journal = preferences.getString(MedUtils.SCHEDULER_JOURNAL_PREF, "");
                String message = preferences.getString(MedUtils.SCHEDULER_MESSAGES_PREF, "");
                String feedbackAnswer = preferences.getString(MedUtils.SCHEDULER_FEEDBACK_ANSWERS_PREF, "");
                String exec = preferences.getString(MedUtils.SCHEDULER_EXEC_PREF, "");
                String execDel = preferences.getString(MedUtils.SCHEDULER_EXEC_DELETE_PREF, "");
                //long contactId = preferences.getLong("ContactId", 0);
                final MedRetrofitService service = ApiFactory.getMedService();
                SharedPreferences cookiePreferences = mContext.getSharedPreferences(MedUtils.COOKIE_PREF, 0);
                final String cookie = cookiePreferences.getString(MedUtils.COOKIE_PREF, "");
                ContentResolver contentResolver = mContext.getContentResolver();
                if (!mes.isEmpty()) {
                    List<UserMeasurement> sync = new MedDTO<UserMeasurement>()
                            .getSync(new UserMeasurement(),
                                    MedContract.UserMeasurement.CONTENT_URI,
                                    MedContract.UserMeasurement.DEFAULT_PROJECTION,
                                    mContext);

                    RequestTaskAddMeasurement taskAddMeasurement = new RequestTaskAddMeasurement(mContext, true, sync);
                    taskAddMeasurement.execute((Void) null);
                }
                if (!journal.isEmpty()) {
                    List<JournalItem> sync = new MedDTO<JournalItem>()
                            .getSync(new JournalItem(),
                                    MedContract.Journal.CONTENT_URI,
                                    MedContract.Journal.DEFAULT_PROJECTION,
                                    mContext);
                    Call<List<MedDTO>> journalRecords = service.postAddJournalRecords(sync, cookie);
                    journalRecords.enqueue(new RetrofitCallback<List<MedDTO>>() {
                        @Override
                        public void onResponse(retrofit.Response<List<MedDTO>> response) {
                            if (response.isSuccess()) {
                                SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(MedUtils.SCHEDULER_JOURNAL_PREF, "");
                                editor.commit();
                                for (int i = 0; i < sync.size(); i++) {
                                    JournalItem journalItem = sync.get(i);
                                    contentResolver
                                            .delete(MedContract.Journal.CONTENT_URI,
                                                    MedContract.Journal._ID + "=?",
                                                    new String[]{journalItem.getId().toString()});
                                    journalItem.setId(response.body().get(i).getId());
                                    contentResolver.insert(MedContract.Journal.CONTENT_URI,
                                            journalItem.toContentValues());
                                }
                            } else {
                                RequestTaskReauthorize requestTaskReauthorize = new RequestTaskReauthorize(mContext);
                                requestTaskReauthorize.execute();
                            }
                            super.onResponse(response);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            super.onFailure(t);
                        }
                    });
                }
                if (!message.isEmpty()) {
                    List<Message> sync = new MedDTO<Message>()
                            .getSync(new Message(),
                                    MedContract.Messages.CONTENT_URI,
                                    MedContract.Messages.DEFAULT_PROJECTION,
                                    mContext);
                    RequestTaskAddMessage taskAddMessage = new RequestTaskAddMessage(mContext, true, sync, success -> {
                    });
                    taskAddMessage.execute();
                }
                if (!feedbackAnswer.isEmpty()) {
                    List<FeedbackAnswer> sync = new MedDTO<FeedbackAnswer>()
                            .getSync(new FeedbackAnswer(),
                                    MedContract.FeedbackAnswer.CONTENT_URI,
                                    MedContract.FeedbackAnswer.DEFAULT_PROJECTION,
                                    mContext);


                    Call<List<MedDTO>> answers = service.postAddPollAnswers(sync, cookie);
                    answers.enqueue(new RetrofitCallback<List<MedDTO>>() {
                        @Override
                        public void onResponse(Response<List<MedDTO>> response) {
                            if (response.isSuccess()) {
                                SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(MedUtils.SCHEDULER_FEEDBACK_ANSWERS_PREF, "");
                                editor.commit();
                                for (int i = 0; i < sync.size(); i++) {
                                    FeedbackAnswer feedbackAnswer = sync.get(i);
                                    contentResolver.delete(MedContract.FeedbackAnswer.CONTENT_URI,
                                            MedContract.FeedbackAnswer._ID + "=?",
                                            new String[]{feedbackAnswer.getId().toString()});
                                    feedbackAnswer.setId(response.body().get(i).getId());
                                    contentResolver.insert(MedContract.FeedbackAnswer.CONTENT_URI,
                                            feedbackAnswer.toContentValues());
                                }
                            } else {
                                RequestTaskReauthorize requestTaskReauthorize = new RequestTaskReauthorize(mContext);
                                requestTaskReauthorize.execute();
                            }
                            super.onResponse(response);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            super.onFailure(t);
                        }
                    });
                }
                if (!exec.isEmpty()) {
                    List<PrescrExecution> sync = new PrescrExecution().fromCursor(contentResolver
                            .query(MedContract.PrescrExecution.CONTENT_URI,
                                    MedContract.PrescrExecution.DEFAULT_PROJECTION,
                                    MedContract.PrescrExecution._ID + " < 0" +
                                            " and " + MedContract.PrescrExecution.KEY_EXEC_EXECUTION_IS_DELETED + " = 0",
                                    null,
                                    MedContract.PrescrExecution.DEFAULT_SORT));
                    Call<List<MedDTO>> listCall = service.postDoPrescriptionExecutions(sync, cookie);
                    listCall.enqueue(new RetrofitCallback<List<MedDTO>>() {
                        @Override
                        public void onResponse(Response<List<MedDTO>> response) {
                            if (response.isSuccess()) {
                                SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(MedUtils.SCHEDULER_EXEC_PREF, "");
                                editor.commit();
                                for (int i = 0; i < sync.size(); i++) {
                                    PrescrExecution prescrExecution = sync.get(i);
                                    contentResolver.delete(MedContract.PrescrExecution.CONTENT_URI,
                                            MedContract.PrescrExecution._ID + "=?",
                                            new String[]{prescrExecution.getId().toString()});
                                    prescrExecution.setId(response.body().get(i).getId());
                                    contentResolver.insert(MedContract.PrescrExecution.CONTENT_URI,
                                            prescrExecution.toContentValues());
                                }
                            } else {
                                RequestTaskReauthorize requestTaskReauthorize = new RequestTaskReauthorize(mContext);
                                requestTaskReauthorize.execute();
                            }
                            super.onResponse(response);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            super.onFailure(t);
                        }
                    });
                }
                if (!execDel.isEmpty()) {
                    List<PrescrExecution> sync = new PrescrExecution().fromCursor(contentResolver
                            .query(MedContract.PrescrExecution.CONTENT_URI,
                                    MedContract.PrescrExecution.DEFAULT_PROJECTION,
                                    MedContract.PrescrExecution.KEY_EXEC_EXECUTION_IS_DELETED + " = 1",
                                    null,
                                    MedContract.PrescrExecution.DEFAULT_SORT));
                    List<MedDTO> medDTOs = new ArrayList<>();
                    Stream.of(sync)
                            .forEach(execution -> medDTOs.add(new MedDTO(execution.getId())));
                    Call<List<MedDTO>> listCall = service.postUndoPrescriptionExecutions(medDTOs, cookie);
                    listCall.enqueue(new RetrofitCallback<List<MedDTO>>() {
                        @Override
                        public void onResponse(Response<List<MedDTO>> response) {
                            if (response.isSuccess()) {
                                SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(MedUtils.SCHEDULER_EXEC_DELETE_PREF, "");
                                editor.commit();
                                for (int i = 0; i < sync.size(); i++) {
                                    PrescrExecution prescrExecution = sync.get(i);
                                    contentResolver.delete(MedContract.PrescrExecution.CONTENT_URI,
                                            MedContract.PrescrExecution._ID + "=?",
                                            new String[]{prescrExecution.getId().toString()});
                                    prescrExecution.setId(response.body().get(i).getId());
                                    contentResolver.insert(MedContract.PrescrExecution.CONTENT_URI,
                                            prescrExecution.toContentValues());
                                }
                            } else {
                                RequestTaskReauthorize requestTaskReauthorize = new RequestTaskReauthorize(mContext);
                                requestTaskReauthorize.execute();
                            }
                            super.onResponse(response);
                        }
                    });
                }

            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d("app", "There's no network connectivity");
            }
        }
    }

    protected void updateLV() {

    }
}


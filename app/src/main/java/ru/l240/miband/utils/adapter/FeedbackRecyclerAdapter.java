package ru.l240.miband.utils.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit.Call;
import retrofit.Response;
import ru.fors.remsmed.MainActivity;
import ru.fors.remsmed.R;
import ru.fors.remsmed.core.MedContract;
import ru.fors.remsmed.core.dto.MedDTO;
import ru.fors.remsmed.core.dto.feedback.FeedbackAnswer;
import ru.fors.remsmed.core.dto.feedback.FeedbackItem;
import ru.fors.remsmed.core.dto.measurements.Measurement;
import ru.fors.remsmed.core.dto.measurements.MeasurementField;
import ru.fors.remsmed.core.dto.measurements.UserMeasurement;
import ru.fors.remsmed.core.retrofitloaders.api.ApiFactory;
import ru.fors.remsmed.core.retrofitloaders.api.MedRetrofitService;
import ru.fors.remsmed.core.retrofitloaders.api.RetrofitCallback;
import ru.fors.remsmed.db.DBHelper;
import ru.fors.remsmed.db.RequestTaskAddMeasurement;
import ru.fors.remsmed.db.RequestTaskReauthorize;
import ru.fors.remsmed.fragments.FeedbackFragment;
import ru.fors.remsmed.utils.MedUtils;

/**
 * @author Alexander Popov created on 14.08.2015.
 */
public class FeedbackRecyclerAdapter extends RecyclerView.Adapter<FeedbackRecyclerAdapter.ViewHolder> {

    private Cursor mCursor;
    private final Context mContext;
    private Date date;
    private RecyclerView lv;

    public FeedbackRecyclerAdapter(Context mContext, RecyclerView lv) {
        this.mContext = mContext;
        this.lv = lv;
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.feedback_listitem, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            FeedbackFragment feedbackFragment = (FeedbackFragment) ((MainActivity) mContext).getSupportFragmentManager().findFragmentByTag(FeedbackFragment.TAG);
            date = feedbackFragment.getDate();
            final long id = mCursor.getLong(mCursor.getColumnIndex(MedContract.FeedbackItem._ID));
            final FeedbackAnswer answer = new FeedbackAnswer().fromCursorOne(mContext.getContentResolver()
                    .query(MedContract.FeedbackAnswer.CONTENT_URI,
                            MedContract.FeedbackAnswer.DEFAULT_PROJECTION,
                            MedContract.FeedbackAnswer.KEY_FEEDBACK_ANSWER_QUESTION_ID + " = ? AND "
                                    + MedContract.FeedbackAnswer.KEY_FEEDBACK_ANSWER_DATE + " >= '"
                                    + new MedUtils().dfDB().format(date) + " 00:00:00' AND " + MedContract.FeedbackAnswer.KEY_FEEDBACK_ANSWER_DATE + " <= '"
                                    + new MedUtils().dfDB().format(date) + " 99:99:99'",
                            new String[]{String.valueOf(id)},
                            MedContract.FeedbackAnswer.DEFAULT_SORT));
            if (answer != null) {
                if (!(mCursor.getInt(mCursor.getColumnIndex(MedContract.FeedbackItem.KEY_FEEDBACK_WELL_RESULT)) == 1)) {
                    if (answer.getResult() < 3) {
                        holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_red);
                    } else if (answer.getResult() < 6) {
                        holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_orange);
                    } else if (answer.getResult() < 9) {
                        holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_yellow);
                    } else {
                        holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_green);
                    }
                } else {
                    if (answer.getResult() < 3) {
                        holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_green);
                    } else if (answer.getResult() < 6) {
                        holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_yellow);
                    } else if (answer.getResult() < 9) {
                        holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_orange);
                    } else {
                        holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_red);
                    }
                }
                if (answer.getResult() != null)
                    holder.tvFeedbackAnswer.setText(answer.getResult().toString());
            } else {
                holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_gray);
                holder.tvFeedbackAnswer.setText("");
            }
            String qText = mCursor.getString(mCursor.getColumnIndex(MedContract.FeedbackItem.KEY_FEEDBACK_QUESTION_TEXT));
            String lowLabel = mCursor.getString(mCursor.getColumnIndex(MedContract.FeedbackItem.KEY_FEEDBACK_LOW_LABEL));
            String hiLabel = mCursor.getString(mCursor.getColumnIndex(MedContract.FeedbackItem.KEY_FEEDBACK_HI_LABEL));
            final boolean reverse = mCursor.getInt(mCursor.getColumnIndex(MedContract.FeedbackItem.KEY_FEEDBACK_WELL_RESULT)) == 1;
            holder.tvFeedbackQuestion.setText(qText);

            holder.tvFeedbackValuesLeft.setText(lowLabel);
            holder.tvFeedbackValuesRight.setText(hiLabel);
            holder.itemView = paint(holder.itemView, position);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (answer != null) {
                        holder.seekBarFeedback.setProgress(answer.getResult());
                    }
                    if (holder.tvFeedbackAnswer.getText().toString().isEmpty()) {
                        holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_white);
                    }
                    switch (holder.feedBackAnswerLayout.getVisibility()) {
                        case View.GONE:
                            holder.feedBackAnswerLayout.setVisibility(View.VISIBLE);
                            lv.getLayoutManager().scrollToPosition(position);
                            break;
                        case View.VISIBLE:
                            holder.feedBackAnswerLayout.setVisibility(View.GONE);
                            break;
                    }


                    if (!reverse) {
                        holder.seekBarFeedback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (progress < 3) {
                                    holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_red);
                                } else if (progress < 6) {
                                    holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_orange);
                                } else if (progress < 9) {
                                    holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_yellow);
                                } else {
                                    holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_green);
                                }

                                holder.tvFeedbackAnswer.setText(String.valueOf(progress));
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                DBHelper dbHelper = new DBHelper(mContext);
                                final FeedbackAnswer answerNew = new FeedbackAnswer();
                                if (answer != null) {
                                    answer.setDate(date);
                                    answer.setCasePollQuestionId(id);
                                    answer.setResult(seekBar.getProgress());
                                    answer.setUpdate(true);
                                } else {
                                    answerNew.setDate(date);
                                    answerNew.setResult(seekBar.getProgress());
                                    answerNew.setCasePollQuestionId(id);
                                    answerNew.setUpdate(false);
                                }

                                holder.feedBackAnswerLayout.setVisibility(View.GONE);

                                final ContentResolver contentResolver = mContext.getContentResolver();
                                if (MedUtils.isNetworkConnected(mContext)) {
                                    MedRetrofitService service = ApiFactory.getMedService();
                                    SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.COOKIE_PREF, 0);
                                    String cookie = preferences.getString(MedUtils.COOKIE_PREF, "");
                                    Call<List<MedDTO>> answers = service.postAddPollAnswers(Collections.singletonList(answer != null ? answer : answerNew), cookie);
                                    answers.enqueue(new RetrofitCallback<List<MedDTO>>() {
                                        @Override
                                        public void onResponse(Response<List<MedDTO>> response) {
                                            if (response.isSuccess()) {
                                                for (MedDTO medO : response.body()) {
                                                    if (answer != null) {
                                                        answer.setId(medO.getId());
                                                        contentResolver
                                                                .update(MedContract.FeedbackAnswer.CONTENT_URI,
                                                                        answer.toContentValues(),
                                                                        MedContract.FeedbackAnswer._ID + "= ?",
                                                                        new String[]{medO.getId().toString()});
                                                    } else {
                                                        answerNew.setId(medO.getId());
                                                        contentResolver
                                                                .insert(MedContract.FeedbackAnswer.CONTENT_URI,
                                                                        answerNew.toContentValues());
                                                    }
                                                }
                                                addWellnessIndex();
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

                                } else {
                                    long syncId = new MedDTO<FeedbackAnswer>().getSyncId(MedContract.FeedbackAnswer.CONTENT_URI,
                                            mContext);
                                    if (answer != null) {
                                        contentResolver.delete(MedContract.FeedbackAnswer.CONTENT_URI,
                                                MedContract.FeedbackAnswer._ID + " = ?",
                                                new String[]{answer.getId().toString()});
                                        answer.setId(syncId);
                                        contentResolver.insert(MedContract.FeedbackAnswer.CONTENT_URI,
                                                answer.toContentValues());
                                    } else {
                                        answerNew.setId(syncId);
                                        contentResolver.insert(MedContract.FeedbackAnswer.CONTENT_URI,
                                                answerNew.toContentValues());

                                    }
                                    SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(MedUtils.SCHEDULER_FEEDBACK_ANSWERS_PREF, "need");
                                    editor.commit();
                                    addWellnessIndex();
                                }
                            }
                        });
                    } else {
                        holder.seekBarFeedback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (progress < 3) {
                                    holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_green);
                                } else if (progress < 6) {
                                    holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_yellow);
                                } else if (progress < 9) {
                                    holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_orange);
                                } else {
                                    holder.tvFeedbackAnswer.setBackgroundResource(R.drawable.feedback_red);
                                }

                                holder.tvFeedbackAnswer.setText(String.valueOf(progress));
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                final FeedbackAnswer answerNew = new FeedbackAnswer();
                                if (answer != null) {
                                    answer.setDate(date);
                                    answer.setCasePollQuestionId(id);
                                    answer.setResult(seekBar.getProgress());
                                    answer.setUpdate(true);
                                } else {
                                    answerNew.setDate(date);
                                    answerNew.setResult(seekBar.getProgress());
                                    answerNew.setCasePollQuestionId(id);
                                    answerNew.setUpdate(false);
                                }

                                holder.feedBackAnswerLayout.setVisibility(View.GONE);

                                final ContentResolver contentResolver = mContext.getContentResolver();
                                if (MedUtils.isNetworkConnected(mContext)) {
                                    MedRetrofitService service = ApiFactory.getMedService();
                                    SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.COOKIE_PREF, 0);
                                    String cookie = preferences.getString(MedUtils.COOKIE_PREF, "");
                                    Call<List<MedDTO>> answers = service.postAddPollAnswers(Collections.singletonList(answer != null ? answer : answerNew), cookie);
                                    answers.enqueue(new RetrofitCallback<List<MedDTO>>() {
                                        @Override
                                        public void onResponse(Response<List<MedDTO>> response) {
                                            if (response.isSuccess()) {

                                                for (MedDTO medO : response.body()) {
                                                    if (answer != null) {
                                                        answer.setId(medO.getId());
                                                        contentResolver
                                                                .update(MedContract.FeedbackAnswer.CONTENT_URI,
                                                                        answer.toContentValues(),
                                                                        MedContract.FeedbackAnswer._ID + "= ?",
                                                                        new String[]{medO.getId().toString()});
                                                    } else {
                                                        answerNew.setId(medO.getId());
                                                        contentResolver
                                                                .insert(MedContract.FeedbackAnswer.CONTENT_URI,
                                                                        answerNew.toContentValues());
                                                    }
                                                }
                                                addWellnessIndex();
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

                                } else {
                                    long syncId = new MedDTO<FeedbackAnswer>().getSyncId(MedContract.FeedbackAnswer.CONTENT_URI,
                                            mContext);
                                    if (answer != null) {
                                        contentResolver.delete(MedContract.FeedbackAnswer.CONTENT_URI,
                                                MedContract.FeedbackAnswer._ID + " = ?",
                                                new String[]{answer.getId().toString()});
                                        answer.setId(syncId);
                                        contentResolver.insert(MedContract.FeedbackAnswer.CONTENT_URI,
                                                answer.toContentValues());

                                    } else {
                                        answerNew.setId(syncId);
                                        contentResolver.insert(MedContract.FeedbackAnswer.CONTENT_URI,
                                                answerNew.toContentValues());

                                    }
                                    addWellnessIndex();
                                    SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(MedUtils.SCHEDULER_FEEDBACK_ANSWERS_PREF, "need");
                                    editor.commit();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvFeedbackAnswer;
        public TextView tvFeedbackQuestion;
        public TextView tvFeedbackValuesLeft;
        public TextView tvFeedbackValuesRight;
        public LinearLayout feedBackAnswerLayout;
        public SeekBar seekBarFeedback;
        public View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvFeedbackAnswer = (TextView) itemView.findViewById(R.id.tvFeedbackAnswer);
            tvFeedbackQuestion = (TextView) itemView.findViewById(R.id.tvFeedbackQuestion);
            tvFeedbackValuesLeft = (TextView) itemView.findViewById(R.id.tvFeedbackValuesLeft);
            tvFeedbackValuesRight = (TextView) itemView.findViewById(R.id.tvFeedbackValuesRight);
            feedBackAnswerLayout = (LinearLayout) itemView.findViewById(R.id.feedBackAnswerLayout);
            seekBarFeedback = (SeekBar) itemView.findViewById(R.id.seekBarFeedback);
        }
    }

    private View paint(View rowView, int position) {
        if (position % 2 == 0)
            rowView.setBackgroundColor(mContext.getResources().getColor(R.color.listview_first));
        else
            rowView.setBackgroundColor(mContext.getResources().getColor(R.color.listview_second));
        return rowView;
    }

    private void addWellnessIndex() {
        UserMeasurement userMeasurement = new UserMeasurement();
        userMeasurement.setMeasurementDate(new Date());
        ContentResolver contentResolver = mContext.getContentResolver();
        Measurement measurement = new Measurement().fromCursorOne(contentResolver.query(MedContract.Measurement.CONTENT_URI,
                MedContract.Measurement.DEFAULT_PROJECTION,
                MedContract.Measurement.KEY_MEASUREMENT_SYS_NAME + " = ?",
                new String[]{"wellness_index"},
                MedContract.Measurement.DEFAULT_SORT));
        userMeasurement.setMeasurementFieldId(new MeasurementField().fromCursorOne(contentResolver
                .query(MedContract.MeasurementField.CONTENT_URI,
                        MedContract.MeasurementField.DEFAULT_PROJECTION,
                        MedContract.MeasurementField.KEY_MEASUREMENT_FIELD_MEASUREMENT_ID + " = ?",
                        new String[]{measurement.getId().toString()},
                        MedContract.MeasurementField.DEFAULT_SORT)).getId());
        Integer count = 0;
        List<FeedbackAnswer> feedbackAnswers = new FeedbackAnswer().fromCursor(contentResolver.query(MedContract.FeedbackAnswer.CONTENT_URI,
                MedContract.FeedbackAnswer.DEFAULT_PROJECTION,
                MedContract.FeedbackAnswer.KEY_FEEDBACK_ANSWER_DATE + " >= '"
                        + new MedUtils().dfDB().format(new Date()) + " 00:00:00' AND " + MedContract.FeedbackAnswer.KEY_FEEDBACK_ANSWER_DATE + " <= '"
                        + new MedUtils().dfDB().format(new Date()) + " 99:99:99'",
                null,
                MedContract.FeedbackAnswer.DEFAULT_SORT));
        for (int i = 0; i < feedbackAnswers.size(); i++) {
            FeedbackAnswer feedbackAnswer = feedbackAnswers.get(i);
            FeedbackItem feedbackItem = new FeedbackItem().fromCursorOne(contentResolver
                    .query(MedContract.FeedbackItem.CONTENT_URI,
                            MedContract.FeedbackItem.DEFAULT_PROJECTION,
                            MedContract.FeedbackItem._ID + " = ?",
                            new String[]{String.valueOf(feedbackAnswer.getCasePollQuestionId())},
                            MedContract.FeedbackItem.DEFAULT_SORT));
            if (!feedbackItem.getReverse()) {
                count += feedbackAnswer.getResult();
            } else {
                count += (10 - feedbackAnswer.getResult());
            }
        }
        userMeasurement.setMeasurementType(measurement.getId());
        userMeasurement.setMeasurementName(measurement.getName());
        userMeasurement.setMeasurementUnit(measurement.getUnit());
        userMeasurement.setMeasurementValue(String.format("%.0f", (float) ((100 * count) / (getItemCount() * 10))));
        RequestTaskAddMeasurement taskAddMeasurement = new RequestTaskAddMeasurement(mContext, false, Collections.singletonList(userMeasurement), true);
        taskAddMeasurement.execute();
    }

}

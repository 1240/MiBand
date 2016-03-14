package ru.l240.miband.retrofit;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import ru.l240.miband.R;
import ru.l240.miband.models.UserMeasurement;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.utils.HttpUtils;
import ru.l240.miband.utils.MedUtils;


/**
 * @author Alexander Popov on 20.05.15.
 */
public class RequestTaskAddMeasurement extends AsyncTask<Void, Void, Boolean> {

    public final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Context mContext;
    Boolean isSync;
    Boolean isWellIndex = false;
    private List<UserMeasurement> data;

    public RequestTaskAddMeasurement(Context mContext, Boolean isSync, List<UserMeasurement> data) {
        this.mContext = mContext;
        this.isSync = isSync;
        this.data = data;
    }

    public RequestTaskAddMeasurement(Context mContext, Boolean isSync, List<UserMeasurement> data, Boolean isWellIndex) {
        this.mContext = mContext;
        this.isSync = isSync;
        this.data = data;
        this.isWellIndex = isWellIndex;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            SharedPreferences preferences = mContext.getSharedPreferences("LOGIN", 0);
            String login = preferences.getString(MedUtils.LOGIN_PREF, "");
            String pass = preferences.getString(MedUtils.PASS_PREF, "");
            HttpUtils httpUtils = new HttpUtils();
            String loginResponse = httpUtils.login(String.format(mContext.getResources().getString(R.string.url) + "/login?username=%s&password=%s", login, pass));
            if (!loginResponse.contains("Logged in")) {
                return false;
            }
            String addMeasurementValues = mContext.getResources().getString(R.string.url) + "/addMeasurementValues";
            String addMeasurementValuesResponse = httpUtils.postMethod(addMeasurementValues, getJsonData().toString());

            if (isSync) {
                RealmHelper.clear(Realm.getInstance(mContext), UserMeasurement.class);
            }
            JSONArray response = new JSONArray(addMeasurementValuesResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            SharedPreferences preferences = mContext.getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(MedUtils.SCHEDULER_MES_PREF, "");
            editor.commit();
        }
    }


    public JSONArray getJsonData() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (UserMeasurement userMes : data) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("measurementFieldId", userMes.getMeasurementId());
            jsonObject.put("measurementDate", simpleDateFormat.format(userMes.getMeasurementDate()));
            jsonObject.put("measurementValue", userMes.getStrValue());
            jsonObject.put("measurementComment", "From MI Band");
            jsonArray.put(jsonObject);


        }
        return jsonArray;
    }
}

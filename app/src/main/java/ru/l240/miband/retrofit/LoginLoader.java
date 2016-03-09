package ru.l240.miband.retrofit;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import ru.l240.miband.R;
import ru.l240.miband.models.Login;
import ru.l240.miband.models.Measurement;
import ru.l240.miband.models.MeasurementField;
import ru.l240.miband.models.Profile;
import ru.l240.miband.realm.RealmHelper;
import ru.l240.miband.retrofit.response.MedResponse;
import ru.l240.miband.retrofit.response.RequestResult;
import ru.l240.miband.retrofit.response.Response;
import ru.l240.miband.utils.HttpUtils;
import ru.l240.miband.utils.MedUtils;


/**
 * @author Alexander Popov created on 18.09.2015.
 */
public class LoginLoader extends BaseLoader {


    public LoginLoader(Context context) {
        super(context);
    }

    @Override
    protected Response apiCall() throws IOException {
        Profile profile = null;
        try {
            ApiService service = ApiFac.getApiService();
            SharedPreferences loginPreferences = getContext().getSharedPreferences("LOGIN", 0);
            String login = loginPreferences.getString(MedUtils.LOGIN_PREF, "");
            String pass = loginPreferences.getString(MedUtils.PASS_PREF, "");
            Call<JSONObject> authorize = service.authorize(new Login(login, pass));
            retrofit2.Response<JSONObject> execute = authorize.execute();
            String cookie = execute.raw().header("Set-Cookie");
            SharedPreferences cookiePreferences = getContext().getSharedPreferences(MedUtils.COOKIE_PREF, 0);
            SharedPreferences.Editor editor = cookiePreferences.edit();
            editor.putString(MedUtils.COOKIE_PREF, cookie);
            editor.commit();
            //save profile
            Call<Profile> call = service.getCurrentUser(cookie);
            profile = call.execute().body();
            RealmHelper.save(Realm.getInstance(getContext()), Collections.singletonList(profile));
//            dbHelper.createProfile(profile);

            Call<List<Measurement>> getMeasurementsDictionary = service.getMeasurementsDictionary(cookie);
            List<Measurement> measurements = getMeasurementsDictionary.execute().body();
            RealmHelper.save(Realm.getInstance(getContext()), measurements);

        } catch (Exception e) {
            e.printStackTrace();
            return new MedResponse()
                    .setRequestResult(RequestResult.ERROR)
                    .setAnswer(profile);
        }

        return new MedResponse()
                .setRequestResult(RequestResult.SUCCESS)
                .setAnswer(profile);
    }

    @Override
    protected void onError() {
        RealmHelper.clear(Realm.getInstance(getContext()), Profile.class);
        RealmHelper.clear(Realm.getInstance(getContext()), Measurement.class);
    }
}

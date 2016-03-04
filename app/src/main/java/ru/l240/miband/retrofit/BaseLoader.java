package ru.l240.miband.retrofit;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.AsyncTaskLoader;

import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import ru.l240.miband.models.Login;
import ru.l240.miband.retrofit.response.RequestResult;
import ru.l240.miband.retrofit.response.Response;
import ru.l240.miband.utils.MedUtils;

/**
 * @author Alexander Popov created on 18.09.2015.
 */
public abstract class BaseLoader extends AsyncTaskLoader<Response> {

    public BaseLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Response loadInBackground() {
        try {
            Response response = apiCall();
            if (response.getRequestResult() == RequestResult.SUCCESS) {
                response.save(getContext());
                onSuccess();
            } else {
                onError();
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            onError();
            return new Response();
        } catch (NullPointerException e) {
            e.printStackTrace();
            onError();
            ApiService service = ApiFac.getApiService();
            SharedPreferences loginPreferences = getContext().getSharedPreferences("LOGIN", 0);
            String login = loginPreferences.getString(MedUtils.LOGIN_PREF, "");
            String pass = loginPreferences.getString(MedUtils.PASS_PREF, "");
            Call<JSONObject> authorize = service.authorize(new Login(login, pass));
            authorize.enqueue(new RetrofitCallback<JSONObject>() {
                @Override
                public void onResponse(Call<JSONObject> call, retrofit2.Response<JSONObject> response) {
                    String cookie = response.raw().header("Set-Cookie");
                    SharedPreferences cookiePreferences = getContext().getSharedPreferences(MedUtils.COOKIE_PREF, 0);
                    SharedPreferences.Editor editor = cookiePreferences.edit();
                    editor.putString(MedUtils.COOKIE_PREF, cookie);
                    editor.commit();
                    super.onResponse(call, response);
                }
            });
            return new Response();

        }
    }

    protected void onSuccess() {
    }

    protected void onError() {
    }

    protected abstract Response apiCall() throws IOException;
}

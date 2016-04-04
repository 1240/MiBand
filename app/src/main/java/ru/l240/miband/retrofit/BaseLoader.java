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
                onSuccess();
            } else {
                onError();
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            onError();
            return new Response();
        }
    }

    protected void onSuccess() {
    }

    protected void onError() {
    }

    protected abstract Response apiCall() throws IOException;
}

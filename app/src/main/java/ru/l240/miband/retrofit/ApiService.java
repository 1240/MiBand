package ru.l240.miband.retrofit;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ru.l240.miband.models.Login;
import ru.l240.miband.models.Profile;
import ru.l240.miband.models.UserMeasurement;

/**
 * @author Alexander Popov created on 01.03.2016.
 */
public interface ApiService {

    @Headers({"Content-Type: application/json"})
    @GET("iapp/getCurrentUser")
    Call<Profile> getCurrentUser(@Header("Cookie") String cookieA);

    @POST("iapp/addMeasurementValues")
    @Headers({"Content-Type: application/json"})
    public Call<List<UserMeasurement>> postAddMeasurementValues(@Body List<UserMeasurement> body, @Header("Cookie") String cookie);

    @Headers({"Content-Type: application/json"})
    @POST("iapp/login")
    Call<JSONObject> authorize(@Body Login login);

}

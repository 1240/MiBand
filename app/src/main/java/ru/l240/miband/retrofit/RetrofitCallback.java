package ru.l240.miband.retrofit;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Alexander Popov created on 18.09.2015.
 */
public abstract class RetrofitCallback<T> implements Callback<T> {

    @Override
    public void onResponse(Call<T> call, Response<T> response) {

    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {

    }
}


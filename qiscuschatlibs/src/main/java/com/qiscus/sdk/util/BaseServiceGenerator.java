package com.qiscus.sdk.util;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedByteArray;

/**
 * Created on : December 09, 2015
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class BaseServiceGenerator {
    public static <S> S createService(Class<S> serviceClass, String baseUrl, RestAdapter.LogLevel logLevel) {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setLogLevel(logLevel)
                .setConverter(new GsonConverter(Qson.pluck().getParser()))
                .setErrorHandler(cause -> {
                    if (cause.getKind().equals(RetrofitError.Kind.HTTP)) {
                        String json = new String(((TypedByteArray) cause.getResponse().getBody()).getBytes());
                        try {
                            JSONObject object = new JSONObject(json);
                            return new Throwable(object.getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (cause.getKind().equals(RetrofitError.Kind.NETWORK)) {
                        return new Throwable("Please check your internet connection!");
                    }
                    return cause;
                })
                .setEndpoint(baseUrl);

        RestAdapter adapter = builder.build();

        return adapter.create(serviceClass);
    }
}

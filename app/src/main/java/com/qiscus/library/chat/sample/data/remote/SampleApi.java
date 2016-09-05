package com.qiscus.library.chat.sample.data.remote;

import com.qiscus.library.chat.sample.BuildConfig;
import com.qiscus.library.chat.sample.data.model.AccountInfo;
import com.qiscus.library.chat.sample.data.remote.response.Response;
import com.qiscus.sdk.util.BaseServiceGenerator;

import retrofit.RestAdapter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public enum SampleApi {
    INSTANCE;

    private final Api api;

    SampleApi() {
        api = BaseServiceGenerator.createService(Api.class, "https://qvc-engine-staging.herokuapp.com",
                                                 BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE);
    }

    public static SampleApi getInstance() {
        return INSTANCE;
    }

    public Observable<AccountInfo> login(String email, String password) {
        return api.login(email, password).map(response -> response.getResult("consumer", AccountInfo.class));
    }

    private interface Api {
        @FormUrlEncoded
        @POST("/consumers/session")
        Observable<Response> login(@Field("email") String email,
                                   @Field("password") String password);
    }
}

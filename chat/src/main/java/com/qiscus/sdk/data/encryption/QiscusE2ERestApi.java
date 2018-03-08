/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.data.encryption;

import android.support.annotation.RestrictTo;
import android.util.Base64;

import com.google.gson.JsonElement;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.encryption.core.BundlePublicCollection;
import com.qiscus.sdk.data.encryption.core.IllegalDataSizeException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created on : March 06, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum QiscusE2ERestApi {
    INSTANCE;

    private Api api;

    QiscusE2ERestApi() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(this::headersInterceptor)
                .build();

        api = new Retrofit.Builder()
                .baseUrl("https://upk-stag.qiscus.com")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(Api.class);
    }

    private Response headersInterceptor(Interceptor.Chain chain) throws IOException {
        Request req = chain.request().newBuilder()
                .addHeader("QISCUS_SDK_APP_ID", Qiscus.getAppId())
                .addHeader("QISCUS_SDK_AUTH_TOKEN", Qiscus.hasSetupUser() ? Qiscus.getToken() : "")
                .build();
        return chain.proceed(req);
    }

    public static QiscusE2ERestApi getInstance() {
        return INSTANCE;
    }

    public Observable<BundlePublicCollection> getBundlePublicCollection(String userId) {
        return api.getBundlePublicCollection(userId)
                .map(json -> json.getAsJsonObject()
                        .get("data").getAsJsonObject()
                        .get("user_public_key").getAsJsonObject()
                        .get("public_key").getAsString())
                .map(encodedBundle -> {
                    try {
                        byte[] decodedBundle = Base64.decode(encodedBundle.getBytes(), Base64.DEFAULT);
                        return BundlePublicCollection.decode(decodedBundle);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IllegalDataSizeException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (SignatureException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(bundlePublicCollection -> bundlePublicCollection != null);
    }

    public Observable<BundlePublicCollection> saveBundlePublicCollection(BundlePublicCollection bundlePublicCollection) {
        return Observable.just(bundlePublicCollection)
                .map(bundlePublicCollection1 -> {
                    try {
                        return Base64.encodeToString(bundlePublicCollection1.encode(), Base64.DEFAULT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(encodedBundle -> encodedBundle != null)
                .map(encodedBundle -> {
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("public_key", encodedBundle);
                    return requestBody;
                })
                .flatMap(requestBody -> api.saveBundlePublicCollection(requestBody))
                .map(json -> json.getAsJsonObject()
                        .get("data").getAsJsonObject()
                        .get("user_public_key").getAsJsonObject()
                        .get("public_key").getAsString())
                .map(encodedBundle -> {
                    try {
                        byte[] decodedBundle = Base64.decode(encodedBundle.getBytes(), Base64.DEFAULT);
                        return BundlePublicCollection.decode(decodedBundle);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IllegalDataSizeException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (SignatureException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(bundlePublicCollection1 -> bundlePublicCollection1 != null);
    }

    private interface Api {
        @GET("/api/v1/user-public-key/{user_id}")
        Observable<JsonElement> getBundlePublicCollection(@Path("user_id") String userId);

        @Headers("Content-Type: application/json")
        @POST("/api/v1/user-public-key")
        Observable<JsonElement> saveBundlePublicCollection(@Body Map<String, Object> body);
    }
}

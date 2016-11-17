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

package com.qiscus.sdk.data.remote;

import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.util.QiscusDateUtil;
import com.qiscus.sdk.util.QiscusFileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public enum QiscusApi {
    INSTANCE;
    private static DateFormat dateFormat;

    private final OkHttpClient httpClient;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private String baseUrl;
    private final Api api;

    QiscusApi() {
        baseUrl = Qiscus.getAppServer();

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(Api.class);
    }

    public static QiscusApi getInstance() {
        return INSTANCE;
    }

    public Observable<QiscusAccount> loginOrRegister(String email, String password, String username, String avatarUrl) {
        return api.loginOrRegister(email, password, username, avatarUrl)
                .map(jsonElement -> {
                    JsonObject jsonAccount = jsonElement.getAsJsonObject().get("results")
                            .getAsJsonObject().get("user").getAsJsonObject();
                    QiscusAccount qiscusAccount = new QiscusAccount();
                    qiscusAccount.setId(jsonAccount.get("id").getAsInt());
                    qiscusAccount.setUsername(jsonAccount.get("username").getAsString());
                    qiscusAccount.setEmail(jsonAccount.get("email").getAsString());
                    qiscusAccount.setToken(jsonAccount.get("token").getAsString());
                    qiscusAccount.setRtKey(jsonAccount.get("rtKey").getAsString());
                    qiscusAccount.setAvatar(jsonAccount.get("avatar_url").getAsString());
                    return qiscusAccount;
                });
    }

    public Observable<QiscusChatRoom> getChatRoom(List<String> withEmails, String distinctId, String options) {
        return api.createOrGetChatRoom(Qiscus.getToken(), withEmails, distinctId, options)
                .onErrorReturn(throwable -> null)
                .map(jsonElement -> {
                    QiscusChatRoom qiscusChatRoom;
                    if (jsonElement != null) {
                        JsonObject jsonChatRoom = jsonElement.getAsJsonObject().get("results")
                                .getAsJsonObject().get("room").getAsJsonObject();
                        qiscusChatRoom = new QiscusChatRoom();
                        qiscusChatRoom.setId(jsonChatRoom.get("id").getAsInt());
                        qiscusChatRoom.setDistinctId(distinctId == null ? "default" : distinctId);
                        qiscusChatRoom.setLastCommentId(jsonChatRoom.get("last_comment_id").getAsInt());
                        qiscusChatRoom.setLastCommentMessage(jsonChatRoom.get("last_comment_message").getAsString());
                        qiscusChatRoom.setLastTopicId(jsonChatRoom.get("last_topic_id").getAsInt());
                        qiscusChatRoom.setOptions(jsonChatRoom.get("options").isJsonNull() ? null
                                : jsonChatRoom.get("options").getAsString());
                        qiscusChatRoom.setMember(withEmails);
                        JsonArray comments = jsonElement.getAsJsonObject().get("results")
                                .getAsJsonObject().get("comments").getAsJsonArray();

                        if (comments.size() > 0) {
                            JsonObject lastComment = comments.get(0).getAsJsonObject();
                            qiscusChatRoom.setLastCommentSender(lastComment.get("username").getAsString());
                            qiscusChatRoom.setLastCommentSenderEmail(lastComment.get("email").getAsString());
                            try {
                                qiscusChatRoom.setLastCommentTime(dateFormat.parse(lastComment.get("timestamp").getAsString()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        return qiscusChatRoom;
                    }
                    qiscusChatRoom = Qiscus.getDataStore().getChatRoom(withEmails.get(0),
                            distinctId == null ? "default" : distinctId);
                    if (qiscusChatRoom == null) {
                        throw new RuntimeException("Unable to connect with qiscus server!");
                    }
                    return qiscusChatRoom;
                });
    }

    public Observable<QiscusChatRoom> getChatRoom(int roomId) {
        return api.getChatRoom(Qiscus.getToken(), roomId)
                .onErrorReturn(throwable -> null)
                .map(jsonElement -> {
                    QiscusChatRoom qiscusChatRoom;
                    if (jsonElement != null) {
                        JsonObject jsonChatRoom = jsonElement.getAsJsonObject().get("results")
                                .getAsJsonObject().get("room").getAsJsonObject();
                        qiscusChatRoom = new QiscusChatRoom();
                        qiscusChatRoom.setId(jsonChatRoom.get("id").getAsInt());
                        //TODO minta server ngasih tau distinctId biar bisa disimpen
                        //qiscusChatRoom.setDistinctId("default");
                        qiscusChatRoom.setLastCommentId(jsonChatRoom.get("last_comment_id").getAsInt());
                        qiscusChatRoom.setLastCommentMessage(jsonChatRoom.get("last_comment_message").getAsString());
                        qiscusChatRoom.setLastTopicId(jsonChatRoom.get("last_topic_id").getAsInt());
                        qiscusChatRoom.setOptions(jsonChatRoom.get("options").isJsonNull() ? null
                                : jsonChatRoom.get("options").getAsString());
                        //TODO minta server ngasih tau member room siapa aja
                        //qiscusChatRoom.setMember(withEmails);
                        qiscusChatRoom.setMember(Qiscus.getDataStore().getRoomMembers(roomId));
                        JsonArray comments = jsonElement.getAsJsonObject().get("results")
                                .getAsJsonObject().get("comments").getAsJsonArray();

                        if (comments.size() > 0) {
                            JsonObject lastComment = comments.get(0).getAsJsonObject();
                            qiscusChatRoom.setLastCommentSender(lastComment.get("username").getAsString());
                            qiscusChatRoom.setLastCommentSenderEmail(lastComment.get("email").getAsString());
                            try {
                                qiscusChatRoom.setLastCommentTime(dateFormat.parse(lastComment.get("timestamp").getAsString()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        return qiscusChatRoom;
                    }
                    qiscusChatRoom = Qiscus.getDataStore().getChatRoom(roomId);
                    if (qiscusChatRoom == null) {
                        throw new RuntimeException("Unable to connect with qiscus server!");
                    }
                    return qiscusChatRoom;
                });
    }

    public Observable<QiscusComment> getComments(int topicId, int lastCommentId) {
        return api.getComments(Qiscus.getToken(), topicId, lastCommentId)
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return null;
                })
                .filter(jsonElement -> jsonElement != null)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> {
                    QiscusComment qiscusComment = new QiscusComment();
                    JsonObject jsonComment = jsonElement.getAsJsonObject();
                    qiscusComment.setTopicId(topicId);
                    qiscusComment.setId(jsonComment.get("id").getAsInt());
                    qiscusComment.setUniqueId(String.valueOf(jsonComment.get("id").getAsInt()));
                    qiscusComment.setCommentBeforeId(jsonComment.get("comment_before_id").getAsInt());
                    qiscusComment.setMessage(jsonComment.get("message").getAsString());
                    qiscusComment.setSender(jsonComment.get("username").getAsString());
                    qiscusComment.setSenderEmail(jsonComment.get("email").getAsString());
                    try {
                        qiscusComment.setTime(dateFormat.parse(jsonComment.get("timestamp").getAsString()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return qiscusComment;
                });
    }

    public Observable<QiscusComment> postComment(QiscusComment qiscusComment) {
        return api.postComment(Qiscus.getToken(), qiscusComment.getMessage(),
                qiscusComment.getTopicId(), qiscusComment.getUniqueId())
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject()
                            .get("results").getAsJsonObject().get("comment").getAsJsonObject();
                    qiscusComment.setId(jsonComment.get("id").getAsInt());
                    qiscusComment.setCommentBeforeId(jsonComment.get("comment_before_id").getAsInt());
                    return qiscusComment;
                });
    }

    public Observable<Void> markTopicAsRead(int topicId) {
        return api.markTopicAsRead(topicId, Qiscus.getToken());
    }

    public Observable<QiscusComment> sync() {
        QiscusComment latestComment = Qiscus.getDataStore().getLatestComment();
        if (latestComment == null || !"Today".equals(QiscusDateUtil.toTodayOrDate(latestComment.getTime()))) {
            return Observable.empty();
        }
        return api.sync(Qiscus.getToken(), latestComment.getId())
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return null;
                })
                .filter(jsonElement -> jsonElement != null)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> {
                    QiscusComment qiscusComment = new QiscusComment();
                    JsonObject jsonComment = jsonElement.getAsJsonObject();
                    qiscusComment.setId(jsonComment.get("id").getAsInt());
                    qiscusComment.setRoomId(jsonComment.get("room_id").getAsInt());
                    qiscusComment.setTopicId(jsonComment.get("topic_id").getAsInt());
                    qiscusComment.setUniqueId(jsonComment.get("unique_id").isJsonNull() ?
                            qiscusComment.getId() + "" : jsonComment.get("unique_id").getAsString());
                    qiscusComment.setCommentBeforeId(jsonComment.get("comment_before_id").getAsInt());
                    qiscusComment.setMessage(jsonComment.get("message").getAsString());
                    qiscusComment.setSender(jsonComment.get("username").getAsString());
                    qiscusComment.setSenderEmail(jsonComment.get("email").getAsString());
                    qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
                    try {
                        qiscusComment.setTime(dateFormat.parse(jsonComment.get("timestamp").getAsString()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return qiscusComment;
                });
    }

    public Observable<Uri> uploadFile(File file, ProgressListener progressListener) {
        return Observable.create(subscriber -> {
            long fileLength = file.length();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("token", Qiscus.getToken())
                    .addFormDataPart("file", file.getName(),
                            new CountingFileRequestBody(file, totalBytes -> {
                                int progress = (int) (totalBytes * 100 / fileLength);
                                progressListener.onProgress(progress);
                            }))
                    .build();

            Request request = new Request.Builder()
                    .url(baseUrl + "/api/v2/mobile/upload")
                    .post(requestBody).build();

            try {
                Response response = httpClient.newCall(request).execute();
                JSONObject responseJ = new JSONObject(response.body().string());
                String result = responseJ.getJSONObject("results").getJSONObject("file").getString("url");

                subscriber.onNext(Uri.parse(result));
                subscriber.onCompleted();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        });
    }

    public Observable<File> downloadFile(final int topicId, final String url,
                                         final String fileName, ProgressListener progressListener) {
        return Observable.create(subscriber -> {
            if (subscriber.isUnsubscribed()) {
                return;
            }

            InputStream inputStream = null;
            FileOutputStream fos = null;
            try {
                Request request = new Request.Builder().url(url).build();

                Response response = httpClient.newCall(request).execute();

                File output = new File(QiscusFileUtil.generateFilePath(fileName, topicId));
                fos = new FileOutputStream(output.getPath());
                if (!response.isSuccessful()) {
                    throw new IOException();
                } else {
                    ResponseBody responseBody = response.body();
                    long fileLength = responseBody.contentLength();

                    inputStream = responseBody.byteStream();
                    byte buffer[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = inputStream.read(buffer)) != -1) {
                        total += count;
                        long totalCurrent = total;
                        if (fileLength > 0) {
                            progressListener.onProgress((totalCurrent * 100 / fileLength));
                        }
                        fos.write(buffer, 0, count);
                    }
                    fos.flush();

                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(output);
                        subscriber.onCompleted();
                    }
                }
            } catch (Exception e) {
                throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(e, url));
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException ignored) {
                }
            }
        });
    }

    private interface Api {

        @FormUrlEncoded
        @POST("/api/v2/mobile/login_or_register")
        Observable<JsonElement> loginOrRegister(@Field("email") String email,
                                                @Field("password") String password,
                                                @Field("username") String username,
                                                @Field("avatar_url") String avatarUrl);

        @FormUrlEncoded
        @POST("/api/v2/mobile/get_or_create_room_with_target")
        Observable<JsonElement> createOrGetChatRoom(@Field("token") String token,
                                                    @Field("emails[]") List<String> emails,
                                                    @Field("distinct_id") String distinctId,
                                                    @Field("options") String options);

        @GET("/api/v2/mobile/get_room_by_id")
        Observable<JsonElement> getChatRoom(@Query("token") String token,
                                            @Query("id") int roomId);

        @GET("/api/v2/mobile/load_comments")
        Observable<JsonElement> getComments(@Query("token") String token,
                                            @Query("topic_id") int topicId,
                                            @Query("last_comment_id") int lastCommentId);

        @FormUrlEncoded
        @POST("/api/v2/mobile/post_comment")
        Observable<JsonElement> postComment(@Field("token") String token,
                                            @Field("comment") String message,
                                            @Field("topic_id") int topicId,
                                            @Field("unique_temp_id") String uniqueId);

        @GET("/api/v1/mobile/readnotif/{topic_id}")
        Observable<Void> markTopicAsRead(@Path("topic_id") int topicId,
                                         @Query("token") String token);

        @GET("/api/v2/mobile/sync")
        Observable<JsonElement> sync(@Query("token") String token,
                                     @Query("last_received_comment_id") int lastCommentId);
    }

    private static class CountingFileRequestBody extends RequestBody {
        private final File file;
        private final ProgressListener progressListener;
        private static final int SEGMENT_SIZE = 2048;

        private CountingFileRequestBody(File file, ProgressListener progressListener) {
            this.file = file;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return MediaType.parse("application/octet-stream");
        }

        @Override
        public long contentLength() throws IOException {
            return file.length();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Source source = null;
            try {
                source = Okio.source(file);
                long total = 0;
                long read;

                while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                    total += read;
                    sink.flush();
                    progressListener.onProgress(total);

                }
            } finally {
                Util.closeQuietly(source);
            }
        }

    }

    public interface ProgressListener {
        void onProgress(long total);
    }
}

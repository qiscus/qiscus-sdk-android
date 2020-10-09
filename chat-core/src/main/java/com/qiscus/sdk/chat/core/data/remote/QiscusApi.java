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

package com.qiscus.sdk.chat.core.data.remote;

import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qiscus.sdk.chat.core.BuildConfig;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.R;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.data.model.QUser;
import com.qiscus.sdk.chat.core.data.model.QUserPresence;
import com.qiscus.sdk.chat.core.data.model.QiscusAppConfig;
import com.qiscus.sdk.chat.core.data.model.QiscusChannels;
import com.qiscus.sdk.chat.core.data.model.QiscusNonce;
import com.qiscus.sdk.chat.core.data.model.QiscusRealtimeStatus;
import com.qiscus.sdk.chat.core.event.QMessageSentEvent;
import com.qiscus.sdk.chat.core.event.QiscusClearMessageEvent;
import com.qiscus.sdk.chat.core.util.BuildVersionUtil;
import com.qiscus.sdk.chat.core.util.QiscusDateUtil;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;
import com.qiscus.sdk.chat.core.util.QiscusHashMapUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusApi {
    private OkHttpClient httpClient;
    private Api api;
    private String baseUrl;
    private QiscusCore qiscusCore;

    public QiscusApi(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;

        baseUrl = qiscusCore.getAppServer();

        if (Build.VERSION.SDK_INT <= 19) {
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                    .supportsTlsExtensions(true)
                    .allEnabledTlsVersions()
                    .allEnabledCipherSuites()
                    .build();

            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(this::headersInterceptor)
                    .addInterceptor(makeLoggingInterceptor(qiscusCore.getChatConfig().isEnableLog()))
                    .connectionSpecs(Collections.singletonList(spec))
                    .build();

        } else {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(this::headersInterceptor)
                    .addInterceptor(makeLoggingInterceptor(qiscusCore.getChatConfig().isEnableLog()))
                    .build();
        }

        api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(Api.class);
    }

    public void reInitiateInstance() {
        baseUrl = qiscusCore.getAppServer();

        if (Build.VERSION.SDK_INT <= 19) {
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                    .supportsTlsExtensions(true)
                    .allEnabledTlsVersions()
                    .allEnabledCipherSuites()
                    .build();

            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(this::headersInterceptor)
                    .addInterceptor(makeLoggingInterceptor(qiscusCore.getChatConfig().isEnableLog()))
                    .connectionSpecs(Collections.singletonList(spec))
                    .build();

        } else {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(this::headersInterceptor)
                    .addInterceptor(makeLoggingInterceptor(qiscusCore.getChatConfig().isEnableLog()))
                    .build();
        }

        api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(Api.class);
    }

    private Response headersInterceptor(Interceptor.Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        JSONObject jsonCustomHeader = qiscusCore.getCustomHeader();

        builder.addHeader("QISCUS-SDK-APP-ID", qiscusCore.getAppId());
        builder.addHeader("QISCUS-SDK-TOKEN", qiscusCore.hasSetupUser() ? qiscusCore.getToken() : "");
        builder.addHeader("QISCUS-SDK-USER-EMAIL", qiscusCore.hasSetupUser() ? qiscusCore.getQiscusAccount().getId() : "");
        builder.addHeader("QISCUS-SDK-VERSION", "ANDROID_" + BuildConfig.VERSION_NAME);
        builder.addHeader("QISCUS-SDK-PLATFORM", "ANDROID");
        builder.addHeader("QISCUS-SDK-DEVICE-BRAND", Build.MANUFACTURER);
        builder.addHeader("QISCUS-SDK-DEVICE-MODEL", Build.MODEL);
        builder.addHeader("QISCUS-SDK-DEVICE-OS-VERSION", BuildVersionUtil.OS_VERSION_NAME);

        if (jsonCustomHeader != null) {
            Iterator<String> keys = jsonCustomHeader.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    Object customHeader = jsonCustomHeader.get(key);
                    if (customHeader != null) {
                        builder.addHeader(key, customHeader.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        Request req = builder.build();

        return chain.proceed(req);
    }

    private HttpLoggingInterceptor makeLoggingInterceptor(boolean isDebug) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(isDebug ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        return logging;
    }

    @Deprecated
    public Observable<QiscusNonce> requestNonce() {
        return api.requestNonce().map(QiscusApiParser::parseNonce);
    }

    public Observable<QiscusNonce> getJWTNonce() {
        return api.requestNonce().map(QiscusApiParser::parseNonce);
    }

    @Deprecated
    public Observable<QAccount> login(String token) {
        return api.login(QiscusHashMapUtil.login(token)).map(QiscusApiParser::parseQiscusAccount);
    }

    public Observable<QAccount> setUserWithIdentityToken(String token) {
        return api.login(QiscusHashMapUtil.login(token)).map(QiscusApiParser::parseQiscusAccount);
    }

    @Deprecated
    public Observable<QAccount> loginOrRegister(String email, String password, String username, String avatarUrl) {
        return loginOrRegister(email, password, username, avatarUrl, null);
    }

    @Deprecated
    public Observable<QAccount> loginOrRegister(String email, String password, String username, String avatarUrl, JSONObject extras) {
        return api.loginOrRegister(QiscusHashMapUtil.loginOrRegister(
                email, password, username, avatarUrl, extras == null ? null : extras.toString()
        ))
                .map(QiscusApiParser::parseQiscusAccount);
    }

    public Observable<QAccount> setUser(String userId, String userKey, String username, String avatarURL, JSONObject extras) {
        return api.loginOrRegister(QiscusHashMapUtil.loginOrRegister(
                userId, userKey, username, avatarURL, extras == null ? null : extras.toString()))
                .map(QiscusApiParser::parseQiscusAccount);
    }

    @Deprecated
    public Observable<QAccount> updateProfile(String username, String avatarUrl) {
        return updateProfile(username, avatarUrl, null);
    }

    public Observable<QAccount> updateUser(String name, String avatarURL) {
        return updateUser(name, avatarURL, null);
    }

    @Deprecated
    public Observable<QAccount> updateProfile(String username, String avatarUrl, JSONObject extras) {
        return api.updateProfile(QiscusHashMapUtil.updateProfile(
                username, avatarUrl, extras == null ? null : extras.toString()))
                .map(QiscusApiParser::parseQiscusAccount);
    }

    public Observable<QAccount> updateUser(String name, String avatarURL, JSONObject extras) {
        return api.updateProfile(QiscusHashMapUtil.updateProfile(
                name, avatarURL, extras == null ? null : extras.toString()))
                .map(QiscusApiParser::parseQiscusAccount);
    }

    public Observable<QAccount> getUserData() {
        return api.getUserData()
                .map(QiscusApiParser::parseQiscusAccount);
    }

    @Deprecated
    public Observable<QChatRoom> getChatRoom(String withEmail, JSONObject options) {
        return api.createOrGetChatRoom(QiscusHashMapUtil.getChatRoom(Collections.singletonList(withEmail),
                options == null ? null : options.toString()))
                .map(QiscusApiParser::parseQiscusChatRoom);
    }

    public Observable<QChatRoom> chatUser(String userId, JSONObject extras) {
        return api.createOrGetChatRoom(QiscusHashMapUtil.getChatRoom(Collections.singletonList(userId),
                extras == null ? null : extras.toString()))
                .map(QiscusApiParser::parseQiscusChatRoom);
    }

    @Deprecated
    public Observable<QChatRoom> createGroupChatRoom(String name, List<String> emails, String avatarUrl, JSONObject options) {
        return api.createGroupChatRoom(QiscusHashMapUtil.createGroupChatRoom(
                name, emails, avatarUrl, options == null ? null : options.toString()))
                .map(QiscusApiParser::parseQiscusChatRoom);
    }

    public Observable<QChatRoom> createGroupChat(String name, List<String> userIds, String avatarURL, JSONObject extras) {
        return api.createGroupChatRoom(QiscusHashMapUtil.createGroupChatRoom(
                name, userIds, avatarURL, extras == null ? null : extras.toString()))
                .map(QiscusApiParser::parseQiscusChatRoom);
    }

    @Deprecated
    public Observable<QChatRoom> getGroupChatRoom(String uniqueId, String name, String avatarUrl, JSONObject options) {
        return api.createOrGetGroupChatRoom(QiscusHashMapUtil.getGroupChatRoom(
                uniqueId, name, avatarUrl, options == null ? null : options.toString()))
                .map(QiscusApiParser::parseQiscusChatRoom);
    }

    public Observable<QChatRoom> createChannel(String uniqueId, String name, String avatarURL, JSONObject extras) {
        return api.createOrGetGroupChatRoom(QiscusHashMapUtil.getGroupChatRoom(
                uniqueId, name, avatarURL, extras == null ? null : extras.toString()))
                .map(QiscusApiParser::parseQiscusChatRoom);
    }

    public Observable<QChatRoom> getChannel(String uniqueId) {
        return api.createOrGetGroupChatRoom(QiscusHashMapUtil.getGroupChatRoom(
                uniqueId, null, null, null))
                .map(QiscusApiParser::parseQiscusChatRoom);
    }

    @Deprecated
    public Observable<QChatRoom> getChatRoom(long roomId) {
        return api.getChatRooms(QiscusHashMapUtil.getChatRooms(
                Collections.singletonList(String.valueOf(roomId)), new ArrayList<>(), true, false))
                .map(QiscusApiParser::parseQiscusChatRoomInfo)
                .flatMap(Observable::from)
                .take(1);
    }

    public Observable<QChatRoom> getChatRoomInfo(long roomId) {
        return api.getChatRooms(QiscusHashMapUtil.getChatRooms(
                Collections.singletonList(String.valueOf(roomId)), new ArrayList<>(), true, false))
                .map(QiscusApiParser::parseQiscusChatRoomInfo)
                .flatMap(Observable::from)
                .take(1);
    }

    @Deprecated
    public Observable<Pair<QChatRoom, List<QMessage>>> getChatRoomComments(long roomId) {
        return api.getChatRoom(roomId)
                .map(QiscusApiParser::parseQiscusChatRoomWithComments);
    }

    public Observable<Pair<QChatRoom, List<QMessage>>> getChatRoomWithMessages(long roomId) {
        return api.getChatRoom(roomId)
                .map(QiscusApiParser::parseQiscusChatRoomWithComments);
    }

    @Deprecated
    public Observable<List<QChatRoom>> getChatRooms(int page, int limit, boolean showMembers) {
        return api.getChatRooms(page, limit, showMembers, false, false)
                .map(QiscusApiParser::parseQiscusChatRoomInfo);
    }

    public Observable<List<QChatRoom>> getAllChatRooms(boolean showParticipant, boolean showRemoved,
                                                       boolean showEmpty, int page, int limit) {
        return api.getChatRooms(page, limit, showParticipant, showEmpty, showRemoved)
                .map(QiscusApiParser::parseQiscusChatRoomInfo);
    }

    @Deprecated
    public Observable<List<QChatRoom>> getChatRooms(List<Long> roomIds, List<String> uniqueIds, boolean showMembers) {
        List<String> listOfRoomIds = new ArrayList<>();
        for (Long roomId : roomIds) {
            listOfRoomIds.add(String.valueOf(roomId));
        }
        return api.getChatRooms(QiscusHashMapUtil.getChatRooms(
                listOfRoomIds, uniqueIds, showMembers, false))
                .map(QiscusApiParser::parseQiscusChatRoomInfo);
    }

    public Observable<List<QChatRoom>> getChatRoomsWithUniqueIds(List<String> uniqueIds,
                                                                 boolean showRemoved, boolean showParticipant) {
        return api.getChatRooms(QiscusHashMapUtil.getChatRooms(
                null, uniqueIds, showParticipant, showRemoved))
                .map(QiscusApiParser::parseQiscusChatRoomInfo);
    }

    public Observable<List<QChatRoom>> getChatRooms(List<Long> roomIds, boolean showRemoved, boolean showParticipant) {
        List<String> listOfRoomIds = new ArrayList<>();
        for (Long roomId : roomIds) {
            listOfRoomIds.add(String.valueOf(roomId));
        }
        return api.getChatRooms(QiscusHashMapUtil.getChatRooms(
                listOfRoomIds, null, showParticipant, showRemoved))
                .map(QiscusApiParser::parseQiscusChatRoomInfo);
    }

    @Deprecated
    public Observable<QMessage> getComments(long roomId, long lastCommentId) {
        Long lastCommentId1 = lastCommentId;
        if (lastCommentId1 == 0) {
            lastCommentId1 = null;
        }
        return api.getComments(roomId, lastCommentId1, false, 20)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> QiscusApiParser.parseQMessage(jsonElement, roomId));
    }

    @Deprecated
    public Observable<QMessage> getCommentsAfter(long roomId, long lastCommentId) {
        Long lastCommentId1 = lastCommentId;
        if (lastCommentId1 == 0) {
            lastCommentId1 = null;
        }
        return api.getComments(roomId, lastCommentId1, true, 20)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> QiscusApiParser.parseQMessage(jsonElement, roomId));
    }

    public Observable<QMessage> getPreviousMessagesById(long roomId, int limit, long messageId) {
        Long messageId1 = messageId;
        if (messageId1 == 0) {
            messageId1 = null;
        }
        return api.getComments(roomId, messageId1, false, limit)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> QiscusApiParser.parseQMessage(jsonElement, roomId));
    }

    public Observable<QMessage> getNextMessagesById(long roomId, int limit, long messageId) {
        Long messageId1 = messageId;
        if (messageId1 == 0) {
            messageId1 = null;
        }
        return api.getComments(roomId, messageId1, true, limit)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> QiscusApiParser.parseQMessage(jsonElement, roomId));
    }

    @Deprecated
    public Observable<QMessage> postComment(QMessage qMessage) {
        qiscusCore.getChatConfig().getCommentSendingInterceptor().sendComment(qMessage);
        return api.postComment(QiscusHashMapUtil.postComment(qMessage))
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject()
                            .get("results").getAsJsonObject().get("comment").getAsJsonObject();
                    qMessage.setId(jsonComment.get("id").getAsLong());
                    qMessage.setPreviousMessageId(jsonComment.get("comment_before_id").getAsInt());

                    //timestamp is in nano seconds format, convert it to milliseconds by divide it
                    long timestamp = jsonComment.get("unix_nano_timestamp").getAsLong() / 1000000L;
                    qMessage.setTimestamp(new Date(timestamp));
                    qiscusCore.getLogger().print("Sent Comment...");
                    return qMessage;
                })
                .doOnNext(comment -> EventBus.getDefault().post(new QMessageSentEvent(comment)));
    }

    public Observable<QMessage> sendMessage(QMessage message) {
        qiscusCore.getChatConfig().getCommentSendingInterceptor().sendComment(message);
        return api.postComment(QiscusHashMapUtil.postComment(message))
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject()
                            .get("results").getAsJsonObject().get("comment").getAsJsonObject();
                    message.setId(jsonComment.get("id").getAsLong());
                    message.setPreviousMessageId(jsonComment.get("comment_before_id").getAsInt());

                    //timestamp is in nano seconds format, convert it to milliseconds by divide it
                    long timestamp = jsonComment.get("unix_nano_timestamp").getAsLong() / 1000000L;
                    message.setTimestamp(new Date(timestamp));

                    qiscusCore.getLogger().print("Sent Comment...");
                    return message;
                })
                .doOnNext(comment -> EventBus.getDefault().post(new QMessageSentEvent(comment)));
    }

    public Observable<QMessage> sendFileMessage(QMessage message, File file, ProgressListener progressUploadListener) {
        return Observable.create(subscriber -> {
            long fileLength = file.length();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(),
                            new CountingFileRequestBody(file, totalBytes -> {
                                int progress = (int) (totalBytes * 100 / fileLength);
                                progressUploadListener.onProgress(progress);
                            }))
                    .build();

            Request request = new Request.Builder()
                    .url(baseUrl + "api/v2/mobile/upload")
                    .post(requestBody).build();

            try {
                Response response = httpClient.newCall(request).execute();
                JSONObject responseJ = new JSONObject(response.body().string());
                String result = responseJ.getJSONObject("results").getJSONObject("file").getString("url");
                message.updateAttachmentUrl(Uri.parse(result).toString());
                qiscusCore.getDataStore().addOrUpdate(message);

                sendMessage(message)
                        .doOnSubscribe(() -> qiscusCore.getDataStore().addOrUpdate(message))
                        .doOnError(throwable -> {
                            subscriber.onError(throwable);
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(commentSend -> {
                            subscriber.onNext(commentSend);
                            subscriber.onCompleted();
                        }, throwable -> {
                            qiscusCore.getErrorLogger().print(throwable);
                            throwable.printStackTrace();
                            subscriber.onError(throwable);
                        });


            } catch (IOException | JSONException e) {
                qiscusCore.getErrorLogger().print("UploadFile", e);
                subscriber.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);

    }

    @Deprecated
    public Observable<QMessage> sync(long lastCommentId) {
        return api.sync(lastCommentId)
                .onErrorReturn(throwable -> {
                    qiscusCore.getErrorLogger().print("Sync", throwable);
                    return null;
                })
                .filter(jsonElement -> jsonElement != null)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject();
                    return QiscusApiParser.parseQMessage(jsonElement, jsonComment.get("room_id").getAsLong());
                });
    }

    public Observable<QMessage> synchronize(long lastMessageId) {
        return api.sync(lastMessageId)
                .onErrorReturn(throwable -> {
                    qiscusCore.getErrorLogger().print("Sync", throwable);
                    return null;
                })
                .filter(jsonElement -> jsonElement != null)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject();
                    return QiscusApiParser.parseQMessage(jsonElement, jsonComment.get("room_id").getAsLong());
                });
    }

    public Observable<QMessage> sync() {
        QMessage latestComment = qiscusCore.getDataStore().getLatestComment();
        if (latestComment == null || !qiscusCore.getApps().getString(R.string.qiscus_today)
                .equals(QiscusDateUtil.toTodayOrDate(latestComment.getTimestamp()))) {
            return Observable.empty();
        }
        return synchronize(latestComment.getId());
    }

    @Deprecated
    public Observable<Uri> uploadFile(File file, ProgressListener progressListener) {
        return Observable.create(subscriber -> {
            long fileLength = file.length();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(),
                            new CountingFileRequestBody(file, totalBytes -> {
                                int progress = (int) (totalBytes * 100 / fileLength);
                                progressListener.onProgress(progress);
                            }))
                    .build();

            Request request = new Request.Builder()
                    .url(baseUrl + "api/v2/mobile/upload")
                    .post(requestBody).build();

            try {
                Response response = httpClient.newCall(request).execute();
                JSONObject responseJ = new JSONObject(response.body().string());
                String result = responseJ.getJSONObject("results").getJSONObject("file").getString("url");

                subscriber.onNext(Uri.parse(result));
                subscriber.onCompleted();
            } catch (IOException | JSONException e) {
                qiscusCore.getErrorLogger().print("UploadFile", e);
                subscriber.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    public Observable<Uri> upload(File file, ProgressListener progressListener) {
        return Observable.create(subscriber -> {
            long fileLength = file.length();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(),
                            new CountingFileRequestBody(file, totalBytes -> {
                                int progress = (int) (totalBytes * 100 / fileLength);
                                progressListener.onProgress(progress);
                            }))
                    .build();

            Request request = new Request.Builder()
                    .url(baseUrl + "api/v2/mobile/upload")
                    .post(requestBody).build();

            try {
                Response response = httpClient.newCall(request).execute();
                JSONObject responseJ = new JSONObject(response.body().string());
                String result = responseJ.getJSONObject("results").getJSONObject("file").getString("url");

                subscriber.onNext(Uri.parse(result));
                subscriber.onCompleted();
            } catch (IOException | JSONException e) {
                qiscusCore.getErrorLogger().print("UploadFile", e);
                subscriber.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    public Observable<File> downloadFile(String url, String fileName, ProgressListener progressListener) {
        return Observable.create(subscriber -> {
            InputStream inputStream = null;
            FileOutputStream fos = null;
            try {
                Request request = new Request.Builder().url(url).build();

                Response response = httpClient.newCall(request).execute();

                File output = new File(QiscusFileUtil.generateFilePath(fileName));
                fos = new FileOutputStream(output.getPath());
                if (!response.isSuccessful()) {
                    throw new IOException();
                } else {
                    ResponseBody responseBody = response.body();
                    long fileLength = responseBody.contentLength();

                    inputStream = responseBody.byteStream();
                    byte[] buffer = new byte[4096];
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

                    subscriber.onNext(output);
                    subscriber.onCompleted();
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
                    //Do nothing
                }
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    public Observable<QChatRoom> updateChatRoom(long roomId, String name, String avatarURL, JSONObject extras) {
        return api.updateChatRoom(QiscusHashMapUtil.updateChatRoom(
                String.valueOf(roomId), name, avatarURL, extras == null ? null : extras.toString()))
                .map(QiscusApiParser::parseQiscusChatRoom)
                .doOnNext(qiscusChatRoom -> qiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));
    }

    public Observable<Void> updateCommentStatus(long roomId, long lastReadId, long lastReceivedId) {
        return api.updateCommentStatus(QiscusHashMapUtil.updateCommentStatus(
                String.valueOf(roomId), String.valueOf(lastReadId), String.valueOf(lastReceivedId)))
                .map(jsonElement -> null);
    }

    @Deprecated
    public Observable<Void> registerFcmToken(String fcmToken) {
        return api.registerFcmToken(QiscusHashMapUtil.registerOrRemoveFcmToken(fcmToken))
                .map(jsonElement -> null);
    }

    public Observable<Void> registerDeviceToken(String token) {
        return api.registerFcmToken(QiscusHashMapUtil.registerOrRemoveFcmToken(token))
                .map(jsonElement -> null);
    }

    public Observable<Void> removeDeviceToken(String token) {
        return api.removeDeviceToken(QiscusHashMapUtil.registerOrRemoveFcmToken(token))
                .map(jsonElement -> null);
    }

    @Deprecated
    public Observable<Void> clearCommentsByRoomIds(List<Long> roomIds) {
        List<String> listOfRoomIds = new ArrayList<>();
        for (Long roomId : roomIds) {
            listOfRoomIds.add(String.valueOf(roomId));
        }
        return api.getChatRooms(QiscusHashMapUtil.getChatRooms(
                listOfRoomIds, null, false, false))
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("results").getAsJsonObject())
                .map(jsonObject -> jsonObject.get("rooms_info").getAsJsonArray())
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("unique_id").getAsString())
                .toList()
                .flatMap(this::clearCommentsByRoomUniqueIds);
    }

    @Deprecated
    public Observable<Void> clearCommentsByRoomUniqueIds(List<String> roomUniqueIds) {
        return api.clearChatRoomMessages(roomUniqueIds)
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.get("results").getAsJsonObject())
                .map(jsonResults -> jsonResults.get("rooms").getAsJsonArray())
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .doOnNext(json -> {
                    long roomId = json.get("id").getAsLong();
                    if (qiscusCore.getDataStore().deleteCommentsByRoomId(roomId)) {
                        EventBus.getDefault().post(new QiscusClearMessageEvent(roomId));
                    }
                })
                .toList()
                .map(qiscusChatRooms -> null);
    }

    public Observable<Void> clearMessagesByChatRoomIds(List<Long> roomIds) {
        List<String> listOfRoomIds = new ArrayList<>();
        for (Long roomId : roomIds) {
            listOfRoomIds.add(String.valueOf(roomId));
        }
        return api.getChatRooms(QiscusHashMapUtil.getChatRooms(
                listOfRoomIds, null, false, false))
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("results").getAsJsonObject())
                .map(jsonObject -> jsonObject.get("rooms_info").getAsJsonArray())
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("unique_id").getAsString())
                .toList()
                .flatMap(this::clearMessagesByChatRoomUniqueIds);
    }

    public Observable<Void> clearMessagesByChatRoomUniqueIds(List<String> roomUniqueIds) {
        return api.clearChatRoomMessages(roomUniqueIds)
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.get("results").getAsJsonObject())
                .map(jsonResults -> jsonResults.get("rooms").getAsJsonArray())
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .doOnNext(json -> {
                    long roomId = json.get("id").getAsLong();
                    if (qiscusCore.getDataStore().deleteCommentsByRoomId(roomId)) {
                        EventBus.getDefault().post(new QiscusClearMessageEvent(roomId));
                    }
                })
                .toList()
                .map(qiscusChatRooms -> null);
    }

    @Deprecated
    public Observable<List<QMessage>> deleteComments(List<String> commentUniqueIds,
                                                     boolean isHardDelete) {
        // isDeleteForEveryone => akan selalu true, karena deleteForMe deprecated
        return api.deleteComments(commentUniqueIds, true, isHardDelete)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject();
                    return QiscusApiParser.parseQMessage(jsonElement, jsonComment.get("room_id").getAsLong());
                })
                .toList()
                .doOnNext(comments -> {
                    QAccount account = qiscusCore.getQiscusAccount();
                    QParticipant actor = new QParticipant();
                    actor.setId(account.getId());
                    actor.setName(account.getName());
                    actor.setAvatarUrl(account.getAvatarUrl());

                    List<QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment> deletedComments = new ArrayList<>();
                    for (QMessage comment : comments) {
                        deletedComments.add(new QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment(comment.getChatRoomId(),
                                comment.getUniqueId()));
                    }

                    QiscusDeleteCommentHandler.DeletedCommentsData deletedCommentsData
                            = new QiscusDeleteCommentHandler.DeletedCommentsData();
                    deletedCommentsData.setActor(actor);
                    deletedCommentsData.setHardDelete(isHardDelete);
                    deletedCommentsData.setDeletedComments(deletedComments);

                    qiscusCore.getDeleteCommentHandler().handle(deletedCommentsData);
                });
    }

    public Observable<List<QMessage>> deleteMessages(List<String> messageUniqueIds) {
        return api.deleteComments(messageUniqueIds, true, true)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject();
                    return QiscusApiParser.parseQMessage(jsonElement, jsonComment.get("room_id").getAsLong());
                })
                .toList()
                .doOnNext(comments -> {
                    QAccount account = qiscusCore.getQiscusAccount();
                    QParticipant actor = new QParticipant();
                    actor.setId(account.getId());
                    actor.setName(account.getName());
                    actor.setAvatarUrl(account.getAvatarUrl());

                    List<QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment> deletedComments = new ArrayList<>();
                    for (QMessage comment : comments) {
                        deletedComments.add(new QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment(comment.getChatRoomId(),
                                comment.getUniqueId()));
                    }

                    QiscusDeleteCommentHandler.DeletedCommentsData deletedCommentsData
                            = new QiscusDeleteCommentHandler.DeletedCommentsData();
                    deletedCommentsData.setActor(actor);
                    deletedCommentsData.setHardDelete(true);
                    deletedCommentsData.setDeletedComments(deletedComments);

                    qiscusCore.getDeleteCommentHandler().handle(deletedCommentsData);
                });
    }

    @Deprecated
    public Observable<List<JSONObject>> getEvents(long startEventId) {
        return api.getEvents(startEventId)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("events").getAsJsonArray()))
                .map(jsonEvent -> {
                    try {
                        return new JSONObject(jsonEvent.toString());
                    } catch (JSONException e) {
                        return null;
                    }
                })
                .filter(jsonObject -> jsonObject != null)
                .doOnNext(qiscusCore.getPusherApi()::handleNotification)
                .toList();
    }

    public Observable<List<JSONObject>> synchronizeEvent(long lastEventId) {
        return api.getEvents(lastEventId)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("events").getAsJsonArray()))
                .map(jsonEvent -> {
                    try {
                        return new JSONObject(jsonEvent.toString());
                    } catch (JSONException e) {
                        return null;
                    }
                })
                .filter(jsonObject -> jsonObject != null)
                .doOnNext(qiscusCore.getPusherApi()::handleNotification)
                .toList();
    }

    public Observable<Long> getTotalUnreadCount() {
        return api.getTotalUnreadCount()
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.get("results").getAsJsonObject())
                .map(jsonResults -> jsonResults.get("total_unread_count").getAsLong());
    }

    @Deprecated
    public Observable<QChatRoom> addRoomMember(long roomId, List<String> emails) {
        return api.addRoomMember(QiscusHashMapUtil.addRoomMember(String.valueOf(roomId), emails))
                .flatMap(jsonElement -> getChatRoomInfo(roomId));
    }

    public Observable<QChatRoom> addParticipants(long roomId, List<String> userIds) {
        return api.addRoomMember(QiscusHashMapUtil.addRoomMember(String.valueOf(roomId), userIds))
                .flatMap(jsonElement -> getChatRoomInfo(roomId));
    }

    @Deprecated
    public Observable<QChatRoom> removeRoomMember(long roomId, List<String> userIds) {
        return api.removeRoomMember(QiscusHashMapUtil.removeRoomMember(String.valueOf(roomId), userIds))
                .flatMap(jsonElement -> getChatRoomInfo(roomId));
    }

    public Observable<QChatRoom> removeParticipants(long roomId, List<String> userIds) {
        return api.removeRoomMember(QiscusHashMapUtil.removeRoomMember(String.valueOf(roomId), userIds))
                .flatMap(jsonElement -> getChatRoomInfo(roomId));
    }


    public Observable<QUser> blockUser(String userId) {
        return api.blockUser(QiscusHashMapUtil.blockUser(userId))
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.getAsJsonObject("results"))
                .map(jsonResults -> jsonResults.getAsJsonObject("user"))
                .map(jsonAccount -> QiscusApiParser.parseQUser(jsonAccount));
    }

    public Observable<QUser> unblockUser(String userId) {
        return api.unblockUser(QiscusHashMapUtil.unblockUser(userId))
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.getAsJsonObject("results"))
                .map(jsonResults -> jsonResults.getAsJsonObject("user"))
                .map(jsonAccount -> QiscusApiParser.parseQUser(jsonAccount));
    }

    public Observable<List<QUser>> getBlockedUsers() {
        return getBlockedUsers(0, 100);
    }

    public Observable<List<QUser>> getBlockedUsers(long page, long limit) {
        return api.getBlockedUsers(page, limit)
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.getAsJsonObject("results"))
                .map(jsonResults -> jsonResults.getAsJsonArray("blocked_users"))
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .map(jsonAccount -> QiscusApiParser.parseQUser(jsonAccount))
                .toList();
    }

    @Deprecated
    public Observable<List<QParticipant>> getRoomMembers(String roomUniqueId, int offset,
                                                         MetaRoomMembersListener metaRoomMembersListener) {
        return getRoomMembers(roomUniqueId, offset, null, metaRoomMembersListener);
    }

    @Deprecated
    public Observable<List<QParticipant>> getRoomMembers(String roomUniqueId, int offset, String sorting,
                                                         MetaRoomMembersListener metaRoomMembersListener) {
        return api.getRoomParticipants(roomUniqueId, 0, 0, offset, sorting)
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.getAsJsonObject("results"))
                .doOnNext(jsonResults -> {
                    JsonObject meta = jsonResults.getAsJsonObject("meta");
                    if (metaRoomMembersListener != null) {
                        metaRoomMembersListener.onMetaReceived(
                                meta.get("current_offset").getAsInt(),
                                meta.get("per_page").getAsInt(),
                                meta.get("total").getAsInt()
                        );
                    }
                })
                .map(jsonResults -> jsonResults.getAsJsonArray("participants"))
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .map(QiscusApiParser::parseQiscusRoomMember)
                .toList();
    }

    public Observable<List<QParticipant>> getParticipants(String roomUniqueId, int page, int limit,
                                                          String sorting, MetaRoomParticipantsListener metaRoomParticipantListener) {
        return api.getRoomParticipants(roomUniqueId, page, limit, 0,
                sorting)
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.getAsJsonObject("results"))
                .doOnNext(jsonResults -> {
                    JsonObject meta = jsonResults.getAsJsonObject("meta");
                    if (metaRoomParticipantListener != null) {
                        metaRoomParticipantListener.onMetaReceived(
                                meta.get("current_page").getAsInt(),
                                meta.get("per_page").getAsInt(),
                                meta.get("total").getAsInt()
                        );
                    }
                })
                .map(jsonResults -> jsonResults.getAsJsonArray("participants"))
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .map(QiscusApiParser::parseQiscusRoomMember)
                .toList();
    }

    public Observable<List<QParticipant>> getParticipants(String roomUniqueId, int page, int limit, String sorting) {
        return api.getRoomParticipants(roomUniqueId, page, limit, 0, sorting)
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.getAsJsonObject("results"))
                .doOnNext(jsonResults -> {

                })
                .map(jsonResults -> jsonResults.getAsJsonArray("participants"))
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .map(QiscusApiParser::parseQiscusRoomMember)
                .toList();
    }

    public Observable<String> getMqttBaseUrl() {
        return Observable.create(subscriber -> {
            OkHttpClient httpClientLB;
            if (Build.VERSION.SDK_INT <= 19) {
                ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                        .supportsTlsExtensions(true)
                        .allEnabledTlsVersions()
                        .allEnabledCipherSuites()
                        .build();

                httpClientLB = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .addInterceptor(this::headersInterceptor)
                        .addInterceptor(makeLoggingInterceptor(qiscusCore.getChatConfig().isEnableLog()))
                        .connectionSpecs(Collections.singletonList(spec))
                        .build();

            } else {
                httpClientLB = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .addInterceptor(this::headersInterceptor)
                        .addInterceptor(makeLoggingInterceptor(qiscusCore.getChatConfig().isEnableLog()))
                        .build();
            }

            String url = qiscusCore.getBaseURLLB();
            Request okHttpRequest = new Request.Builder().url(url).build();
            try {
                Response response = httpClientLB.newCall(okHttpRequest).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                String node = jsonResponse.getString("node");
                subscriber.onNext(node);
                subscriber.onCompleted();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    public Observable<List<QUser>> getUsers(String searchUsername) {
        return getUsers(searchUsername, 0, 100);
    }

    @Deprecated
    public Observable<List<QUser>> getUsers(long page, long limit, String query) {
        return api.getUserList(page, limit, "username asc", query)
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.getAsJsonObject("results"))
                .map(jsonResults -> jsonResults.getAsJsonArray("users"))
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .map(jsonAccount -> QiscusApiParser.parseQUser(jsonAccount))
                .toList();
    }

    public Observable<List<QUser>> getUsers(String searchUsername, long page, long limit) {
        return api.getUserList(page, limit, "username asc", searchUsername)
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.getAsJsonObject("results"))
                .map(jsonResults -> jsonResults.getAsJsonArray("users"))
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .map(jsonAccount -> QiscusApiParser.parseQUser(jsonAccount))
                .toList();
    }

    public Observable<Void> eventReport(String moduleName, String event, String message) {
        return api.eventReport(QiscusHashMapUtil.eventReport(moduleName, event, message))
                .map(jsonElement -> null);
    }

    public Observable<QiscusAppConfig> getAppConfig() {
        return api.getAppConfig()
                .map(QiscusApiParser::parseQiscusAppConfig);

    }

    public Observable<QiscusRealtimeStatus> getRealtimeStatus(String topic) {
        return api.getRealtimeStatus(QiscusHashMapUtil.getRealtimeStatus(topic))
                .map(QiscusApiParser::parseQiscusRealtimeStatus);
    }

    public Observable<List<QiscusChannels>> getChannels() {
        return api.getChannels()
                .map(QiscusApiParser::parseQiscusChannels);
    }

    public Observable<List<QiscusChannels>> getChannelsInfo(List<String> uniqueIds) {
        return api.getChannelsInfo(QiscusHashMapUtil.getChannelsInfo(uniqueIds))
                .map(QiscusApiParser::parseQiscusChannels);
    }

    public Observable<List<QiscusChannels>> joinChannels(List<String> uniqueIds) {
        return api.joinChannels(QiscusHashMapUtil.joinChannels(uniqueIds))
                .map(QiscusApiParser::parseQiscusChannels);
    }

    public Observable<List<QiscusChannels>> leaveChannels(List<String> uniqueIds) {
        return api.leaveChannels(QiscusHashMapUtil.leaveChannels(uniqueIds))
                .map(QiscusApiParser::parseQiscusChannels);
    }

    public Observable<List<QUserPresence>> getUsersPresence(List<String> userIds) {
        return api.getUsersPresence(QiscusHashMapUtil.getUsersPresence(userIds))
                .map(QiscusApiParser::parseQUsersPresence);
    }

    public Observable<Long> getRoomUnreadCount() {
        return api.getRoomUnreadCount(qiscusCore.getToken())
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.getAsJsonObject("results"))
                .map(jsonResults -> jsonResults.get("total_unread_count").getAsLong());

    }

    private interface Api {

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/event_report")
        Observable<JsonElement> eventReport(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/auth/nonce")
        Observable<JsonElement> requestNonce();

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/auth/verify_identity_token")
        Observable<JsonElement> login(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/login_or_register")
        Observable<JsonElement> loginOrRegister(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @PATCH("api/v2/mobile/my_profile")
        Observable<JsonElement> updateProfile(
                @Body HashMap<String, Object> data
        );

        @GET("api/v2/mobile/my_profile")
        Observable<JsonElement> getUserData(
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/get_or_create_room_with_target")
        Observable<JsonElement> createOrGetChatRoom(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/create_room")
        Observable<JsonElement> createGroupChatRoom(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/get_or_create_room_with_unique_id")
        Observable<JsonElement> createOrGetGroupChatRoom(
                @Body HashMap<String, Object> data
        );

        @GET("api/v2/mobile/get_room_by_id")
        Observable<JsonElement> getChatRoom(
                @Query("id") long roomId
        );

        @GET("api/v2/mobile/load_comments")
        Observable<JsonElement> getComments(
                @Query("topic_id") long roomId,
                @Query("last_comment_id") long lastCommentId,
                @Query("after") boolean after,
                @Query("limit") int limit
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/post_comment")
        Observable<JsonElement> postComment(
                @Body HashMap<String, Object> data
        );

        @GET("api/v2/mobile/sync")
        Observable<JsonElement> sync(
                @Query("last_received_comment_id") long lastCommentId
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/update_room")
        Observable<JsonElement> updateChatRoom(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/update_comment_status")
        Observable<JsonElement> updateCommentStatus(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/set_user_device_token")
        Observable<JsonElement> registerFcmToken(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/remove_user_device_token")
        Observable<JsonElement> removeDeviceToken(
                @Body HashMap<String, Object> data
        );

        @GET("api/v2/mobile/user_rooms")
        Observable<JsonElement> getChatRooms(
                @Query("page") int page,
                @Query("limit") int limit,
                @Query("show_participants") boolean showParticipants,
                @Query("show_empty") boolean showEmpty,
                @Query("show_removed") boolean showRemoved
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/rooms_info")
        Observable<JsonElement> getChatRooms(
                @Body HashMap<String, Object> data
        );

        @DELETE("api/v2/mobile/clear_room_messages")
        Observable<JsonElement> clearChatRoomMessages(
                @Query("room_channel_ids[]") List<String> roomUniqueIds
        );

        @DELETE("api/v2/mobile/delete_messages")
        Observable<JsonElement> deleteComments(
                @Query("unique_ids[]") List<String> commentUniqueIds,
                @Query("is_delete_for_everyone") boolean isDeleteForEveryone,
                @Query("is_hard_delete") boolean isHardDelete
        );

        @GET("api/v2/mobile/sync_event")
        Observable<JsonElement> getEvents(
                @Query("start_event_id") long startEventId
        );

        @GET("api/v2/mobile/total_unread_count")
        Observable<JsonElement> getTotalUnreadCount();

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/add_room_participants")
        Observable<JsonElement> addRoomMember(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/remove_room_participants")
        Observable<JsonElement> removeRoomMember(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("/api/v2/mobile/block_user")
        Observable<JsonElement> blockUser(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("/api/v2/mobile/unblock_user")
        Observable<JsonElement> unblockUser(
                @Body HashMap<String, Object> data
        );

        @GET("/api/v2/mobile/get_blocked_users")
        Observable<JsonElement> getBlockedUsers(
                @Query("page") long page,
                @Query("limit") long limit
        );

        @GET("/api/v2/mobile/room_participants")
        Observable<JsonElement> getRoomParticipants(
                @Query("room_unique_id") String roomUniqId,
                @Query("page") int page,
                @Query("limit") int limit,
                @Query("offset") int offset,
                @Query("sorting") String sorting
        );

        @GET("/api/v2/mobile/get_user_list")
        Observable<JsonElement> getUserList(
                @Query("page") long page,
                @Query("limit") long limit,
                @Query("order_query") String orderQuery,
                @Query("query") String query
        );

        @GET("api/v2/mobile/config")
        Observable<JsonElement> getAppConfig();

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/realtime")
        Observable<JsonElement> getRealtimeStatus(
                @Body HashMap<String, Object> data
        );

        @GET("api/v2/mobile/channels")
        Observable<JsonElement> getChannels();

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/channels/info")
        Observable<JsonElement> getChannelsInfo(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/channels/join")
        Observable<JsonElement> joinChannels(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/channels/leave")
        Observable<JsonElement> leaveChannels(
                @Body HashMap<String, Object> data
        );

        @Headers("Content-Type: application/json")
        @POST("api/v2/mobile/users/status")
        Observable<JsonElement> getUsersPresence(
                @Body HashMap<String, Object> data
        );

        @GET("api/v2/mobile/get_room_unread_count")
        Observable<JsonElement> getRoomUnreadCount(
                @Query("token") String token
        );
    }

    public interface MetaRoomMembersListener {
        void onMetaReceived(int currentOffset, int perPage, int total);
    }

    public interface MetaRoomParticipantsListener {
        void onMetaReceived(int currentPage, int perPage, int total);
    }

    public interface ProgressListener {
        void onProgress(long total);
    }

    private static class CountingFileRequestBody extends RequestBody {
        private static final int SEGMENT_SIZE = 2048;
        private static final int IGNORE_FIRST_NUMBER_OF_WRITE_TO_CALL = 0;
        private final File file;
        private final ProgressListener progressListener;
        private int numWriteToCall = -1;

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
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
            numWriteToCall++;

            Source source = null;
            try {
                source = Okio.source(file);
                long total = 0;
                long read;

                while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                    total += read;
                    sink.flush();

                    /**
                     * When we use HttpLoggingInterceptor,
                     * we have issue with progress update not valid.
                     * So we must check, first call is to HttpLoggingInterceptor
                     * second call is to request
                     */
                    if (numWriteToCall > IGNORE_FIRST_NUMBER_OF_WRITE_TO_CALL) {
                        progressListener.onProgress(total);
                    }

                }
            } finally {
                Util.closeQuietly(source);
            }
        }
    }
}
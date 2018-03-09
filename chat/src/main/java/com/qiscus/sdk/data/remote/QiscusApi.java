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
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.QiscusDeleteCommentHandler;
import com.qiscus.sdk.data.QiscusEncryptionHandler;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusNonce;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.event.QiscusClearCommentsEvent;
import com.qiscus.sdk.event.QiscusCommentSentEvent;
import com.qiscus.sdk.util.QiscusDateUtil;
import com.qiscus.sdk.util.QiscusErrorLogger;
import com.qiscus.sdk.util.QiscusFileUtil;
import com.qiscus.sdk.util.QiscusLogger;
import com.qiscus.sdk.util.QiscusTextUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
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
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Emitter;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public enum QiscusApi {
    INSTANCE;
    private final OkHttpClient httpClient;

    private String baseUrl;
    private final Api api;

    QiscusApi() {
        baseUrl = Qiscus.getAppServer();

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(this::headersInterceptor)
                .build();

        api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(Api.class);
    }

    private Response headersInterceptor(Interceptor.Chain chain) throws IOException {
        Request req = chain.request().newBuilder()
                .addHeader("QISCUS_SDK_APP_ID", Qiscus.getAppId())
                .addHeader("QISCUS_SDK_TOKEN", Qiscus.hasSetupUser() ? Qiscus.getToken() : "")
                .addHeader("QISCUS_SDK_USER_EMAIL", Qiscus.hasSetupUser() ? Qiscus.getQiscusAccount().getEmail() : "")
                .build();
        return chain.proceed(req);
    }

    public static QiscusApi getInstance() {
        return INSTANCE;
    }

    public Observable<QiscusNonce> requestNonce() {
        return api.requestNonce().map(QiscusApiParser::parseNonce);
    }

    public Observable<QiscusAccount> login(String token) {
        return api.login(token).map(QiscusApiParser::parseQiscusAccount);
    }

    public Observable<QiscusAccount> loginOrRegister(String email, String password, String username, String avatarUrl) {
        return api.loginOrRegister(email, password, username, avatarUrl)
                .map(QiscusApiParser::parseQiscusAccount);
    }

    public Observable<QiscusAccount> updateProfile(String username, String avatarUrl) {
        return api.updateProfile(Qiscus.getToken(), username, avatarUrl)
                .map(QiscusApiParser::parseQiscusAccount);
    }

    public Observable<QiscusChatRoom> getChatRoom(String withEmail, String distinctId, JSONObject options) {
        return api.createOrGetChatRoom(Qiscus.getToken(), Collections.singletonList(withEmail), distinctId,
                options == null ? null : options.toString())
                .map(QiscusApiParser::parseQiscusChatRoom)
                .doOnNext(this::decryptLastComment);
    }

    public Observable<QiscusChatRoom> createGroupChatRoom(String name, List<String> emails, String avatarUrl, JSONObject options) {
        return api.createGroupChatRoom(Qiscus.getToken(), name, emails, avatarUrl, options == null ? null : options.toString())
                .map(QiscusApiParser::parseQiscusChatRoom)
                .doOnNext(this::decryptLastComment);
    }

    public Observable<QiscusChatRoom> getGroupChatRoom(String uniqueId, String name, String avatarUrl, JSONObject options) {
        return api.createOrGetGroupChatRoom(Qiscus.getToken(), uniqueId, name, avatarUrl, options == null ? null : options.toString())
                .map(QiscusApiParser::parseQiscusChatRoom)
                .doOnNext(this::decryptLastComment);
    }

    public Observable<QiscusChatRoom> getChatRoom(long roomId) {
        return api.getChatRoom(Qiscus.getToken(), roomId)
                .map(QiscusApiParser::parseQiscusChatRoom)
                .doOnNext(this::decryptLastComment);
    }

    public Observable<Pair<QiscusChatRoom, List<QiscusComment>>> getChatRoomComments(long roomId) {
        return api.getChatRoom(Qiscus.getToken(), roomId)
                .map(QiscusApiParser::parseQiscusChatRoomWithComments)
                .doOnNext(data -> decryptLastComment(data.first))
                .doOnNext(data -> decryptComments(data.second));
    }

    public Observable<List<QiscusChatRoom>> getChatRooms(int page, int limit, boolean showMembers) {
        return api.getChatRooms(Qiscus.getToken(), page, limit, showMembers)
                .map(QiscusApiParser::parseQiscusChatRoomInfo)
                .doOnNext(qiscusChatRooms -> {
                    for (QiscusChatRoom qiscusChatRoom : qiscusChatRooms) {
                        decryptLastComment(qiscusChatRoom);
                    }
                });
    }

    public Observable<List<QiscusChatRoom>> getChatRooms(List<Long> roomIds, List<String> uniqueIds, boolean showMembers) {
        return api.getChatRooms(Qiscus.getToken(), roomIds, uniqueIds, showMembers)
                .map(QiscusApiParser::parseQiscusChatRoomInfo)
                .doOnNext(qiscusChatRooms -> {
                    for (QiscusChatRoom qiscusChatRoom : qiscusChatRooms) {
                        decryptLastComment(qiscusChatRoom);
                    }
                });
    }

    public Observable<QiscusComment> getComments(long roomId, long lastCommentId) {
        return api.getComments(Qiscus.getToken(), roomId, lastCommentId, false)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> QiscusApiParser.parseQiscusComment(jsonElement, roomId))
                .doOnNext(this::decryptComment);
    }

    public Observable<QiscusComment> getCommentsAfter(long roomId, long lastCommentId) {
        return api.getComments(Qiscus.getToken(), roomId, lastCommentId, true)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> QiscusApiParser.parseQiscusComment(jsonElement, roomId))
                .doOnNext(this::decryptComment);
    }

    public Observable<QiscusComment> postComment(QiscusComment qiscusComment) {
        Qiscus.getChatConfig().getCommentSendingInterceptor().sendComment(qiscusComment);

        Observable<String> encryptMessage = Observable.just(qiscusComment.getMessage())
                .map(message -> {
                    if (Qiscus.getChatConfig().isEnableEndToEndEncryption()
                            && qiscusComment.getType() == QiscusComment.Type.TEXT) {
                        QiscusChatRoom chatRoom = Qiscus.getDataStore().getChatRoom(qiscusComment.getRoomId());
                        if (chatRoom == null || chatRoom.isGroup()) {
                            return message;
                        }

                        String recipientId = "";
                        for (QiscusRoomMember qiscusRoomMember : chatRoom.getMember()) {
                            if (!qiscusRoomMember.getEmail().equals(Qiscus.getQiscusAccount().getEmail())) {
                                recipientId = qiscusRoomMember.getEmail();
                                break;
                            }
                        }

                        if (TextUtils.isEmpty(recipientId)) {
                            return message;
                        }

                        return QiscusEncryptionHandler.encrypt(recipientId, message);
                    }
                    return message;
                })
                .subscribeOn(Schedulers.computation());

        return encryptMessage.flatMap(message -> api.postComment(Qiscus.getToken(), message,
                qiscusComment.getRoomId(), qiscusComment.getUniqueId(), qiscusComment.getRawType(),
                qiscusComment.getExtraPayload(), qiscusComment.getExtras() == null ? null :
                        qiscusComment.getExtras().toString()))
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject()
                            .get("results").getAsJsonObject().get("comment").getAsJsonObject();
                    qiscusComment.setId(jsonComment.get("id").getAsLong());
                    qiscusComment.setCommentBeforeId(jsonComment.get("comment_before_id").getAsInt());
                    QiscusLogger.print("Sent Comment...");
                    return qiscusComment;
                })
                .doOnNext(comment -> EventBus.getDefault().post(new QiscusCommentSentEvent(comment)));
    }

    public Observable<QiscusComment> sync(long lastCommentId) {
        return api.sync(Qiscus.getToken(), lastCommentId)
                .onErrorReturn(throwable -> {
                    QiscusErrorLogger.print("Sync", throwable);
                    return null;
                })
                .filter(jsonElement -> jsonElement != null)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject();
                    return QiscusApiParser.parseQiscusComment(jsonElement, jsonComment.get("room_id").getAsLong());
                });
    }

    public Observable<QiscusComment> sync() {
        QiscusComment latestComment = Qiscus.getDataStore().getLatestComment();
        if (latestComment == null || !QiscusTextUtil.getString(R.string.qiscus_today)
                .equals(QiscusDateUtil.toTodayOrDate(latestComment.getTime()))) {
            return Observable.empty();
        }
        return sync(latestComment.getId());
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
                QiscusErrorLogger.print("UploadFile", e);
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

    public Observable<QiscusChatRoom> updateChatRoom(long roomId, String name, String avatarUrl, JSONObject options) {
        return api.updateChatRoom(Qiscus.getToken(), roomId, name, avatarUrl, options == null ? null : options.toString())
                .map(QiscusApiParser::parseQiscusChatRoom)
                .doOnNext(this::decryptLastComment)
                .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom));
    }

    public Observable<Void> updateCommentStatus(long roomId, long lastReadId, long lastReceivedId) {
        return api.updateCommentStatus(Qiscus.getToken(), roomId, lastReadId, lastReceivedId)
                .map(jsonElement -> null);
    }

    public Observable<Void> registerFcmToken(String fcmToken) {
        return api.registerFcmToken(Qiscus.getToken(), "android", fcmToken)
                .map(jsonElement -> null);
    }

    public Observable<List<QiscusComment>> searchComments(String query, long lastCommentId) {
        return searchComments(query, 0, lastCommentId);
    }

    public Observable<List<QiscusComment>> searchComments(String query, long roomId, long lastCommentId) {
        return api.searchComments(Qiscus.getToken(), query, roomId, lastCommentId)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject();
                    return QiscusApiParser.parseQiscusComment(jsonElement, jsonComment.get("room_id").getAsLong());
                })
                .doOnNext(this::decryptComment)
                .toList();
    }

    public Observable<Void> clearCommentsByRoomIds(List<Long> roomIds) {
        return api.getChatRooms(Qiscus.getToken(), roomIds, null, false)
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("results").getAsJsonObject())
                .map(jsonObject -> jsonObject.get("rooms_info").getAsJsonArray())
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get("unique_id").getAsString())
                .toList()
                .flatMap(this::clearCommentsByRoomUniqueIds);
    }

    public Observable<Void> clearCommentsByRoomUniqueIds(List<String> roomUniqueIds) {
        return api.clearChatRoomMessages(Qiscus.getToken(), roomUniqueIds)
                .map(JsonElement::getAsJsonObject)
                .map(jsonResponse -> jsonResponse.get("results").getAsJsonObject())
                .map(jsonResults -> jsonResults.get("rooms").getAsJsonArray())
                .flatMap(Observable::from)
                .map(JsonElement::getAsJsonObject)
                .doOnNext(json -> {
                    long roomId = json.get("id").getAsLong();
                    if (Qiscus.getDataStore().deleteCommentsByRoomId(roomId)) {
                        EventBus.getDefault().post(new QiscusClearCommentsEvent(roomId));
                    }
                })
                .toList()
                .map(qiscusChatRooms -> null);
    }

    public Observable<List<QiscusComment>> deleteComments(List<String> commentUniqueIds,
                                                          boolean isDeleteForEveryone,
                                                          boolean isHardDelete) {
        return api.deleteComments(Qiscus.getToken(), commentUniqueIds, isDeleteForEveryone, isHardDelete)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("results")
                        .getAsJsonObject().get("comments").getAsJsonArray()))
                .map(jsonElement -> {
                    JsonObject jsonComment = jsonElement.getAsJsonObject();
                    return QiscusApiParser.parseQiscusComment(jsonElement, jsonComment.get("room_id").getAsLong());
                })
                .toList()
                .doOnNext(comments -> {
                    QiscusAccount account = Qiscus.getQiscusAccount();
                    QiscusRoomMember actor = new QiscusRoomMember();
                    actor.setEmail(account.getEmail());
                    actor.setUsername(account.getUsername());
                    actor.setAvatar(account.getAvatar());

                    List<QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment> deletedComments = new ArrayList<>();
                    for (QiscusComment comment : comments) {
                        deletedComments.add(new QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment(comment.getRoomId(),
                                comment.getUniqueId()));
                    }

                    QiscusDeleteCommentHandler.DeletedCommentsData deletedCommentsData
                            = new QiscusDeleteCommentHandler.DeletedCommentsData();
                    deletedCommentsData.setActor(actor);
                    deletedCommentsData.setHardDelete(isHardDelete);
                    deletedCommentsData.setDeletedComments(deletedComments);

                    QiscusDeleteCommentHandler.handle(deletedCommentsData);
                });
    }

    public Observable<List<JSONObject>> getEvents(long startEventId) {
        return api.getEvents(Qiscus.getToken(), startEventId)
                .flatMap(jsonElement -> Observable.from(jsonElement.getAsJsonObject().get("events").getAsJsonArray()))
                .map(jsonEvent -> {
                    try {
                        return new JSONObject(jsonEvent.toString());
                    } catch (JSONException e) {
                        return null;
                    }
                })
                .filter(jsonObject -> jsonObject != null)
                .doOnNext(QiscusPusherApi::handleNotification)
                .toList();
    }

    private interface Api {

        @POST("/api/v2/auth/nonce")
        Observable<JsonElement> requestNonce();

        @FormUrlEncoded
        @POST("/api/v2/auth/verify_identity_token")
        Observable<JsonElement> login(@Field("identity_token") String token);

        @FormUrlEncoded
        @POST("/api/v2/mobile/login_or_register")
        Observable<JsonElement> loginOrRegister(@Field("email") String email,
                                                @Field("password") String password,
                                                @Field("username") String username,
                                                @Field("avatar_url") String avatarUrl);

        @FormUrlEncoded
        @PATCH("/api/v2/mobile/my_profile")
        Observable<JsonElement> updateProfile(@Field("token") String token,
                                              @Field("name") String name,
                                              @Field("avatar_url") String avatarUrl);

        @FormUrlEncoded
        @POST("/api/v2/mobile/get_or_create_room_with_target")
        Observable<JsonElement> createOrGetChatRoom(@Field("token") String token,
                                                    @Field("emails[]") List<String> emails,
                                                    @Field("distinct_id") String distinctId,
                                                    @Field("options") String options);

        @FormUrlEncoded
        @POST("/api/v2/mobile/create_room")
        Observable<JsonElement> createGroupChatRoom(@Field("token") String token,
                                                    @Field("name") String name,
                                                    @Field("participants[]") List<String> emails,
                                                    @Field("avatar_url") String avatarUrl,
                                                    @Field("options") String options);

        @FormUrlEncoded
        @POST("/api/v2/mobile/get_or_create_room_with_unique_id")
        Observable<JsonElement> createOrGetGroupChatRoom(@Field("token") String token,
                                                         @Field("unique_id") String uniqueId,
                                                         @Field("name") String name,
                                                         @Field("avatar_url") String avatarUrl,
                                                         @Field("options") String options);

        @GET("/api/v2/mobile/get_room_by_id")
        Observable<JsonElement> getChatRoom(@Query("token") String token,
                                            @Query("id") long roomId);

        @GET("/api/v2/mobile/load_comments")
        Observable<JsonElement> getComments(@Query("token") String token,
                                            @Query("topic_id") long roomId,
                                            @Query("last_comment_id") long lastCommentId,
                                            @Query("after") boolean after);

        @FormUrlEncoded
        @POST("/api/v2/mobile/post_comment")
        Observable<JsonElement> postComment(@Field("token") String token,
                                            @Field("comment") String message,
                                            @Field("topic_id") long roomId,
                                            @Field("unique_temp_id") String uniqueId,
                                            @Field("type") String type,
                                            @Field("payload") String payload,
                                            @Field("extras") String extras);

        @GET("/api/v2/mobile/sync")
        Observable<JsonElement> sync(@Query("token") String token,
                                     @Query("last_received_comment_id") long lastCommentId);

        @FormUrlEncoded
        @POST("/api/v2/mobile/update_room")
        Observable<JsonElement> updateChatRoom(@Field("token") String token,
                                               @Field("id") long id,
                                               @Field("room_name") String name,
                                               @Field("avatar_url") String avatarUrl,
                                               @Field("options") String options);

        @FormUrlEncoded
        @POST("/api/v2/mobile/update_comment_status")
        Observable<JsonElement> updateCommentStatus(@Field("token") String token,
                                                    @Field("room_id") long roomId,
                                                    @Field("last_comment_read_id") long lastReadId,
                                                    @Field("last_comment_received_id") long lastReceivedId);

        @FormUrlEncoded
        @POST("/api/v2/mobile/set_user_device_token")
        Observable<JsonElement> registerFcmToken(@Field("token") String token,
                                                 @Field("device_platform") String devicePlatform,
                                                 @Field("device_token") String fcmToken);

        @POST("/api/v2/mobile/search_messages")
        Observable<JsonElement> searchComments(@Query("token") String token,
                                               @Query("query") String query,
                                               @Query("room_id") long roomId,
                                               @Query("last_comment_id") long lastCommentId);

        @GET("/api/v2/mobile/user_rooms")
        Observable<JsonElement> getChatRooms(@Query("token") String token,
                                             @Query("page") int page,
                                             @Query("limit") int limit,
                                             @Query("show_participants") boolean showParticipants);

        @FormUrlEncoded
        @POST("/api/v2/mobile/rooms_info")
        Observable<JsonElement> getChatRooms(@Field("token") String token,
                                             @Field("room_id[]") List<Long> roomIds,
                                             @Field("room_unique_id[]") List<String> roomUniqueIds,
                                             @Field("show_participants") boolean showParticipants);

        @DELETE("/api/v2/mobile/clear_room_messages")
        Observable<JsonElement> clearChatRoomMessages(@Query("token") String token,
                                                      @Query("room_channel_ids[]") List<String> roomUniqueIds);

        @DELETE("/api/v2/mobile/delete_messages")
        Observable<JsonElement> deleteComments(@Query("token") String token,
                                               @Query("unique_ids[]") List<String> commentUniqueIds,
                                               @Query("is_delete_for_everyone") boolean isDeleteForEveryone,
                                               @Query("is_hard_delete") boolean isHardDelete);

        @GET("/api/v2/mobile/sync_event")
        Observable<JsonElement> getEvents(@Query("token") String token,
                                          @Query("start_event_id") long startEventId);
    }

    private void decryptLastComment(QiscusChatRoom qiscusChatRoom) {
        if (qiscusChatRoom.getLastComment() != null) {
            decryptComment(qiscusChatRoom.getLastComment());
        }
    }

    private void decryptComment(QiscusComment qiscusComment) {
        if (Qiscus.getChatConfig().isEnableEndToEndEncryption()) {
            QiscusEncryptionHandler.decrypt(qiscusComment);
        }
    }

    private void decryptComments(List<QiscusComment> qiscusComments) {
        if (Qiscus.getChatConfig().isEnableEndToEndEncryption()) {
            QiscusEncryptionHandler.decrypt(qiscusComments);
        }
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
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
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

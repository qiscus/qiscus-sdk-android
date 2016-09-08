package com.qiscus.sdk.data.remote;

import android.net.Uri;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qiscus.library.chat.BuildConfig;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.util.QiscusFileUtil;
import com.qiscus.sdk.util.QiscusParser;
import com.qiscus.sdk.util.QiscusServiceGenerator;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.internal.Util;

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

import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit.RestAdapter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import timber.log.Timber;

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

    private final Api api;

    QiscusApi() {
        api = QiscusServiceGenerator.createService(Api.class, Qiscus.getApiUrl(),
                BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.FULL);
        httpClient = QiscusHttpClient.getInstance().getHttpClient();
    }

    public static QiscusApi getInstance() {
        return INSTANCE;
    }

    private String generateToken() {
        return Qiscus.getToken();
    }

    public Observable<QiscusAccount> loginOrRegister(String email, String password, String username, String avatarUrl) {
        return api.loginOrRegister(email, password, username, avatarUrl)
                .map(jsonElement -> QiscusParser.get().parser()
                        .fromJson(jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("user"),
                                QiscusAccount.class));
    }

    public Observable<QiscusChatRoom> getChatRoom(List<String> withEmails) {
        return api.createOrGetChatRoom(generateToken(), withEmails)
                .map(jsonElement -> QiscusParser.get().parser()
                        .fromJson(jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("room"),
                                QiscusChatRoom.class));
    }

    public Observable<QiscusComment> getComments(int topicId, int lastCommentId) {
        return api.getComments(generateToken(), topicId, lastCommentId)
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return null;
                })
                .filter(commentsResponse -> commentsResponse != null)
                .flatMap(commentsResponse -> Observable.from(commentsResponse.results.comments))
                .map(CommentsResponse.CommentsResponseComments.CommentResponseCommentsSingle::toComment)
                .map(comment -> {
                    comment.setTopicId(topicId);
                    return comment;
                });
    }

    private static class CommentsResponse {
        private CommentsResponseComments results;

        private static class CommentsResponseComments {
            private List<CommentResponseCommentsSingle> comments;

            private static class CommentResponseCommentsSingle {
                int id;
                int commentBeforeId;
                String message;
                String username;
                String email;
                String timestamp;
                boolean deleted;

                private QiscusComment toComment() {
                    QiscusComment qiscusComment = new QiscusComment();
                    qiscusComment.setId(id);
                    qiscusComment.setUniqueId(String.valueOf(id));
                    qiscusComment.setCommentBeforeId(commentBeforeId);
                    qiscusComment.setMessage(message);
                    qiscusComment.setSender(username);
                    qiscusComment.setSenderEmail(email);
                    try {
                        qiscusComment.setTime(dateFormat.parse(timestamp));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    return qiscusComment;
                }
            }
        }

    }

    public Observable<QiscusComment> postComment(QiscusComment qiscusComment) {
        return api.postComment(generateToken(), qiscusComment.getMessage(),
                qiscusComment.getTopicId(), qiscusComment.getUniqueId())
                .map(jsonElement -> {
                    JsonObject commentJson = jsonElement.getAsJsonObject()
                            .get("results").getAsJsonObject().get("comment").getAsJsonObject();
                    qiscusComment.setId(commentJson.get("id").getAsInt());
                    qiscusComment.setCommentBeforeId(commentJson.get("comment_before_id").getAsInt());
                    return qiscusComment;
                });
    }

    public Observable<Void> markTopicAsRead(int topicId) {
        return api.markTopicAsRead(generateToken(), topicId);
    }

    public Observable<Uri> uploadFile(File file, ProgressListener progressListener) {
        return Observable.create(subscriber -> {
            long fileLength = file.length();

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("token", generateToken())
                    .addFormDataPart("file", file.getName(),
                            new CountingFileRequestBody(file, totalBytes -> {
                                int progress = (int) (totalBytes * 100 / fileLength);
                                progressListener.onProgress(progress);
                            }))
                    .build();

            Request request = new Request.Builder()
                    .url(Qiscus.getApiUrl() + "/api/v2/mobile/upload")
                    .post(requestBody).build();

            try {
                com.squareup.okhttp.Response response = httpClient.newCall(request).execute();
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

                com.squareup.okhttp.Response response = httpClient.newCall(request).execute();

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
                                                    @Field("emails[]") List<String> emails);

        @GET("/api/v2/mobile/load_comments")
        Observable<CommentsResponse> getComments(@Query("token") String token,
                                                 @Query("topic_id") int topicId,
                                                 @Query("last_comment_id") int lastCommentId);

        @FormUrlEncoded
        @POST("/api/v2/mobile/post_comment")
        Observable<JsonElement> postComment(@Field("token") String token,
                                                    @Field("comment") String message,
                                                    @Field("topic_id") int topicId,
                                                    @Field("unique_temp_id") String uniqueId);

        @GET("/api/v1/mobile/readnotif/{topic_id}")
        Observable<Void> markTopicAsRead(@Query("token") String token,
                                         @Path("topic_id") int topicId);
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

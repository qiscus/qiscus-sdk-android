package com.qiscus.sdk.data.remote;

import android.net.Uri;

import com.qiscus.library.chat.BuildConfig;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.remote.response.ChatRoomResponse;
import com.qiscus.sdk.util.QiscusServiceGenerator;
import com.qiscus.sdk.util.QiscusFileUtil;
import com.squareup.okhttp.Headers;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okio.BufferedSink;
import retrofit.RestAdapter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Query;
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
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final Api api;

    QiscusApi() {
        api = QiscusServiceGenerator.createService(Api.class, Qiscus.getApiUrl(),
                                                 BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE);
        httpClient = QiscusHttpClient.getInstance().getHttpClient();
    }

    public static QiscusApi getInstance() {
        return INSTANCE;
    }

    private String generateToken() {
        return "Token token=" + Qiscus.getToken();
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
                String usernameAs;
                String usernameReal;
                String createdAt;
                boolean deleted;

                private QiscusComment toComment() {
                    QiscusComment qiscusComment = new QiscusComment();
                    qiscusComment.setId(id);
                    qiscusComment.setUniqueId(String.valueOf(id));
                    qiscusComment.setCommentBeforeId(commentBeforeId);
                    qiscusComment.setMessage(message);
                    qiscusComment.setSender(usernameAs);
                    qiscusComment.setSenderEmail(usernameReal);
                    try {
                        qiscusComment.setTime(dateFormat.parse(createdAt));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    return qiscusComment;
                }
            }
        }

    }

    private static class PostCommentResponse {
        public int success;
        public int commentId;
        public int commentBeforeId;
    }

    public Observable<QiscusComment> postComment(QiscusComment qiscusComment) {
        return api.postComment(generateToken(), qiscusComment.getMessage(), qiscusComment.getRoomId(),
                               qiscusComment.getTopicId(), qiscusComment.getUniqueId())
                .map(postCommentResponse -> {
                    qiscusComment.setId(postCommentResponse.commentId);
                    qiscusComment.setCommentBeforeId(postCommentResponse.commentBeforeId);
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
                    .addPart(Headers.of("Content-Disposition",
                                        String.format("form-data; name=\"%s\"; filename=\"%s\"", "raw_file", file.getName())),
                             new CountingFileRequestBody(file, totalBytes -> {
                                 int progress = (int) (totalBytes * 100 / fileLength);
                                 progressListener.onProgress(progress);
                             }))
                    .build();

            Request request = new Request.Builder()
                    .url(Qiscus.getApiUrl() + "/files/upload")
                    .addHeader("Authorization", generateToken())
                    .post(requestBody).build();

            try {
                com.squareup.okhttp.Response response = httpClient.newCall(request).execute();
                JSONObject responseJ = new JSONObject(response.body().string());
                String result = responseJ.getJSONObject("data").getJSONObject("file").getString("url");

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
                Request request = new Request.Builder().url(url)
                        .addHeader("Authorization", generateToken()).build();

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

    public Observable<QiscusChatRoom> getChatRoom(int chatRoomId) {
        return api.getChatRoom(generateToken(), chatRoomId).map(ChatRoomResponse::getResult);
    }

    private interface Api {

        @GET("/chats/topic_comments")
        Observable<CommentsResponse> getComments(@Header("Authorization") String token,
                                                 @Query("topic_id") int topicId,
                                                 @Query("comment_id") int lastCommentId);

        @FormUrlEncoded
        @POST("/chats/postcomment")
        Observable<PostCommentResponse> postComment(@Header("Authorization") String token,
                                                    @Field("comment") String message,
                                                    @Field("room_id") int roomId,
                                                    @Field("topic_id") int topicId,
                                                    @Field("unique_id") String uniqueId);

        @GET("/chats/readnotif")
        Observable<Void> markTopicAsRead(@Header("Authorization") String token,
                                         @Query("topic_id") int topicId);

        @GET("/archives/chat_rooms")
        Observable<ChatRoomResponse> getChatRoom(@Header("Authorization") String token,
                                                 @Query("room_id") int chatRoomId);
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
        public void writeTo(BufferedSink sink) throws IOException {
            InputStream inputStream = new FileInputStream(file);
            try {
                OutputStream outputStream = sink.outputStream();
                long total = 0;
                int read;
                byte buffer[] = new byte[SEGMENT_SIZE];
                while ((read = inputStream.read(buffer)) != -1) {
                    total += read;
                    outputStream.write(buffer);
                    sink.flush();
                    progressListener.onProgress(total);
                }
            } finally {
                Util.closeQuietly(inputStream);
            }
        }

    }

    public interface ProgressListener {
        void onProgress(long total);
    }
}

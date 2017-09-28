package com.qiscus.sdk.chat.data.remote

import com.qiscus.sdk.chat.data.remote.model.*
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*


/**
 * Interface yang digunakan untuk berinteraksi dengan Rest API server
 */
interface QiscusRestApi {

    /**
     * Endpoint yang bisa digunakan untuk meminta nonce yang nantinya bisa digunakan untuk
     * membuat JWT token di backend aplikasi anda yang selanjutnya bisa digunakan untuk melakukan
     * autentikasi ke qiscus dengan memanggil method [authenticate]
     */
    @POST("/api/v2/auth/nonce")
    fun requestNonce(): Single<NonceResponseModel>

    /**
     * Endpoint yang digunakan untuk melakukan autentikasi menggunakan JWT [token] yang sebelumnya
     * telah dibuat backend anda dengan menngunakan nonce yang dapat anda ambil menggunakan [requestNonce]
     */
    @FormUrlEncoded
    @POST("/api/v2/auth/verify_identity_token")
    fun authenticate(@Field("identity_token") token: String): Single<AccountResponseModel>

    /**
     * Dengan endpoint ini anda dapat melakukan autentikasi ke server Qiscus tanpa perlu membuat
     * JWT token, yang perlu anda lakukan hanyalah berikan [userId] yang unik untuk user ini
     * dan juga [userKey] sebagai kata sandi atau pengaman untuk memastikan bahwa hanya [userId] ini
     * yang bisa masuk ke sebagai user ini. Anda juga dapat menyertakan [name] sebagai nama pengguna
     * dan [avatarUrl] sebagai alamat gambar avatar bagi pengguna ini.
     */
    @FormUrlEncoded
    @POST("/api/v2/mobile/login_or_register")
    fun authenticateWithKey(@Field("email") userId: String,
                            @Field("password") userKey: String,
                            @Field("username") name: String = userId,
                            @Field("avatar_url") avatarUrl: String = ""): Single<AccountResponseModel>

    @FormUrlEncoded
    @PATCH("/api/v2/mobile/my_profile")
    fun updateProfile(@Field("token") token: String,
                      @Field("name") name: String,
                      @Field("avatar_url") avatarUrl: String): Single<AccountResponseModel>

    @FormUrlEncoded
    @POST("/api/v2/mobile/get_or_create_room_with_target")
    fun createOrGetChatRoom(@Field("token") token: String,
                            @Field("emails[]") userId: String,
                            @Field("distinct_id") uniqueId: String = "",
                            @Field("options") options: String = ""): Single<RoomResponseModel>

    @FormUrlEncoded
    @POST("/api/v2/mobile/create_room")
    fun createGroupChatRoom(@Field("token") token: String,
                            @Field("name") name: String,
                            @Field("participants[]") userIds: List<String>,
                            @Field("avatar_url") avatarUrl: String,
                            @Field("options") options: String): Single<RoomResponseModel>

    @FormUrlEncoded
    @POST("/api/v2/mobile/get_or_create_room_with_unique_id")
    fun createOrGetGroupChatRoom(@Field("token") token: String,
                                 @Field("unique_id") uniqueId: String,
                                 @Field("name") name: String,
                                 @Field("avatar_url") avatarUrl: String,
                                 @Field("options") options: String): Single<RoomResponseModel>

    @GET("/api/v2/mobile/get_room_by_id")
    fun getChatRoom(@Query("token") token: String,
                    @Query("id") roomId: String): Single<RoomResponseModel>

    @GET("/api/v2/mobile/load_comments")
    fun getComments(@Query("token") token: String,
                    @Query("topic_id") roomId: String,
                    @Query("after") afterCommentId: Boolean): Single<ListCommentResponseModel>

    @GET("/api/v2/mobile/load_comments")
    fun getComments(@Query("token") token: String,
                    @Query("topic_id") roomId: String,
                    @Query("last_comment_id") lastCommentId: String,
                    @Query("limit") limit: Int,
                    @Query("after") afterCommentId: Boolean): Single<ListCommentResponseModel>

    @FormUrlEncoded
    @POST("/api/v2/mobile/post_comment")
    fun postComment(@Field("token") token: String,
                    @Field("topic_id") roomId: String,
                    @Field("comment") message: String,
                    @Field("unique_temp_id") uniqueId: String,
                    @Field("type") type: String,
                    @Field("payload") payload: String): Single<CommentResponseModel>

    @GET("/api/v2/mobile/sync")
    fun sync(@Query("token") token: String,
             @Query("last_received_comment_id") lastCommentId: String): Single<SyncCommentResponseModel>

    @FormUrlEncoded
    @POST("/api/v2/mobile/update_room")
    fun updateChatRoom(@Field("token") token: String,
                       @Field("id") roomId: String,
                       @Field("room_name") name: String,
                       @Field("avatar_url") avatarUrl: String,
                       @Field("options") options: String): Single<RoomResponseModel>

    @FormUrlEncoded
    @POST("/api/v2/mobile/update_comment_status")
    fun updateCommentStatus(@Field("token") token: String,
                            @Field("room_id") roomId: String,
                            @Field("last_comment_received_id") lastReceivedId: String,
                            @Field("last_comment_read_id") lastReadId: String): Completable

    @FormUrlEncoded
    @POST("/api/v2/mobile/set_user_device_token")
    fun registerFcmToken(@Field("token") token: String,
                         @Field("device_platform") devicePlatform: String,
                         @Field("device_token") fcmToken: String): Completable

    @POST("/api/v2/mobile/search_messages")
    fun searchComments(@Query("token") token: String,
                       @Query("room_id") roomId: String,
                       @Query("query") query: String,
                       @Query("last_comment_id") lastCommentId: String): Single<SearchCommentResponseModel>

    @GET("/api/v2/mobile/user_rooms")
    fun getChatRooms(@Query("token") token: String,
                     @Query("page") page: Int,
                     @Query("limit") limit: Int,
                     @Query("show_participants") showParticipants: Boolean): Single<ListRoomResponseModel>

    @FormUrlEncoded
    @POST("/api/v2/mobile/rooms_info")
    fun getChatRooms(@Field("token") token: String,
                     @Field("room_id[]") roomIds: List<String>,
                     @Field("room_unique_id[]") roomUniqueIds: List<String>,
                     @Field("show_participants") showParticipants: Boolean): Single<ListRoomResponseModel>
}
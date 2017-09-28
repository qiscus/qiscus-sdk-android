package com.qiscus.sdk.chat.data.pusher.mapper

import com.qiscus.sdk.chat.data.mapper.transformToTypedCommentEntity
import com.qiscus.sdk.chat.data.model.*
import org.json.JSONObject

/**
 * Created on : September 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentPayloadMapper : ModelPusherMapper<String, CommentEntity> {
    override fun mapFromPusher(payload: String): CommentEntity {
        val json = JSONObject(payload)
        var extraPayload: JSONObject? = json.optJSONObject("payload")
        if (extraPayload == null) {
            extraPayload = JSONObject()
        }

        return CommentEntity(
                CommentIdEntity(json.getString("id_str"),
                        json.getString("comment_before_id_str"),
                        json.getString("unique_temp_id")),
                json.getString("message"),
                UserEntity(json.getString("email"),
                        json.getString("username"),
                        json.getString("user_avatar")),
                json.getLong("unix_nano_timestamp"),
                RoomEntity(json.getString("room_id_str"),
                        name = json.optString("room_name"),
                        avatar = json.optString("room_avatar", ""),
                        group = "single" != json.getString("chat_type")),
                CommentStateEntity.ON_SERVER,
                CommentTypeEntity(json.optString("type", "text"),
                        extraPayload)
        ).transformToTypedCommentEntity()
    }
}
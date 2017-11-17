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

package com.qiscus.sdk.chat.data.pusher.mapper

import com.qiscus.sdk.chat.data.mapper.transformToTypedMessageEntity
import com.qiscus.sdk.chat.data.model.*
import org.json.JSONObject

/**
 * Created on : September 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class MessagePayloadMapper : ModelPusherMapper<String, MessageEntity> {
    override fun mapFromPusher(payload: String): MessageEntity {
        val json = JSONObject(payload)
        var extraPayload: JSONObject? = json.optJSONObject("payload")
        if (extraPayload == null) {
            extraPayload = JSONObject()
        }

        return MessageEntity(
                MessageIdEntity(json.getString("id_str"),
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
                MessageStateEntity.ON_SERVER,
                MessageTypeEntity(json.optString("type", "text"),
                        extraPayload)
        ).transformToTypedMessageEntity()
    }
}
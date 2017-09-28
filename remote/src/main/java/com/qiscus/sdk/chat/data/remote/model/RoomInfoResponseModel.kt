package com.qiscus.sdk.chat.data.remote.model

import com.qiscus.sdk.chat.data.model.MemberStateEntity
import com.qiscus.sdk.chat.data.model.RoomEntity
import com.qiscus.sdk.chat.data.model.RoomMemberEntity
import com.qiscus.sdk.chat.data.model.UserEntity

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */

data class RoomInfoResponseModel(var results: Results, var status: Int) {

    data class Results(var rooms_info: List<RoomsInfo>)

    data class RoomsInfo(
            var lastCommentId: Long,
            var lastCommentMessage: String,
            var lastCommentTimestamp: String,
            var participants: List<Participant>,
            var roomAvatarUrl: String,
            var roomId: Long,
            var roomIdStr: String,
            var roomName: String,
            var rawRoomName: String,
            var roomType: String,
            var unreadCount: Int
    ) {
        fun toEntity(): RoomEntity {
            return RoomEntity(
                    roomIdStr, rawRoomName, roomName, roomAvatarUrl
            )
        }
    }

    data class Participant(
            var avatarUrl: String,
            var email: String,
            var id: Long,
            var lastCommentReadId: Long,
            var lastCommentReadIdStr: String,
            var lastCommentReceivedId: Long,
            var lastCommentReceivedIdStr: String,
            var username: String
    ) {
        fun toEntity(): RoomMemberEntity {
            return RoomMemberEntity(UserEntity(email, username, avatarUrl),
                    MemberStateEntity(lastCommentReceivedIdStr, lastCommentReadIdStr))
        }
    }
}
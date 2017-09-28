package com.qiscus.sdk.chat.data.remote

import com.qiscus.sdk.chat.data.model.RoomEntity
import com.qiscus.sdk.chat.data.model.RoomMemberEntity
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.comment.CommentLocal
import com.qiscus.sdk.chat.data.source.room.RoomLocal
import com.qiscus.sdk.chat.data.source.room.RoomRemote
import io.reactivex.Single

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class RoomRemoteImpl(private val accountLocal: AccountLocal,
                     private val qiscusRestApi: QiscusRestApi,
                     private val roomLocal: RoomLocal,
                     private val commentLocal: CommentLocal) : RoomRemote {

    override fun getRoom(roomId: String): Single<RoomEntity> {
        return qiscusRestApi.getChatRoom(accountLocal.getAccount().token, roomId)
                .doOnSuccess {
                    val participants = it.results.room.participants
                    roomLocal.updateRoomMembers(it.results.room.idStr, participants.map { it.toEntity() })

                    it.results.comments.map { it.toEntity(accountLocal.getAccount(), participants) }
                            .forEach { commentLocal.addOrUpdateComment(it) }
                }
                .map { it.results.room.toEntity() }
    }

    override fun getRoomWithUserId(userId: String): Single<RoomEntity> {
        return qiscusRestApi.createOrGetChatRoom(accountLocal.getAccount().token, userId)
                .doOnSuccess {
                    val participants = it.results.room.participants
                    roomLocal.updateRoomMembers(it.results.room.idStr, participants.map { it.toEntity() })

                    it.results.comments.map { it.toEntity(accountLocal.getAccount(), participants) }
                            .forEach { commentLocal.addOrUpdateComment(it) }
                }
                .map { it.results.room.toEntity() }
    }

    override fun createGroupRoom(userIds: List<String>, roomName: String, roomAvatarUrl: String): Single<RoomEntity> {
        return qiscusRestApi.createGroupChatRoom(accountLocal.getAccount().token, roomName, userIds, roomAvatarUrl, "")
                .doOnSuccess {
                    val participants = it.results.room.participants
                    roomLocal.updateRoomMembers(it.results.room.idStr, participants.map { it.toEntity() })

                    it.results.comments.map { it.toEntity(accountLocal.getAccount(), participants) }
                            .forEach { commentLocal.addOrUpdateComment(it) }
                }
                .map { it.results.room.toEntity() }
    }

    override fun getRoomWithChannelId(channelId: String, roomAvatarUrl: String): Single<RoomEntity> {
        return qiscusRestApi.createOrGetGroupChatRoom(accountLocal.getAccount().token, channelId, "", roomAvatarUrl, "")
                .doOnSuccess {
                    val participants = it.results.room.participants
                    roomLocal.updateRoomMembers(it.results.room.idStr, participants.map { it.toEntity() })

                    it.results.comments.map { it.toEntity(accountLocal.getAccount(), participants) }
                            .forEach { commentLocal.addOrUpdateComment(it) }
                }
                .map { it.results.room.toEntity() }
    }

    override fun getRoomMembers(roomId: String): Single<List<RoomMemberEntity>> {
        return qiscusRestApi.getChatRoom(accountLocal.getAccount().token, roomId)
                .map { it.results.room.participants.map { it.toEntity() } }
    }

    override fun getRooms(page: Int, limit: Int): Single<List<RoomEntity>> {
        return qiscusRestApi.getChatRooms(accountLocal.getAccount().token, page, limit, true)
                .doOnSuccess {
                    it.results.roomsInfo.forEach {
                        val participants = it.participants
                        roomLocal.updateRoomMembers(it.idStr, participants.map { it.toEntity() })

                        commentLocal.addOrUpdateComment(it.lastComment.toEntity(accountLocal.getAccount(), participants))
                    }
                }
                .map { it.results.roomsInfo.map { it.toEntity() } }
    }

    override fun getRoomsWithSpecificIds(roomIds: List<String>, channelIds: List<String>): Single<List<RoomEntity>> {
        return qiscusRestApi.getChatRooms(accountLocal.getAccount().token, roomIds, channelIds, true)
                .doOnSuccess {
                    it.results.roomsInfo.forEach {
                        val participants = it.participants
                        roomLocal.updateRoomMembers(it.idStr, participants.map { it.toEntity() })

                        commentLocal.addOrUpdateComment(it.lastComment.toEntity(accountLocal.getAccount(), participants))
                    }
                }
                .map { it.results.roomsInfo.map { it.toEntity() } }
    }
}
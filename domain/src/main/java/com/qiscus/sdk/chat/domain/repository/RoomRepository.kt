package com.qiscus.sdk.chat.domain.repository

import com.qiscus.sdk.chat.domain.model.Room
import com.qiscus.sdk.chat.domain.model.RoomMember
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface RoomRepository {
    fun getRoom(roomId: String): Single<Room>

    fun getRoomWithUserId(userId: String): Single<Room>

    fun createGroupRoom(userIds: List<String>, roomName: String, roomAvatarUrl: String): Single<Room>

    fun getRoomWithChannelId(channelId: String, roomAvatarUrl: String): Single<Room>

    fun getRoomMembers(roomId: String): Single<List<RoomMember>>

    fun getRooms(page: Int, limit: Int): Single<List<Room>>

    fun getRoomsWithSpecificIds(roomIds: List<String>, channelIds: List<String>): Single<List<Room>>

    fun clearData(): Completable
}
package com.qiscus.sdk.chat.data

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.model.RoomMemberEntity
import com.qiscus.sdk.chat.data.source.room.RoomLocal
import com.qiscus.sdk.chat.data.source.room.RoomRemote
import com.qiscus.sdk.chat.domain.model.Room
import com.qiscus.sdk.chat.domain.model.RoomMember
import com.qiscus.sdk.chat.domain.repository.RoomRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class RoomDataRepository(private val roomLocal: RoomLocal,
                         private val roomRemote: RoomRemote) : RoomRepository {

    override fun getRoom(roomId: String): Single<Room> {
        val savedRoom = roomLocal.getRoom(roomId)
        if (savedRoom != null) {
            roomRemote.getRoom(roomId)
                    .doOnSuccess { roomLocal.updateRoom(it) }
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, {})

            return Single.fromCallable { savedRoom }
                    .map { it.toDomainModel() }
        }
        return roomRemote.getRoom(roomId)
                .doOnSuccess { roomLocal.addOrUpdateRoom(it) }
                .map { it.toDomainModel() }
    }

    override fun getRoomWithUserId(userId: String): Single<Room> {
        val savedRoom = roomLocal.getRoomWithUserId(userId)
        if (savedRoom != null) {
            roomRemote.getRoomWithUserId(userId)
                    .doOnSuccess { roomLocal.updateRoom(it) }
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, {})

            return Single.fromCallable { savedRoom }
                    .map { it.toDomainModel() }
        }
        return roomRemote.getRoomWithUserId(userId)
                .doOnSuccess { roomLocal.addOrUpdateRoom(it) }
                .map { it.toDomainModel() }
    }

    override fun createGroupRoom(userIds: List<String>, roomName: String, roomAvatarUrl: String): Single<Room> {
        return roomRemote.createGroupRoom(userIds, roomName, roomAvatarUrl)
                .doOnSuccess { roomLocal.addOrUpdateRoom(it) }
                .map { it.toDomainModel() }
    }

    override fun getRoomWithChannelId(channelId: String, roomAvatarUrl: String): Single<Room> {
        val savedRoom = roomLocal.getRoomWithChannelId(channelId)
        if (savedRoom != null) {
            roomRemote.getRoomWithChannelId(channelId, roomAvatarUrl)
                    .doOnSuccess { roomLocal.updateRoom(it) }
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, {})

            return Single.fromCallable { savedRoom }
                    .map { it.toDomainModel() }
        }

        return roomRemote.getRoomWithChannelId(channelId, roomAvatarUrl)
                .doOnSuccess { roomLocal.addOrUpdateRoom(it) }
                .map { it.toDomainModel() }
    }

    override fun getRoomMembers(roomId: String): Single<List<RoomMember>> {
        roomRemote.getRoomMembers(roomId)
                .doOnSuccess { updateLocalRoomMembers(roomId, it) }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        return Single.defer { Single.fromCallable { roomLocal.getRoomMembers(roomId) } }
                .flatMap {
                    if (it.isEmpty()) {
                        return@flatMap roomRemote.getRoomMembers(roomId)
                                .doOnSuccess { updateLocalRoomMembers(roomId, it) }
                    }
                    return@flatMap Single.just(it)
                }.map { it.map { it.toDomainModel() } }
    }

    override fun getRooms(page: Int, limit: Int): Single<List<Room>> {
        roomRemote.getRooms(page, limit)
                .doOnSuccess { it.forEach { roomLocal.addOrUpdateRoom(it) } }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        return Single.defer { Single.fromCallable { roomLocal.getRooms(page, limit) } }
                .flatMap {
                    if (it.isEmpty()) {
                        return@flatMap roomRemote.getRooms(page, limit)
                                .doOnSuccess { it.forEach { roomLocal.addOrUpdateRoom(it) } }
                    }
                    return@flatMap Single.just(it)
                }.map { it.map { it.toDomainModel() } }
    }

    override fun getRoomsWithSpecificIds(roomIds: List<String>, channelIds: List<String>): Single<List<Room>> {
        roomRemote.getRoomsWithSpecificIds(roomIds, channelIds)
                .doOnSuccess { it.forEach { roomLocal.addOrUpdateRoom(it) } }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        return Single.defer { Single.fromCallable { roomLocal.getRoomsWithSpecificIds(roomIds, channelIds) } }
                .flatMap {
                    if (roomIds.all { ids -> it.any { it.id == ids } } && channelIds.all { ids -> it.any { it.uniqueId == ids } }) {
                        return@flatMap Single.just(it)
                    }
                    return@flatMap roomRemote.getRoomsWithSpecificIds(roomIds, channelIds)
                            .doOnSuccess { it.forEach { roomLocal.addOrUpdateRoom(it) } }
                }.map { it.map { it.toDomainModel() } }
    }

    private fun updateLocalRoomMembers(roomId: String, members: List<RoomMemberEntity>) {
        val savedRoom = roomLocal.getRoom(roomId)
        if (savedRoom != null) {
            roomLocal.updateRoomMembers(savedRoom.id, members)
        } else {
            roomRemote.getRoom(roomId)
                    .doOnSuccess { room ->
                        roomLocal.addOrUpdateRoom(room)
                        roomLocal.updateRoomMembers(room.id, members)
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, {})
        }
    }

    override fun clearData(): Completable {
        return Completable.defer {
            roomLocal.clearData()
            Completable.complete()
        }
    }
}
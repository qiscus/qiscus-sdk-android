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

package com.qiscus.sdk.chat.data

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.model.ParticipantEntity
import com.qiscus.sdk.chat.data.source.room.RoomLocal
import com.qiscus.sdk.chat.data.source.room.RoomRemote
import com.qiscus.sdk.chat.domain.model.Room
import com.qiscus.sdk.chat.domain.model.Participant
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

    override fun getRoomParticipants(roomId: String): Single<List<Participant>> {
        roomRemote.getParticipants(roomId)
                .doOnSuccess { updateLocalParticipants(roomId, it) }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        return Single.defer { Single.fromCallable { roomLocal.getParticipants(roomId) } }
                .flatMap {
                    if (it.isEmpty()) {
                        return@flatMap roomRemote.getParticipants(roomId)
                                .doOnSuccess { updateLocalParticipants(roomId, it) }
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

    private fun updateLocalParticipants(roomId: String, participants: List<ParticipantEntity>) {
        val savedRoom = roomLocal.getRoom(roomId)
        if (savedRoom != null) {
            roomLocal.updateParticipants(savedRoom.id, participants)
        } else {
            roomRemote.getRoom(roomId)
                    .doOnSuccess { room ->
                        roomLocal.addOrUpdateRoom(room)
                        roomLocal.updateParticipants(room.id, participants)
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
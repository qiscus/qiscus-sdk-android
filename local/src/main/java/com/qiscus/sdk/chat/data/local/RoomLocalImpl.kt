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

package com.qiscus.sdk.chat.data.local

import android.database.DatabaseUtils
import com.qiscus.sdk.chat.data.local.database.Db
import com.qiscus.sdk.chat.data.local.database.DbOpenHelper
import com.qiscus.sdk.chat.data.local.database.transaction
import com.qiscus.sdk.chat.data.local.mapper.toContentValues
import com.qiscus.sdk.chat.data.local.mapper.toRoomEntity
import com.qiscus.sdk.chat.data.local.mapper.toRoomMemberEntity
import com.qiscus.sdk.chat.data.model.MessageIdEntity
import com.qiscus.sdk.chat.data.model.RoomEntity
import com.qiscus.sdk.chat.data.model.RoomMemberEntity
import com.qiscus.sdk.chat.data.pubsub.room.RoomPublisher
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.room.RoomLocal
import com.qiscus.sdk.chat.data.source.user.UserLocal

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class RoomLocalImpl(dbOpenHelper: DbOpenHelper,
                    private val accountLocal: AccountLocal,
                    private val userLocal: UserLocal,
                    private val roomPublisher: RoomPublisher) : RoomLocal {

    private val database = dbOpenHelper.readableDatabase

    override fun addRoom(roomEntity: RoomEntity) {
        if (!isExistRoom(roomEntity.id)) {
            database.transaction {
                database.insert(Db.RoomTable.TABLE_NAME, null, roomEntity.toContentValues())
                roomPublisher.onRoomAdded(roomEntity)
            }
        }
    }

    override fun updateRoom(roomEntity: RoomEntity) {
        val oldRoom = getRoom(roomEntity.id)
        if (oldRoom != null) {
            if (oldRoom != roomEntity) {
                val where = Db.RoomTable.COLUMN_ID + " = " + roomEntity.id
                database.transaction {
                    database.update(Db.RoomTable.TABLE_NAME, roomEntity.toContentValues(), where, null)
                    roomPublisher.onRoomUpdated(roomEntity)
                }
            }
        }
    }

    override fun addOrUpdateRoom(roomEntity: RoomEntity) {
        val oldRoom = getRoom(roomEntity.id)
        if (oldRoom != null) {
            if (oldRoom != roomEntity) {
                val where = Db.RoomTable.COLUMN_ID + " = " + roomEntity.id
                database.transaction {
                    database.update(Db.RoomTable.TABLE_NAME, roomEntity.toContentValues(), where, null)
                    roomPublisher.onRoomUpdated(roomEntity)
                }
            }
        } else {
            database.transaction {
                database.insert(Db.RoomTable.TABLE_NAME, null, roomEntity.toContentValues())
                roomPublisher.onRoomAdded(roomEntity)
            }
        }
    }

    override fun deleteRoom(roomEntity: RoomEntity) {
        if (isExistRoom(roomEntity.id)) {
            val where = Db.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomEntity.id
            database.transaction {
                database.delete(Db.RoomTable.TABLE_NAME, where, null)
                roomPublisher.onRoomDeleted(roomEntity)
            }
        }
    }

    override fun getRoom(roomId: String): RoomEntity? {
        val query = "SELECT * FROM ${Db.RoomTable.TABLE_NAME} WHERE ${Db.RoomTable.COLUMN_ID} " +
                "= ${DatabaseUtils.sqlEscapeString(roomId)}"
        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val roomEntity = cursor.toRoomEntity()
            cursor.close()
            return roomEntity
        }

        cursor.close()
        return null
    }

    override fun getRoomByUniqueId(roomUniqueId: String): RoomEntity? {
        val query = "SELECT * FROM ${Db.RoomTable.TABLE_NAME} WHERE ${Db.RoomTable.COLUMN_UNIQUE_ID} " +
                "= ${DatabaseUtils.sqlEscapeString(roomUniqueId)}"
        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val roomEntity = cursor.toRoomEntity()
            cursor.close()
            return roomEntity
        }

        cursor.close()
        return null
    }

    override fun getRoomWithUserId(userId: String): RoomEntity? {
        val account = accountLocal.getAccount()
        var room = getRoomByUniqueId(account.user.id + " " + userId)
        if (room == null) {
            room = getRoomByUniqueId(userId + " " + account.user.id)
        }
        return room
    }

    override fun getRoomWithUserId(userId: String, uniqueId: String): RoomEntity? {
        val account = accountLocal.getAccount()
        var room = getRoomByUniqueId(account.user.id + " " + userId + " " + uniqueId)
        if (room == null) {
            room = getRoomByUniqueId(userId + " " + account.user.id + "" + uniqueId)
        }
        return room
    }

    override fun getRoomWithChannelId(channelId: String): RoomEntity? {
        return getRoomByUniqueId(channelId)
    }

    override fun addRoomMember(roomId: String, roomMemberEntity: RoomMemberEntity) {
        if (!isExistRoomMember(roomId, roomMemberEntity.userEntity.id)) {
            database.transaction {
                database.insert(Db.RoomMemberTable.TABLE_NAME, null,
                        roomMemberEntity.toContentValues(roomId))
            }
        }
        userLocal.addOrUpdateUser(roomMemberEntity.userEntity)
    }

    override fun updateRoomMemberDeliveredState(roomId: String, userId: String, messageIdEntity: MessageIdEntity) {
        database.transaction {
            val sql = "UPDATE " + Db.RoomMemberTable.TABLE_NAME + " SET " + Db.RoomMemberTable.COLUMN_LAST_DELIVERED +
                    " = " + DatabaseUtils.sqlEscapeString(messageIdEntity.id) + " WHERE " +
                    Db.RoomMemberTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) +
                    " AND " + Db.RoomMemberTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userId)
            database.execSQL(sql)
        }
    }

    override fun updateRoomMemberReadState(roomId: String, userId: String, messageIdEntity: MessageIdEntity) {
        database.transaction {
            val sql = "UPDATE " + Db.RoomMemberTable.TABLE_NAME + " SET " + Db.RoomMemberTable.COLUMN_LAST_READ +
                    " = " + DatabaseUtils.sqlEscapeString(messageIdEntity.id) + " WHERE " +
                    Db.RoomMemberTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) +
                    " AND " + Db.RoomMemberTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userId)
            database.execSQL(sql)
        }
    }

    override fun updateRoomMembers(roomId: String, roomMemberEntities: List<RoomMemberEntity>) {
        deleteRoomMembers(roomId)
        for (member in roomMemberEntities) {
            addRoomMember(roomId, member)
        }
    }

    override fun deleteRoomMember(roomId: String, userId: String) {
        val where = Db.RoomMemberTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.RoomMemberTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userId)
        database.transaction {
            database.delete(Db.RoomMemberTable.TABLE_NAME, where, null)
        }
    }

    override fun deleteRoomMembers(roomId: String) {
        val where = Db.RoomMemberTable.COLUMN_ROOM_ID + " = ${DatabaseUtils.sqlEscapeString(roomId)}"
        database.transaction {
            database.delete(Db.RoomMemberTable.TABLE_NAME, where, null)
        }
    }

    override fun getRoomMembers(roomId: String): List<RoomMemberEntity> {
        val query = "SELECT * FROM " + Db.RoomMemberTable.TABLE_NAME + " WHERE " +
                Db.RoomMemberTable.COLUMN_ROOM_ID + " = ${DatabaseUtils.sqlEscapeString(roomId)}"
        val cursor = database.rawQuery(query, null)
        val members = ArrayList<RoomMemberEntity>()
        while (cursor.moveToNext()) {
            var roomMemberEntity = cursor.toRoomMemberEntity()
            roomMemberEntity = RoomMemberEntity(
                    userLocal.getUser(roomMemberEntity.userEntity.id)!!,
                    roomMemberEntity.memberStateEntity
            )
            members.add(roomMemberEntity)
        }
        cursor.close()
        return members
    }

    override fun getRooms(page: Int, limit: Int): List<RoomEntity> {
        val roomsTable = Db.RoomTable.TABLE_NAME
        val messagesTable = Db.MessageTable.TABLE_NAME
        val query = "SELECT DISTINCT ${Db.RoomTable.COLUMN_ID}, ${Db.RoomTable.COLUMN_UNIQUE_ID}, " +
                "${Db.RoomTable.COLUMN_NAME}, ${Db.RoomTable.COLUMN_IS_GROUP}, ${Db.RoomTable.COLUMN_OPTIONS}, " +
                "${Db.RoomTable.COLUMN_AVATAR_URL}, ${Db.RoomTable.COLUMN_UNREAD_COUNT}" +
                " FROM (SELECT $roomsTable.${Db.RoomTable.COLUMN_ID}, " +
                "$roomsTable.${Db.RoomTable.COLUMN_UNIQUE_ID}, $roomsTable.${Db.RoomTable.COLUMN_NAME}, " +
                "$roomsTable.${Db.RoomTable.COLUMN_IS_GROUP}, $roomsTable.${Db.RoomTable.COLUMN_OPTIONS}, " +
                "$roomsTable.${Db.RoomTable.COLUMN_AVATAR_URL}, $roomsTable.${Db.RoomTable.COLUMN_UNREAD_COUNT}," +
                "$messagesTable.${Db.MessageTable.COLUMN_TIME} FROM $roomsTable, $messagesTable WHERE " +
                "$roomsTable.${Db.RoomTable.COLUMN_ID} = $messagesTable.${Db.MessageTable.COLUMN_ROOM_ID} " +
                "ORDER BY $messagesTable.${Db.MessageTable.COLUMN_TIME} DESC) " +
                "ORDER BY ${Db.MessageTable.COLUMN_TIME} DESC LIMIT ${(page - 1) * limit},$limit"

        val cursor = database.rawQuery(query, null)
        val rooms = ArrayList<RoomEntity>()
        while (cursor.moveToNext()) {
            rooms.add(cursor.toRoomEntity())
        }
        cursor.close()
        return rooms
    }

    override fun getRoomsWithSpecificIds(roomIds: List<String>, channelIds: List<String>): List<RoomEntity> {
        if (roomIds.isEmpty() && channelIds.isEmpty()) {
            return arrayListOf()
        }

        var query = "SELECT * FROM ${Db.RoomTable.TABLE_NAME} WHERE "
        for (i in 0 until roomIds.size) {
            query += "${Db.RoomTable.COLUMN_ID} = ${DatabaseUtils.sqlEscapeString(roomIds[i])}"
            if (i < roomIds.size - 1) {
                query += " OR "
            }
        }

        if (roomIds.isNotEmpty() && channelIds.isNotEmpty()) {
            query += " OR "
        }

        for (i in 0 until channelIds.size) {
            query += "${Db.RoomTable.COLUMN_UNIQUE_ID} = ${DatabaseUtils.sqlEscapeString(channelIds[i])}"
            if (i < channelIds.size - 1) {
                query += " OR "
            }
        }

        val cursor = database.rawQuery(query, null)
        val rooms = ArrayList<RoomEntity>()
        while (cursor.moveToNext()) {
            rooms.add(cursor.toRoomEntity())
        }
        cursor.close()
        return rooms
    }

    override fun clearData() {
        database.transaction {
            database.delete(Db.RoomTable.TABLE_NAME, null, null)
            database.delete(Db.RoomMemberTable.TABLE_NAME, null, null)
        }
    }

    private fun isExistRoom(roomId: String): Boolean {
        val query = "SELECT * FROM ${Db.RoomTable.TABLE_NAME} WHERE ${Db.RoomTable.COLUMN_ID} " +
                "= ${DatabaseUtils.sqlEscapeString(roomId)}"
        val cursor = database.rawQuery(query, null)
        val contains = cursor.count > 0
        cursor.close()
        return contains
    }

    private fun isExistRoomMember(roomId: String, userId: String): Boolean {
        val query = "SELECT * FROM " + Db.RoomMemberTable.TABLE_NAME + " WHERE " +
                Db.RoomMemberTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.RoomMemberTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userId)
        val cursor = database.rawQuery(query, null)
        val contains = cursor.count > 0
        cursor.close()
        return contains
    }
}
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

package com.qiscus.sdk.chat.data.local.database

/**
 * Created on : September 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
object Db {
    private const val MAJOR_VERSION = 3
    private const val MINOR_VERSION = 1
    const val DATABASE_NAME = "qiscus_sdk_chat.db"
    const val DATABASE_VERSION = MAJOR_VERSION * 10 + MINOR_VERSION

    object RoomTable {
        const val TABLE_NAME = "rooms"
        const val COLUMN_ID = "id"
        const val COLUMN_UNIQUE_ID = "unique_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_IS_GROUP = "is_group"
        const val COLUMN_OPTIONS = "options"
        const val COLUMN_AVATAR_URL = "avatar_url"
        const val COLUMN_UNREAD_COUNT = "unread_count"

        const val CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY," +
                COLUMN_UNIQUE_ID + " TEXT DEFAULT 'default'," +
                COLUMN_NAME + " TEXT," +
                COLUMN_IS_GROUP + " INTEGER DEFAULT 0," +
                COLUMN_OPTIONS + " TEXT," +
                COLUMN_AVATAR_URL + " TEXT," +
                COLUMN_UNREAD_COUNT + " INTEGER DEFAULT 0" +
                " ); "
    }

    object UserTable {
        const val TABLE_NAME = "users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USER_NAME = "user_name"
        const val COLUMN_USER_AVATAR = "user_avatar"

        const val CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_USER_ID + " TEXT PRIMARY KEY," +
                COLUMN_USER_NAME + " TEXT," +
                COLUMN_USER_AVATAR + " TEXT" +
                " ); "
    }

    object ParticipantTable {
        const val TABLE_NAME = "participants"
        const val COLUMN_ROOM_ID = "room_id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_LAST_DELIVERED = "last_delivered_message"
        const val COLUMN_LAST_READ = "last_read_message"

        const val CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ROOM_ID + " TEXT," +
                COLUMN_USER_ID + " TEXT," +
                COLUMN_LAST_DELIVERED + " TEXT," +
                COLUMN_LAST_READ + " TEXT" +
                " ); "
    }

    object MessageTable {
        const val TABLE_NAME = "messages"
        const val COLUMN_ID = "id"
        const val COLUMN_ROOM_ID = "room_id"
        const val COLUMN_UNIQUE_ID = "unique_id"
        const val COLUMN_BEFORE_ID = "before_id"
        const val COLUMN_MESSAGE = "text"
        const val COLUMN_SENDER_ID = "sender_id"
        const val COLUMN_SENDER_NAME = "sender_name"
        const val COLUMN_SENDER_AVATAR = "sender_avatar"
        const val COLUMN_TIME = "time"
        const val COLUMN_STATE = "state"
        const val COLUMN_TYPE = "type"
        const val COLUMN_PAYLOAD = "payload"

        const val CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " TEXT," +
                COLUMN_ROOM_ID + " TEXT," +
                COLUMN_UNIQUE_ID + " TEXT unique," +
                COLUMN_BEFORE_ID + " TEXT," +
                COLUMN_MESSAGE + " TEXT," +
                COLUMN_SENDER_ID + " TEXT NOT NULL," +
                COLUMN_SENDER_NAME + " TEXT," +
                COLUMN_SENDER_AVATAR + " TEXT," +
                COLUMN_TIME + " LONG NOT NULL," +
                COLUMN_STATE + " INTEGER NOT NULL," +
                COLUMN_TYPE + " TEXT," +
                COLUMN_PAYLOAD + " TEXT" +
                " ); "
    }

    object FileTable {
        const val TABLE_NAME = "files"
        const val COLUMN_MESSAGE_UNIQUE_ID = "unique_id"
        const val COLUMN_LOCAL_PATH = "local_path"

        const val CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_MESSAGE_UNIQUE_ID + " TEXT PRIMARY KEY," +
                COLUMN_LOCAL_PATH + " TEXT NOT NULL" +
                " ); "
    }
}
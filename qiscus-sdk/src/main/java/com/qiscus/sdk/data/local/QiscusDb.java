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

package com.qiscus.sdk.data.local;

import android.content.ContentValues;
import android.database.Cursor;

import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;

import java.util.Date;

final class QiscusDb {
    static final String DATABASE_NAME = "qiscus.db";
    static final int DATABASE_VERSION = 2;

    static abstract class RoomTable {
        static final String TABLE_NAME = "rooms";
        static final String COLUMN_ID = "id";
        static final String COLUMN_DISTINCT_ID = "distinct_id";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_SUBTITLE = "subtitle";
        static final String COLUMN_IS_GROUP = "is_group";
        static final String COLUMN_OPTIONS = "options";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER," +
                        COLUMN_DISTINCT_ID + " TEXT DEFAULT 'default'," +
                        COLUMN_NAME + " TEXT," +
                        COLUMN_SUBTITLE + " TEXT," +
                        COLUMN_IS_GROUP + " INTEGER DEFAULT 0," +
                        COLUMN_OPTIONS + " TEXT" +
                        " ); ";

        static ContentValues toContentValues(QiscusChatRoom qiscusChatRoom) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, qiscusChatRoom.getId());
            values.put(COLUMN_DISTINCT_ID, qiscusChatRoom.getDistinctId());
            values.put(COLUMN_NAME, qiscusChatRoom.getName());
            values.put(COLUMN_SUBTITLE, qiscusChatRoom.getSubtitle());
            values.put(COLUMN_IS_GROUP, qiscusChatRoom.isGroup() ? 1 : 0);
            values.put(COLUMN_OPTIONS, qiscusChatRoom.getOptions());
            return values;
        }

        static QiscusChatRoom parseCursor(Cursor cursor) {
            QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
            qiscusChatRoom.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            qiscusChatRoom.setDistinctId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISTINCT_ID)));
            qiscusChatRoom.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            qiscusChatRoom.setSubtitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBTITLE)));
            qiscusChatRoom.setGroup(cursor.getShort(cursor.getColumnIndexOrThrow(COLUMN_IS_GROUP)) == 1);
            qiscusChatRoom.setOptions(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTIONS)));
            return qiscusChatRoom;
        }
    }

    static abstract class RoomMemberTable {
        static final String TABLE_NAME = "room_members";
        static final String COLUMN_ROOM_ID = "room_id";
        static final String COLUMN_USER_EMAIL = "user_email";
        static final String COLUMN_DISTINCT_ID = "distinct_id";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ROOM_ID + " INTEGER," +
                        COLUMN_USER_EMAIL + " TEXT," +
                        COLUMN_DISTINCT_ID + " TEXT DEFAULT 'default'" +
                        " ); ";

        static ContentValues toContentValues(int roomId, String userEmail) {
            return toContentValues(roomId, userEmail, "default");
        }

        static ContentValues toContentValues(int roomId, String userEmail, String distinctId) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ROOM_ID, roomId);
            values.put(COLUMN_USER_EMAIL, userEmail);
            values.put(COLUMN_DISTINCT_ID, distinctId);
            return values;
        }

        static int getRoomId(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ROOM_ID));
        }

        static String getMember(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL));
        }
    }

    static abstract class CommentTable {
        static final String TABLE_NAME = "comments";
        static final String COLUMN_ID = "id";
        static final String COLUMN_ROOM_ID = "room_id";
        static final String COLUMN_TOPIC_ID = "topic_id";
        static final String COLUMN_UNIQUE_ID = "unique_id";
        static final String COLUMN_COMMENT_BEFORE_ID = "comment_before_id";
        static final String COLUMN_MESSAGE = "message";
        static final String COLUMN_SENDER = "sender";
        static final String COLUMN_SENDER_EMAIL = "sender_email";
        static final String COLUMN_TIME = "time";
        static final String COLUMN_STATE = "state";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER," +
                        COLUMN_ROOM_ID + " INTEGER," +
                        COLUMN_TOPIC_ID + " INTEGER," +
                        COLUMN_UNIQUE_ID + " TEXT," +
                        COLUMN_COMMENT_BEFORE_ID + " INTEGER," +
                        COLUMN_MESSAGE + " TEXT," +
                        COLUMN_SENDER + " TEXT," +
                        COLUMN_SENDER_EMAIL + " TEXT NOT NULL," +
                        COLUMN_TIME + " LONG NOT NULL," +
                        COLUMN_STATE + " INTEGER NOT NULL" +
                        " ); ";

        static ContentValues toContentValues(QiscusComment qiscusComment) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, qiscusComment.getId());
            values.put(COLUMN_ROOM_ID, qiscusComment.getRoomId());
            values.put(COLUMN_TOPIC_ID, qiscusComment.getTopicId());
            values.put(COLUMN_UNIQUE_ID, qiscusComment.getUniqueId());
            values.put(COLUMN_COMMENT_BEFORE_ID, qiscusComment.getCommentBeforeId());
            values.put(COLUMN_MESSAGE, qiscusComment.getMessage());
            values.put(COLUMN_SENDER, qiscusComment.getSender());
            values.put(COLUMN_SENDER_EMAIL, qiscusComment.getSenderEmail());
            values.put(COLUMN_TIME, qiscusComment.getTime().getTime());
            values.put(COLUMN_STATE, qiscusComment.getState());
            return values;
        }

        static QiscusComment parseCursor(Cursor cursor) {
            QiscusComment qiscusComment = new QiscusComment();
            qiscusComment.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            qiscusComment.setRoomId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ROOM_ID)));
            qiscusComment.setTopicId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOPIC_ID)));
            qiscusComment.setUniqueId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID)));
            qiscusComment.setCommentBeforeId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_BEFORE_ID)));
            qiscusComment.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
            qiscusComment.setSender(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER)));
            qiscusComment.setSenderEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_EMAIL)));
            qiscusComment.setTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME))));
            qiscusComment.setState(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATE)));
            return qiscusComment;
        }
    }

    static abstract class FilesTable {
        static final String TABLE_NAME = "files";
        static final String COLUMN_COMMENT_ID = "comment_id";
        static final String COLUMN_TOPIC_ID = "topic_id";
        static final String COLUMN_LOCAL_PATH = "local_path";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_COMMENT_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_TOPIC_ID + " INTEGER NOT NULL," +
                        COLUMN_LOCAL_PATH + " TEXT NOT NULL" +
                        " ); ";

        static ContentValues toContentValues(int topicId, int commentId, String localPath) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TOPIC_ID, topicId);
            values.put(COLUMN_COMMENT_ID, commentId);
            values.put(COLUMN_LOCAL_PATH, localPath);
            return values;
        }

        static String parseCursor(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCAL_PATH));
        }
    }
}

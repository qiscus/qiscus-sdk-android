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
import com.qiscus.sdk.data.model.QiscusRoomMember;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

final class QiscusDb {
    static final String DATABASE_NAME = "qiscus.db";
    static final int DATABASE_VERSION = 12;

    abstract static class RoomTable {
        static final String TABLE_NAME = "rooms";
        static final String COLUMN_ID = "id";
        static final String COLUMN_DISTINCT_ID = "distinct_id";
        static final String COLUMN_UNIQUE_ID = "unique_id";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_IS_GROUP = "is_group";
        static final String COLUMN_OPTIONS = "options";
        static final String COLUMN_AVATAR_URL = "avatar_url";
        static final String COLUMN_UNREAD_COUNT = "unread_count";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " LONG," +
                        COLUMN_DISTINCT_ID + " TEXT DEFAULT 'default'," +
                        COLUMN_UNIQUE_ID + " TEXT," +
                        COLUMN_NAME + " TEXT," +
                        COLUMN_IS_GROUP + " INTEGER DEFAULT 0," +
                        COLUMN_OPTIONS + " TEXT," +
                        COLUMN_AVATAR_URL + " TEXT," +
                        COLUMN_UNREAD_COUNT + " INTEGER DEFAULT 0" +
                        " ); ";

        static ContentValues toContentValues(QiscusChatRoom qiscusChatRoom) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, qiscusChatRoom.getId());
            values.put(COLUMN_DISTINCT_ID, qiscusChatRoom.getDistinctId());
            values.put(COLUMN_UNIQUE_ID, qiscusChatRoom.getUniqueId());
            values.put(COLUMN_NAME, qiscusChatRoom.getName());
            values.put(COLUMN_IS_GROUP, qiscusChatRoom.isGroup() ? 1 : 0);
            values.put(COLUMN_OPTIONS, qiscusChatRoom.getOptions() == null ? null : qiscusChatRoom.getOptions().toString());
            values.put(COLUMN_AVATAR_URL, qiscusChatRoom.getAvatarUrl());
            values.put(COLUMN_UNREAD_COUNT, qiscusChatRoom.getUnreadCount());
            return values;
        }

        static QiscusChatRoom parseCursor(Cursor cursor) {
            QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
            qiscusChatRoom.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            qiscusChatRoom.setDistinctId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISTINCT_ID)));
            qiscusChatRoom.setUniqueId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID)));
            qiscusChatRoom.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            qiscusChatRoom.setGroup(cursor.getShort(cursor.getColumnIndexOrThrow(COLUMN_IS_GROUP)) == 1);
            try {
                String options = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTIONS));
                qiscusChatRoom.setOptions(options == null ? null : new JSONObject(options));
            } catch (JSONException ignored) {
                //Do nothing
            }
            qiscusChatRoom.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_URL)));
            qiscusChatRoom.setUnreadCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_UNREAD_COUNT)));
            return qiscusChatRoom;
        }
    }

    abstract static class MemberTable {
        static final String TABLE_NAME = "members";
        static final String COLUMN_USER_EMAIL = "user_email";
        static final String COLUMN_USER_NAME = "user_name";
        static final String COLUMN_USER_AVATAR = "user_avatar";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_USER_EMAIL + " TEXT," +
                        COLUMN_USER_NAME + " TEXT," +
                        COLUMN_USER_AVATAR + " TEXT" +
                        " ); ";

        static ContentValues toContentValues(QiscusRoomMember qiscusRoomMember) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_EMAIL, qiscusRoomMember.getEmail());
            values.put(COLUMN_USER_NAME, qiscusRoomMember.getUsername());
            values.put(COLUMN_USER_AVATAR, qiscusRoomMember.getAvatar());
            return values;
        }

        static QiscusRoomMember getMember(Cursor cursor) {
            QiscusRoomMember qiscusRoomMember = new QiscusRoomMember();
            qiscusRoomMember.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)));
            qiscusRoomMember.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
            qiscusRoomMember.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_AVATAR)));
            return qiscusRoomMember;
        }
    }

    abstract static class RoomMemberTable {
        static final String TABLE_NAME = "room_members";
        static final String COLUMN_ROOM_ID = "room_id";
        static final String COLUMN_USER_EMAIL = "user_email";
        static final String COLUMN_DISTINCT_ID = "distinct_id";
        static final String COLUMN_LAST_DELIVERED = "last_delivered";
        static final String COLUMN_LAST_READ = "last_read";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ROOM_ID + " LONG," +
                        COLUMN_USER_EMAIL + " TEXT," +
                        COLUMN_DISTINCT_ID + " TEXT DEFAULT 'default'," +
                        COLUMN_LAST_DELIVERED + " LONG DEFAULT 0," +
                        COLUMN_LAST_READ + " LONG DEFAULT 0" +
                        " ); ";

        static ContentValues toContentValues(long roomId, QiscusRoomMember roomMember) {
            return toContentValues(roomId, "default", roomMember);
        }

        static ContentValues toContentValues(long roomId, String distinctId, QiscusRoomMember roomMember) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ROOM_ID, roomId);
            values.put(COLUMN_DISTINCT_ID, distinctId);
            values.put(COLUMN_USER_EMAIL, roomMember.getEmail());
            values.put(COLUMN_LAST_DELIVERED, roomMember.getLastDeliveredCommentId());
            values.put(COLUMN_LAST_READ, roomMember.getLastReadCommentId());
            return values;
        }

        static long getRoomId(Cursor cursor) {
            return cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROOM_ID));
        }

        static String getUserEmail(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL));
        }

        static long getLastDeliveredCommentId(Cursor cursor) {
            return cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_DELIVERED));
        }

        static long getLastReadCommentId(Cursor cursor) {
            return cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_READ));
        }
    }

    abstract static class CommentTable {
        static final String TABLE_NAME = "comments";
        static final String COLUMN_ID = "id";
        static final String COLUMN_ROOM_ID = "room_id";
        static final String COLUMN_UNIQUE_ID = "unique_id";
        static final String COLUMN_COMMENT_BEFORE_ID = "comment_before_id";
        static final String COLUMN_MESSAGE = "message";
        static final String COLUMN_SENDER = "sender";
        static final String COLUMN_SENDER_EMAIL = "sender_email";
        static final String COLUMN_SENDER_AVATAR = "sender_avatar";
        static final String COLUMN_TIME = "time";
        static final String COLUMN_STATE = "state";
        static final String COLUMN_TYPE = "type";
        static final String COLUMN_PAYLOAD = "payload";
        static final String COLUMN_EXTRAS = "extras";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " LONG," +
                        COLUMN_ROOM_ID + " LONG," +
                        COLUMN_UNIQUE_ID + " TEXT," +
                        COLUMN_COMMENT_BEFORE_ID + " LONG," +
                        COLUMN_MESSAGE + " TEXT," +
                        COLUMN_SENDER + " TEXT," +
                        COLUMN_SENDER_EMAIL + " TEXT NOT NULL," +
                        COLUMN_SENDER_AVATAR + " TEXT," +
                        COLUMN_TIME + " LONG NOT NULL," +
                        COLUMN_STATE + " INTEGER NOT NULL," +
                        COLUMN_TYPE + " TEXT," +
                        COLUMN_PAYLOAD + " TEXT, " +
                        COLUMN_EXTRAS + " TEXT " +
                        " ); ";

        static ContentValues toContentValues(QiscusComment qiscusComment) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, qiscusComment.getId());
            values.put(COLUMN_ROOM_ID, qiscusComment.getRoomId());
            values.put(COLUMN_UNIQUE_ID, qiscusComment.getUniqueId());
            values.put(COLUMN_COMMENT_BEFORE_ID, qiscusComment.getCommentBeforeId());
            values.put(COLUMN_MESSAGE, qiscusComment.getMessage());
            values.put(COLUMN_SENDER, qiscusComment.getSender());
            values.put(COLUMN_SENDER_EMAIL, qiscusComment.getSenderEmail());
            values.put(COLUMN_SENDER_AVATAR, qiscusComment.getSenderAvatar());
            values.put(COLUMN_TIME, qiscusComment.getTime().getTime());
            values.put(COLUMN_STATE, qiscusComment.getState());
            values.put(COLUMN_TYPE, qiscusComment.getRawType());
            values.put(COLUMN_PAYLOAD, qiscusComment.getExtraPayload());
            values.put(COLUMN_EXTRAS, qiscusComment.getExtras() == null ? null :
                    qiscusComment.getExtras().toString());
            return values;
        }

        static QiscusComment parseCursor(Cursor cursor) {
            QiscusComment qiscusComment = new QiscusComment();
            qiscusComment.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            qiscusComment.setRoomId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROOM_ID)));
            qiscusComment.setUniqueId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID)));
            qiscusComment.setCommentBeforeId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_BEFORE_ID)));
            qiscusComment.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
            qiscusComment.setSender(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER)));
            qiscusComment.setSenderEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_EMAIL)));
            qiscusComment.setSenderAvatar(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_AVATAR)));
            qiscusComment.setTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME))));
            qiscusComment.setState(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATE)));
            qiscusComment.setRawType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
            qiscusComment.setExtraPayload(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYLOAD)));
            try {
                String extras = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXTRAS));
                qiscusComment.setExtras(extras == null ? null : new JSONObject(extras));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return qiscusComment;
        }
    }

    abstract static class FilesTable {
        static final String TABLE_NAME = "files";
        static final String COLUMN_COMMENT_ID = "comment_id";
        static final String COLUMN_ROOM_ID = "room_id";
        static final String COLUMN_LOCAL_PATH = "local_path";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_COMMENT_ID + " LONG PRIMARY KEY," +
                        COLUMN_ROOM_ID + " LONG NOT NULL," +
                        COLUMN_LOCAL_PATH + " TEXT NOT NULL" +
                        " ); ";

        static ContentValues toContentValues(long roomId, long commentId, String localPath) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ROOM_ID, roomId);
            values.put(COLUMN_COMMENT_ID, commentId);
            values.put(COLUMN_LOCAL_PATH, localPath);
            return values;
        }

        static String parseCursor(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCAL_PATH));
        }
    }
}

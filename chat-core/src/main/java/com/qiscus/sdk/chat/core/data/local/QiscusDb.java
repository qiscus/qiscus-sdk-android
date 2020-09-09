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

package com.qiscus.sdk.chat.core.data.local;

import android.content.ContentValues;
import android.database.Cursor;

import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.data.model.QUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

final class QiscusDb {
    static final String DATABASE_NAME = "qiscus.db";
    static final int DATABASE_VERSION = 19;

    abstract static class RoomTable {
        static final String TABLE_NAME = "rooms";
        static final String COLUMN_ID = "id";
        static final String COLUMN_UNIQUE_ID = "unique_id";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_TYPE = "type";
        static final String COLUMN_OPTIONS = "options";
        static final String COLUMN_AVATAR_URL = "avatar_url";
        static final String COLUMN_UNREAD_COUNT = "unread_count";
        static final String COLUMN_MEMBER_COUNT = "member_count";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " LONG PRIMARY KEY," +
                        COLUMN_UNIQUE_ID + " TEXT," +
                        COLUMN_NAME + " TEXT," +
                        COLUMN_TYPE + " TEXT," +
                        COLUMN_OPTIONS + " TEXT," +
                        COLUMN_AVATAR_URL + " TEXT," +
                        COLUMN_UNREAD_COUNT + " INTEGER DEFAULT 0," +
                        COLUMN_MEMBER_COUNT + " INTEGER DEFAULT 0" +
                        " ); ";

        static ContentValues toContentValues(QChatRoom qChatRoom) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, qChatRoom.getId());
            values.put(COLUMN_UNIQUE_ID, qChatRoom.getUniqueId());
            values.put(COLUMN_NAME, qChatRoom.getName());
            values.put(COLUMN_TYPE, qChatRoom.getType());
            values.put(COLUMN_OPTIONS, qChatRoom.getExtras() == null ? null : qChatRoom.getExtras().toString());
            values.put(COLUMN_AVATAR_URL, qChatRoom.getAvatarUrl());
            values.put(COLUMN_UNREAD_COUNT, qChatRoom.getUnreadCount());
            values.put(COLUMN_MEMBER_COUNT, qChatRoom.getTotalParticipants());
            return values;
        }

        static QChatRoom parseCursor(Cursor cursor) {
            QChatRoom qChatRoom = new QChatRoom();
            qChatRoom.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            qChatRoom.setUniqueId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID)));
            qChatRoom.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            qChatRoom.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
            try {
                String options = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTIONS));
                qChatRoom.setExtras(options == null ? null : new JSONObject(options));
            } catch (JSONException ignored) {
                //Do nothing
            }
            qChatRoom.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_URL)));
            qChatRoom.setUnreadCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_UNREAD_COUNT)));
            qChatRoom.setTotalParticipants(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MEMBER_COUNT)));
            return qChatRoom;
        }
    }

    abstract static class MemberTable {
        static final String TABLE_NAME = "members";
        static final String COLUMN_USER_EMAIL = "user_email";
        static final String COLUMN_USER_NAME = "user_name";
        static final String COLUMN_USER_AVATAR = "user_avatar";
        static final String COLUMN_USER_EXTRAS = "user_extras";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_USER_EMAIL + " TEXT PRIMARY KEY," +
                        COLUMN_USER_NAME + " TEXT," +
                        COLUMN_USER_AVATAR + " TEXT," +
                        COLUMN_USER_EXTRAS + " TEXT" +
                        " ); ";

        static ContentValues toContentValues(QParticipant qParticipant) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_EMAIL, qParticipant.getId());
            values.put(COLUMN_USER_NAME, qParticipant.getName());
            values.put(COLUMN_USER_AVATAR, qParticipant.getAvatarUrl());
            values.put(COLUMN_USER_EXTRAS, qParticipant.getExtras() == null ? null :
                    qParticipant.getExtras().toString());
            return values;
        }

        static QParticipant getMember(Cursor cursor) {
            QParticipant qParticipant = new QParticipant();
            qParticipant.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)));
            qParticipant.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
            qParticipant.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_AVATAR)));
            try {
                String extras = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EXTRAS));
                qParticipant.setExtras(extras == null ? null : new JSONObject(extras));
            } catch (JSONException ignored) {
                //Do nothing
            }
            return qParticipant;
        }
    }

    abstract static class RoomMemberTable {
        static final String TABLE_NAME = "room_members";
        static final String COLUMN_ROOM_ID = "room_id";
        static final String COLUMN_USER_EMAIL = "user_email";
        static final String COLUMN_LAST_DELIVERED = "last_delivered";
        static final String COLUMN_LAST_READ = "last_read";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ROOM_ID + " LONG," +
                        COLUMN_USER_EMAIL + " TEXT," +
                        COLUMN_LAST_DELIVERED + " LONG DEFAULT 0," +
                        COLUMN_LAST_READ + " LONG DEFAULT 0," +
                        "PRIMARY KEY (" + COLUMN_ROOM_ID + ", " + COLUMN_USER_EMAIL + ")" +
                        " ); ";

        static ContentValues toContentValues(long roomId, QParticipant roomMember) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ROOM_ID, roomId);
            values.put(COLUMN_USER_EMAIL, roomMember.getId());
            values.put(COLUMN_LAST_DELIVERED, roomMember.getLastMessageDeliveredId());
            values.put(COLUMN_LAST_READ, roomMember.getLastMessageReadId());
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
        static final String COLUMN_DELETED = "deleted";
        static final String COLUMN_HARD_DELETED = "hard_deleted";
        static final String COLUMN_TYPE = "type";
        static final String COLUMN_PAYLOAD = "payload";
        static final String COLUMN_EXTRAS = "extras";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " LONG," +
                        COLUMN_ROOM_ID + " LONG," +
                        COLUMN_UNIQUE_ID + " TEXT PRIMARY KEY," +
                        COLUMN_COMMENT_BEFORE_ID + " LONG," +
                        COLUMN_MESSAGE + " TEXT," +
                        COLUMN_SENDER + " TEXT," +
                        COLUMN_SENDER_EMAIL + " TEXT NOT NULL," +
                        COLUMN_SENDER_AVATAR + " TEXT," +
                        COLUMN_TIME + " LONG NOT NULL," +
                        COLUMN_STATE + " INTEGER NOT NULL," +
                        COLUMN_DELETED + " INTEGER DEFAULT 0," +
                        COLUMN_HARD_DELETED + " INTEGER DEFAULT 0," +
                        COLUMN_TYPE + " TEXT," +
                        COLUMN_PAYLOAD + " TEXT, " +
                        COLUMN_EXTRAS + " TEXT " +
                        " ); ";

        static ContentValues toContentValues(QMessage qiscusMessage) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, qiscusMessage.getId());
            values.put(COLUMN_ROOM_ID, qiscusMessage.getChatRoomId());
            values.put(COLUMN_UNIQUE_ID, qiscusMessage.getUniqueId());
            values.put(COLUMN_COMMENT_BEFORE_ID, qiscusMessage.getPreviousMessageId());
            values.put(COLUMN_MESSAGE, qiscusMessage.getText());
            values.put(COLUMN_SENDER, qiscusMessage.getSender().getName());
            values.put(COLUMN_SENDER_EMAIL, qiscusMessage.getSender().getId());
            values.put(COLUMN_SENDER_AVATAR, qiscusMessage.getSender().getAvatarUrl());
            values.put(COLUMN_TIME, qiscusMessage.getTimestamp().getTime());
            values.put(COLUMN_STATE, qiscusMessage.getStatus());
            values.put(COLUMN_DELETED, qiscusMessage.isDeleted() ? 1 : 0);
            values.put(COLUMN_HARD_DELETED, qiscusMessage.isDeleted() ? 1 : 0);
            values.put(COLUMN_TYPE, qiscusMessage.getRawType());
            values.put(COLUMN_PAYLOAD, qiscusMessage.getPayload());
            values.put(COLUMN_EXTRAS, qiscusMessage.getExtras() == null ? null :
                    qiscusMessage.getExtras().toString());
            return values;
        }

        static QMessage parseCursor(Cursor cursor) {
            QMessage qiscusMessage = new QMessage();
            qiscusMessage.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            qiscusMessage.setChatRoomId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROOM_ID)));
            qiscusMessage.setUniqueId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID)));
            qiscusMessage.setPreviousMessageId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_BEFORE_ID)));
            qiscusMessage.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));

            QUser qUser = new QUser();
            qUser.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER)));
            qUser.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_AVATAR)));
            try {
                String extras = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXTRAS));
                qUser.setExtras(extras == null ? null : new JSONObject(extras));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            qUser.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_EMAIL)));
            qiscusMessage.setSender(qUser);
            qiscusMessage.setTimestamp(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME))));
            qiscusMessage.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATE)));
            qiscusMessage.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DELETED)) == 1);
            qiscusMessage.setRawType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
            qiscusMessage.setPayload(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYLOAD)));
            try {
                String extras = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXTRAS));
                qiscusMessage.setExtras(extras == null ? null : new JSONObject(extras));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return qiscusMessage;
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

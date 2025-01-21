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

import static com.qiscus.sdk.chat.core.data.local.QiscusDataManagement.getBoolean;
import static com.qiscus.sdk.chat.core.data.local.QiscusDataManagement.getInteger;
import static com.qiscus.sdk.chat.core.data.local.QiscusDataManagement.getLong;
import static com.qiscus.sdk.chat.core.data.local.QiscusDataManagement.getString;
import static com.qiscus.sdk.chat.core.data.local.QiscusDataManagement.set;

import android.content.ContentValues;
import android.database.Cursor;

import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.util.QiscusLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

final class QiscusDb {

    static final String DATABASE_NAME = "qiscus.db";
    static final int DATABASE_VERSION = 20;
    static final int DATABASE_MINIMUM_VERSION = 20;
    private static final String JSON_EMPTY_FORMAT = "{}";
    private static final String TAG = QiscusDb.class.getSimpleName();

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
        static final String COLUMN_IS_CHANNEL = "is_channel";
        static final String COLUMN_MEMBER_COUNT = "member_count";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " TEXT PRIMARY KEY," +
                        COLUMN_DISTINCT_ID + " TEXT DEFAULT 'default'," +
                        COLUMN_UNIQUE_ID + " TEXT," +
                        COLUMN_NAME + " TEXT," +
                        COLUMN_IS_GROUP + " TEXT DEFAULT '0'," +
                        COLUMN_OPTIONS + " TEXT," +
                        COLUMN_AVATAR_URL + " TEXT," +
                        COLUMN_UNREAD_COUNT + " TEXT DEFAULT '0'," +
                        COLUMN_IS_CHANNEL + " TEXT DEFAULT '0'," +
                        COLUMN_MEMBER_COUNT + " TEXT DEFAULT '0'" +
                        " ); ";

        static ContentValues toContentValues(QiscusChatRoom qiscusChatRoom) {
            final ContentValues values = new ContentValues();
            values.put(COLUMN_ID, qiscusChatRoom.getId());
            values.put(COLUMN_DISTINCT_ID, qiscusChatRoom.getDistinctId());
            values.put(COLUMN_UNIQUE_ID, qiscusChatRoom.getUniqueId());
            values.put(COLUMN_NAME, set(qiscusChatRoom.getName()));
            values.put(COLUMN_IS_GROUP, set(qiscusChatRoom.isGroup()));
            values.put(COLUMN_OPTIONS, set(
                    qiscusChatRoom.getOptions() == null ? null : qiscusChatRoom.getOptions().toString())
            );
            values.put(COLUMN_AVATAR_URL, set(qiscusChatRoom.getAvatarUrl()));
            values.put(COLUMN_UNREAD_COUNT, set(qiscusChatRoom.getUnreadCount()));
            values.put(COLUMN_IS_CHANNEL, set(qiscusChatRoom.isChannel()));
            values.put(COLUMN_MEMBER_COUNT, set(qiscusChatRoom.getMemberCount()));
            return values;
        }

        static QiscusChatRoom parseCursor(Cursor cursor) {
            final QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
            qiscusChatRoom.setId(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            );
            qiscusChatRoom.setDistinctId(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISTINCT_ID))
            );
            qiscusChatRoom.setUniqueId(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID))
            );
            qiscusChatRoom.setName(getString(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            ));
            qiscusChatRoom.setGroup(getBoolean(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IS_GROUP))
            ));
            try {
                final String options = getString(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTIONS)), JSON_EMPTY_FORMAT
                );
                qiscusChatRoom.setOptions(new JSONObject(options));
            } catch (JSONException e) {
                QiscusLogger.print(TAG, e.getMessage());
            }
            qiscusChatRoom.setAvatarUrl(getString(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_URL))
            ));
            qiscusChatRoom.setUnreadCount(getInteger(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNREAD_COUNT))
            ));
            qiscusChatRoom.setChannel(getBoolean(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IS_CHANNEL))
            ));
            qiscusChatRoom.setMemberCount(getInteger(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMBER_COUNT))
            ));
            return qiscusChatRoom;
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

        static ContentValues toContentValues(QiscusRoomMember qiscusRoomMember) {
            final ContentValues values = new ContentValues();
            values.put(COLUMN_USER_EMAIL, qiscusRoomMember.getEmail());
            values.put(COLUMN_USER_NAME, set(qiscusRoomMember.getUsername()));
            values.put(COLUMN_USER_AVATAR, set(qiscusRoomMember.getAvatar()));
            values.put(COLUMN_USER_EXTRAS, set(
                    qiscusRoomMember.getExtras() == null ?
                            null : qiscusRoomMember.getExtras().toString()
            ));
            return values;
        }

        static QiscusRoomMember getMember(Cursor cursor) {
            final QiscusRoomMember qiscusRoomMember = new QiscusRoomMember();
            qiscusRoomMember.setEmail(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL))
            );
            qiscusRoomMember.setUsername(getString(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME))
            ));
            qiscusRoomMember.setAvatar(getString(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_AVATAR))
            ));
            try {
                final String extras = getString(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EXTRAS)), JSON_EMPTY_FORMAT
                );
                qiscusRoomMember.setExtras(new JSONObject(extras));

            } catch (JSONException e) {
                QiscusLogger.print(TAG, e.getMessage());
            }
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
                        COLUMN_LAST_DELIVERED + " TEXT DEFAULT '0'," +
                        COLUMN_LAST_READ + " TEXT DEFAULT '0'," +
                        "PRIMARY KEY (" + COLUMN_ROOM_ID + ", " + COLUMN_USER_EMAIL + ")" +
                        " ); ";

        static ContentValues toContentValues(
                long roomId, QiscusRoomMember roomMember
        ) {
            return toContentValues(roomId, "default", roomMember);
        }

        static ContentValues toContentValues(
                long roomId, String distinctId, QiscusRoomMember roomMember
        ) {
            final ContentValues values = new ContentValues();
            values.put(COLUMN_ROOM_ID, roomId);
            values.put(COLUMN_USER_EMAIL, roomMember.getEmail());
            values.put(COLUMN_DISTINCT_ID, distinctId);
            values.put(COLUMN_LAST_DELIVERED, set(roomMember.getLastDeliveredCommentId()));
            values.put(COLUMN_LAST_READ, set(roomMember.getLastReadCommentId()));

            return values;
        }

        static long getRoomId(Cursor cursor) {
            return cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROOM_ID));
        }

        static String getUserEmail(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL));
        }

        static long getLastDeliveredCommentId(Cursor cursor) {
            return getLong(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_DELIVERED)));
        }

        static long getLastReadCommentId(Cursor cursor) {
            return getLong(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_READ)));
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
        static final String COLUMN_USER_EXTRAS = "user_extras";

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
                        COLUMN_STATE + " TEXT NOT NULL," +
                        COLUMN_DELETED + " TEXT DEFAULT '0'," +
                        COLUMN_HARD_DELETED + " TEXT DEFAULT '0'," +
                        COLUMN_TYPE + " TEXT," +
                        COLUMN_PAYLOAD + " TEXT," +
                        COLUMN_EXTRAS + " TEXT," +
                        COLUMN_USER_EXTRAS + " TEXT" +
                        ");";

        static ContentValues toContentValues(QiscusComment qiscusComment) {
            final ContentValues values = new ContentValues();
            values.put(COLUMN_ID, qiscusComment.getId());
            values.put(COLUMN_ROOM_ID, qiscusComment.getRoomId());
            values.put(COLUMN_UNIQUE_ID, qiscusComment.getUniqueId());
            values.put(COLUMN_COMMENT_BEFORE_ID, qiscusComment.getCommentBeforeId());
            values.put(COLUMN_MESSAGE, set(qiscusComment.getMessage()));
            values.put(COLUMN_SENDER, set(qiscusComment.getSender()));
            values.put(COLUMN_SENDER_EMAIL, set(qiscusComment.getSenderEmail()));
            values.put(COLUMN_SENDER_AVATAR, set(qiscusComment.getSenderAvatar()));
            values.put(COLUMN_TIME, qiscusComment.getTime().getTime());
            values.put(COLUMN_STATE, set(qiscusComment.getState()));
            values.put(COLUMN_DELETED, set(qiscusComment.isDeleted()));
            values.put(COLUMN_HARD_DELETED, set(qiscusComment.isHardDeleted()));
            values.put(COLUMN_TYPE, set(qiscusComment.getRawType()));
            values.put(COLUMN_PAYLOAD, set(qiscusComment.getExtraPayload()));
            values.put(COLUMN_EXTRAS, set(
                    qiscusComment.getExtras() == null ? null : qiscusComment.getExtras().toString())
            );
            values.put(COLUMN_USER_EXTRAS, set(
                    qiscusComment.getUserExtras() == null ? null : qiscusComment.getUserExtras().toString())
            );
            return values;
        }

        static QiscusComment parseCursor(Cursor cursor) {
            final QiscusComment qiscusComment = new QiscusComment();
            qiscusComment.setId(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            );
            qiscusComment.setRoomId(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROOM_ID))
            );
            qiscusComment.setUniqueId(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID))
            );
            qiscusComment.setCommentBeforeId(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_BEFORE_ID))
            );
            qiscusComment.setMessage(getString(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE))
            ));
            qiscusComment.setSender(getString(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER))
            ));
            qiscusComment.setSenderEmail(getString(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_EMAIL))
            ));
            qiscusComment.setSenderAvatar(getString(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_AVATAR))
            ));
            qiscusComment.setTime(new Date(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME))
            ));
            qiscusComment.setState(getInteger(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATE))
            ));
            qiscusComment.setDeleted(getBoolean(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELETED))
            ));
            qiscusComment.setHardDeleted(getBoolean(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HARD_DELETED))
            ));
            qiscusComment.setRawType(getString(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
            ));
            qiscusComment.setExtraPayload(getString(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYLOAD))
            ));
            try {
                final String extras = getString(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXTRAS)), JSON_EMPTY_FORMAT
                );
                qiscusComment.setExtras(new JSONObject(extras));
                final String userExtras = getString(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EXTRAS)), JSON_EMPTY_FORMAT
                );
                qiscusComment.setUserExtras(new JSONObject(userExtras));

            } catch (JSONException e) {
                QiscusLogger.print(TAG, e.getMessage());
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
                        COLUMN_COMMENT_ID + " TEXT PRIMARY KEY," +
                        COLUMN_ROOM_ID + " TEXT NOT NULL," +
                        COLUMN_LOCAL_PATH + " TEXT NOT NULL" +
                        " ); ";

        static ContentValues toContentValues(
                long roomId, long commentId, String localPath
        ) {
            final ContentValues values = new ContentValues();
            values.put(COLUMN_ROOM_ID, roomId);
            values.put(COLUMN_COMMENT_ID, commentId);
            values.put(COLUMN_LOCAL_PATH, set(localPath));
            return values;
        }

        static String parseCursor(Cursor cursor) {
            return getString(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCAL_PATH)));
        }
    }

}

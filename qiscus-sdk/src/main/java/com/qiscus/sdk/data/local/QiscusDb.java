package com.qiscus.sdk.data.local;

import android.content.ContentValues;
import android.database.Cursor;

import com.qiscus.sdk.data.model.QiscusComment;

import java.util.Date;

public class QiscusDb {
    public static final String DATABASE_NAME = "qiscus.db";
    public static final int DATABASE_VERSION = 1;

    public static abstract class CommentTable {
        public static final String TABLE_NAME = "comments";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ROOM_ID = "room_id";
        public static final String COLUMN_TOPIC_ID = "topic_id";
        public static final String COLUMN_UNIQUE_ID = "unique_id";
        public static final String COLUMN_COMMENT_BEFORE_ID = "comment_before_id";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_SENDER = "sender";
        public static final String COLUMN_SENDER_EMAIL = "sender_email";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_STATE = "state";

        public static final String CREATE =
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

        public static ContentValues toContentValues(QiscusComment qiscusComment) {
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

        public static QiscusComment parseCursor(Cursor cursor) {
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

    public static abstract class FilesTable {
        public static final String TABLE_NAME = "files";
        public static final String COLUMN_COMMENT_ID = "comment_id";
        public static final String COLUMN_TOPIC_ID = "topic_id";
        public static final String COLUMN_LOCAL_PATH = "local_path";

        public static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_COMMENT_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_TOPIC_ID + " INTEGER NOT NULL," +
                        COLUMN_LOCAL_PATH + " TEXT NOT NULL" +
                        " ); ";

        public static ContentValues toContentValues(int topicId, int commentId, String localPath) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TOPIC_ID, topicId);
            values.put(COLUMN_COMMENT_ID, commentId);
            values.put(COLUMN_LOCAL_PATH, localPath);
            return values;
        }

        public static String parseCursor(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCAL_PATH));
        }
    }
}

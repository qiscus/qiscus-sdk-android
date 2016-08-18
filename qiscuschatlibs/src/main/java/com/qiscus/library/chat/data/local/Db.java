package com.qiscus.library.chat.data.local;

import android.content.ContentValues;
import android.database.Cursor;

import com.qiscus.library.chat.data.model.Comment;

import java.util.Date;

public class Db {
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

        public static ContentValues toContentValues(Comment comment) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, comment.getId());
            values.put(COLUMN_ROOM_ID, comment.getRoomId());
            values.put(COLUMN_TOPIC_ID, comment.getTopicId());
            values.put(COLUMN_UNIQUE_ID, comment.getUniqueId());
            values.put(COLUMN_COMMENT_BEFORE_ID, comment.getCommentBeforeId());
            values.put(COLUMN_MESSAGE, comment.getMessage());
            values.put(COLUMN_SENDER, comment.getSender());
            values.put(COLUMN_SENDER_EMAIL, comment.getSenderEmail());
            values.put(COLUMN_TIME, comment.getTime().getTime());
            values.put(COLUMN_STATE, comment.getState());
            return values;
        }

        public static Comment parseCursor(Cursor cursor) {
            Comment comment = new Comment();
            comment.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            comment.setRoomId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ROOM_ID)));
            comment.setTopicId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOPIC_ID)));
            comment.setUniqueId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID)));
            comment.setCommentBeforeId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_BEFORE_ID)));
            comment.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
            comment.setSender(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER)));
            comment.setSenderEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_EMAIL)));
            comment.setTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME))));
            comment.setState(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATE)));
            return comment;
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

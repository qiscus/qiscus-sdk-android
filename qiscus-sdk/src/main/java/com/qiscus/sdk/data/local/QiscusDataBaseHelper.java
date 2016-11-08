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

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class QiscusDataBaseHelper implements QiscusDataStore {

    protected final SQLiteDatabase sqLiteDatabase;

    public QiscusDataBaseHelper() {
        QiscusDbOpenHelper qiscusDbOpenHelper = new QiscusDbOpenHelper(Qiscus.getApps());
        sqLiteDatabase = qiscusDbOpenHelper.getReadableDatabase();
    }

    @Override
    public void add(QiscusChatRoom qiscusChatRoom) {
        if (!isContains(qiscusChatRoom)) {
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.insert(QiscusDb.RoomTable.TABLE_NAME, null, QiscusDb.RoomTable.toContentValues(qiscusChatRoom));
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
            for (String member : qiscusChatRoom.getMember()) {
                addRoomMember(qiscusChatRoom.getId(), member, qiscusChatRoom.getDistinctId());
            }
        }
    }

    @Override
    public boolean isContains(QiscusChatRoom qiscusChatRoom) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomTable.TABLE_NAME + " WHERE "
                + QiscusDb.RoomTable.COLUMN_ID + " = " + qiscusChatRoom.getId();
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    @Override
    public void update(QiscusChatRoom qiscusChatRoom) {
        String where = QiscusDb.RoomTable.COLUMN_ID + " = " + qiscusChatRoom.getId();
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.update(QiscusDb.RoomTable.TABLE_NAME, QiscusDb.RoomTable.toContentValues(qiscusChatRoom), where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
        for (String member : qiscusChatRoom.getMember()) {
            addRoomMember(qiscusChatRoom.getId(), member, qiscusChatRoom.getDistinctId());
        }
    }

    @Override
    public void addOrUpdate(QiscusChatRoom qiscusChatRoom) {
        if (!isContains(qiscusChatRoom)) {
            add(qiscusChatRoom);
        } else {
            update(qiscusChatRoom);
        }
    }

    @Override
    public QiscusChatRoom getChatRoom(int id) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomTable.TABLE_NAME + " WHERE "
                + QiscusDb.RoomTable.COLUMN_ID + " = " + id;

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToNext()) {
            QiscusChatRoom qiscusChatRoom = QiscusDb.RoomTable.parseCursor(cursor);
            qiscusChatRoom.setMember(getRoomMembers(id));
            QiscusComment latestComment = getLatestComment(id);
            qiscusChatRoom.setLastCommentId(latestComment.getId());
            qiscusChatRoom.setLastCommentMessage(latestComment.getMessage());
            qiscusChatRoom.setLastCommentSender(latestComment.getSender());
            qiscusChatRoom.setLastCommentSenderEmail(latestComment.getSenderEmail());
            qiscusChatRoom.setLastCommentTime(latestComment.getTime());
            qiscusChatRoom.setLastTopicId(latestComment.getTopicId());
            cursor.close();
            return qiscusChatRoom;
        } else {
            cursor.close();
            return null;
        }
    }

    @Override
    public QiscusChatRoom getChatRoom(String email) {
        return getChatRoom(email, "default");
    }

    @Override
    public QiscusChatRoom getChatRoom(String email, String distinctId) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomMemberTable.TABLE_NAME + " WHERE "
                + QiscusDb.RoomMemberTable.COLUMN_DISTINCT_ID + " = " + DatabaseUtils.sqlEscapeString(distinctId) + " "
                + "AND " + QiscusDb.RoomMemberTable.COLUMN_USER_EMAIL
                + " = " + DatabaseUtils.sqlEscapeString(email);

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToNext()) {
            return getChatRoom(QiscusDb.RoomMemberTable.getRoomId(cursor));
        } else {
            cursor.close();
            return null;
        }
    }

    @Override
    public List<QiscusChatRoom> getChatRooms(int count) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomTable.TABLE_NAME + " "
                + "LIMIT " + count;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusChatRoom> qiscusChatRooms = new ArrayList<>();
        while (cursor.moveToNext()) {
            QiscusChatRoom qiscusChatRoom = QiscusDb.RoomTable.parseCursor(cursor);
            qiscusChatRoom.setMember(getRoomMembers(qiscusChatRoom.getId()));
            qiscusChatRooms.add(qiscusChatRoom);
        }
        cursor.close();
        return qiscusChatRooms;
    }

    @Override
    public Observable<List<QiscusChatRoom>> getObservableChatRooms(int count) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getChatRooms(count));
            subscriber.onCompleted();
        });
    }

    @Override
    public void addRoomMember(int roomId, String email, String distinctId) {
        distinctId = distinctId == null ? "default" : distinctId;
        if (!isContainsRoomMember(roomId, email)) {
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.insert(QiscusDb.RoomMemberTable.TABLE_NAME, null,
                        QiscusDb.RoomMemberTable.toContentValues(roomId, email, distinctId));
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }

    @Override
    public boolean isContainsRoomMember(int roomId, String email) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomMemberTable.TABLE_NAME + " WHERE "
                + QiscusDb.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomId + " "
                + "AND " + QiscusDb.RoomMemberTable.COLUMN_USER_EMAIL
                + " = " + DatabaseUtils.sqlEscapeString(email);
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    @Override
    public List<String> getRoomMembers(int roomId) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomMemberTable.TABLE_NAME + " "
                + "WHERE " + QiscusDb.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomId;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<String> members = new ArrayList<>();
        while (cursor.moveToNext()) {
            members.add(QiscusDb.RoomMemberTable.getMember(cursor));
        }
        cursor.close();
        return members;
    }

    @Override
    public void deleteRoomMember(int roomId, String email) {
        String where = QiscusDb.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomId + " "
                + "AND " + QiscusDb.RoomMemberTable.COLUMN_USER_EMAIL + " = " + DatabaseUtils.sqlEscapeString(email);

        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(QiscusDb.RoomMemberTable.TABLE_NAME, where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void add(QiscusComment qiscusComment) {
        if (!isContains(qiscusComment)) {
            sqLiteDatabase.beginTransaction();
            try {
                if (qiscusComment.getState() == QiscusComment.STATE_ON_QISCUS) {
                    qiscusComment.setState(QiscusComment.STATE_ON_PUSHER);
                }
                sqLiteDatabase.insert(QiscusDb.CommentTable.TABLE_NAME, null, QiscusDb.CommentTable.toContentValues(qiscusComment));
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }

    @Override
    public void saveLocalPath(int topicId, int commentId, String localPath) {
        if (!isContainsFileOfComment(commentId)) {
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.insert(QiscusDb.FilesTable.TABLE_NAME, null,
                        QiscusDb.FilesTable.toContentValues(topicId, commentId, localPath));
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }

    @Override
    public boolean isContains(QiscusComment qiscusComment) {
        String query;
        if (qiscusComment.getId() == -1) {
            query = "SELECT * FROM "
                    + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                    + QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        } else {
            query = "SELECT * FROM "
                    + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                    + QiscusDb.CommentTable.COLUMN_ID + " = " + qiscusComment.getId() + " OR "
                    + QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        }
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    @Override
    public boolean isContainsFileOfComment(int commentId) {
        String query = "SELECT * FROM "
                + QiscusDb.FilesTable.TABLE_NAME + " WHERE "
                + QiscusDb.FilesTable.COLUMN_COMMENT_ID + " = " + commentId + "";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    @Override
    public void update(QiscusComment qiscusComment) {
        String where;
        if (qiscusComment.getId() == -1) {
            where = QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        } else {
            where = QiscusDb.CommentTable.COLUMN_ID + " = " + qiscusComment.getId() + " OR "
                    + QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        }

        sqLiteDatabase.beginTransaction();
        try {
            if (qiscusComment.getState() == QiscusComment.STATE_ON_QISCUS) {
                qiscusComment.setState(QiscusComment.STATE_ON_PUSHER);
            }
            sqLiteDatabase.update(QiscusDb.CommentTable.TABLE_NAME, QiscusDb.CommentTable.toContentValues(qiscusComment), where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void updateLocalPath(int topicId, int commentId, String localPath) {
        String where = QiscusDb.FilesTable.COLUMN_COMMENT_ID + " = " + commentId + "";
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.update(QiscusDb.FilesTable.TABLE_NAME, QiscusDb.FilesTable.toContentValues(topicId, commentId, localPath), where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void addOrUpdate(QiscusComment qiscusComment) {
        if (!isContains(qiscusComment)) {
            add(qiscusComment);
        } else {
            update(qiscusComment);
        }
    }

    @Override
    public void addOrUpdateLocalPath(int topicId, int commentId, String localPath) {
        if (!isContainsFileOfComment(commentId)) {
            saveLocalPath(topicId, commentId, localPath);
        } else {
            updateLocalPath(topicId, commentId, localPath);
        }
    }

    @Override
    public void delete(QiscusComment qiscusComment) {
        String where;
        if (qiscusComment.getId() == -1) {
            where = QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        } else {
            where = QiscusDb.CommentTable.COLUMN_ID + " = " + qiscusComment.getId() + " OR "
                    + QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        }

        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(QiscusDb.CommentTable.TABLE_NAME, where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public File getLocalPath(int commentId) {
        String query = "SELECT * FROM "
                + QiscusDb.FilesTable.TABLE_NAME + " WHERE "
                + QiscusDb.FilesTable.COLUMN_COMMENT_ID + " = " + commentId + "";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToNext()) {
            File file = new File(QiscusDb.FilesTable.parseCursor(cursor));
            cursor.close();
            if (file.exists()) {
                return file;
            }
            return null;
        } else {
            cursor.close();
            return null;
        }
    }

    @Override
    public QiscusComment getComment(int id, String uniqueId) {
        String query;
        if (id == -1) {
            query = "SELECT * FROM "
                    + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                    + QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = '" + id + "'";
        } else {
            query = "SELECT * FROM "
                    + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                    + QiscusDb.CommentTable.COLUMN_ID + " = " + id + " OR "
                    + QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = '" + uniqueId + "'";
        }
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToNext()) {
            QiscusComment qiscusComment = QiscusDb.CommentTable.parseCursor(cursor);
            cursor.close();
            return qiscusComment;
        } else {
            cursor.close();
            return null;
        }
    }

    @Override
    public List<QiscusComment> getComments(int topicId, int count) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC "
                + "LIMIT " + count;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusComment> qiscusComments = new ArrayList<>();
        while (cursor.moveToNext()) {
            qiscusComments.add(QiscusDb.CommentTable.parseCursor(cursor));
        }
        cursor.close();
        return qiscusComments;
    }

    @Override
    public Observable<List<QiscusComment>> getObservableComments(final int topicId, final int count) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getComments(topicId, count));
            subscriber.onCompleted();
        });
    }

    @Override
    public List<QiscusComment> getOlderCommentsThan(QiscusComment qiscusComment, int topicId, int count) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " AND "
                + QiscusDb.CommentTable.COLUMN_TIME + " < " + qiscusComment.getTime().getTime() + " "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC "
                + "LIMIT " + count;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusComment> qiscusComments = new ArrayList<>();
        while (cursor.moveToNext()) {
            qiscusComments.add(QiscusDb.CommentTable.parseCursor(cursor));
        }
        cursor.close();
        return qiscusComments;
    }

    @Override
    public Observable<List<QiscusComment>> getObservableOlderCommentsThan(QiscusComment qiscusComment, int topicId, int count) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getOlderCommentsThan(qiscusComment, topicId, count));
            subscriber.onCompleted();
        });
    }

    @Override
    public QiscusComment getLatestComment() {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME
                + " WHERE " + QiscusDb.CommentTable.COLUMN_ID + " != -1 "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_ID + " DESC "
                + "LIMIT " + 1;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        QiscusComment qiscusComment = null;
        while (cursor.moveToNext()) {
            qiscusComment = QiscusDb.CommentTable.parseCursor(cursor);
        }
        cursor.close();
        return qiscusComment;
    }

    @Override
    public QiscusComment getLatestComment(int roomId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME
                + " WHERE " + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId + " "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC "
                + "LIMIT " + 1;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        QiscusComment qiscusComment = null;
        while (cursor.moveToNext()) {
            qiscusComment = QiscusDb.CommentTable.parseCursor(cursor);
        }
        cursor.close();
        return qiscusComment;
    }

    @Override
    public void clear() {
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(QiscusDb.RoomTable.TABLE_NAME, null, null);
            sqLiteDatabase.delete(QiscusDb.RoomMemberTable.TABLE_NAME, null, null);
            sqLiteDatabase.delete(QiscusDb.FilesTable.TABLE_NAME, null, null);
            sqLiteDatabase.delete(QiscusDb.CommentTable.TABLE_NAME, null, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }
}

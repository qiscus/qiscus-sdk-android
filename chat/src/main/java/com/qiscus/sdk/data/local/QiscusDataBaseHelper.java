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
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusRoomMember;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Emitter;
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
            if (qiscusChatRoom.getMember() != null) {
                for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
                    addRoomMember(qiscusChatRoom.getId(), member, qiscusChatRoom.getDistinctId());
                }
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

        if (qiscusChatRoom.getMember() != null && !qiscusChatRoom.getMember().isEmpty()) {
            deleteRoomMembers(qiscusChatRoom.getId());
            for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
                addRoomMember(qiscusChatRoom.getId(), member, qiscusChatRoom.getDistinctId());
            }
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
            if (latestComment != null) {
                qiscusChatRoom.setLastComment(latestComment);
            }
            cursor.close();
            return qiscusChatRoom;
        } else {
            cursor.close();
            return null;
        }
    }

    @Override
    public QiscusChatRoom getChatRoom(String email) {
        QiscusAccount account = Qiscus.getQiscusAccount();
        QiscusChatRoom room = getChatRoom(email, account.getEmail() + " " + email);
        if (room == null) {
            room = getChatRoom(email, email + " " + account.getEmail());
        }
        return room;
    }

    @Override
    public QiscusChatRoom getChatRoom(String email, String distinctId) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomMemberTable.TABLE_NAME + " WHERE "
                + QiscusDb.RoomMemberTable.COLUMN_DISTINCT_ID + " = " + DatabaseUtils.sqlEscapeString(distinctId) + " "
                + "AND " + QiscusDb.RoomMemberTable.COLUMN_USER_EMAIL
                + " = " + DatabaseUtils.sqlEscapeString(email);

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        while (cursor.moveToNext()) {
            QiscusChatRoom qiscusChatRoom = getChatRoom(QiscusDb.RoomMemberTable.getRoomId(cursor));
            if (qiscusChatRoom == null) {
                cursor.close();
                return null;
            }
            if (!qiscusChatRoom.isGroup()) {
                cursor.close();
                return qiscusChatRoom;
            }
        }

        cursor.close();
        return null;
    }

    @Override
    public QiscusChatRoom getChatRoomWithUniqueId(String uniqueId) {
        String query = String.format(
                "SELECT * FROM %s WHERE %s = \"%s\"",
                QiscusDb.RoomTable.TABLE_NAME,
                QiscusDb.RoomTable.COLUMN_DISTINCT_ID,
                uniqueId
        );

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToNext()) {
            QiscusChatRoom qiscusChatRoom = QiscusDb.RoomTable.parseCursor(cursor);
            qiscusChatRoom.setMember(getRoomMembers(qiscusChatRoom.getId()));
            QiscusComment latestComment = getLatestComment(qiscusChatRoom.getId());
            if (latestComment != null) {
                qiscusChatRoom.setLastComment(latestComment);
            }
            cursor.close();
            return qiscusChatRoom;
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
            QiscusComment latestComment = getLatestComment(qiscusChatRoom.getId());
            if (latestComment != null) {
                qiscusChatRoom.setLastComment(latestComment);
            }
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
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public List<QiscusChatRoom> getChatRooms(List<Integer> roomIds, List<String> uniqueIds) {
        List<QiscusChatRoom> qiscusChatRooms = new ArrayList<>();
        if (roomIds.isEmpty() && uniqueIds.isEmpty()) {
            return qiscusChatRooms;
        }

        String query = "SELECT * FROM " + QiscusDb.RoomTable.TABLE_NAME + " WHERE ";
        for (int i = 0; i < roomIds.size(); i++) {
            query += QiscusDb.RoomTable.COLUMN_ID + " = " + roomIds.get(i);
            if (i < roomIds.size() - 1) {
                query += " OR ";
            }
        }

        if (!roomIds.isEmpty() && !uniqueIds.isEmpty()) {
            query += " OR ";
        }

        for (int i = 0; i < uniqueIds.size(); i++) {
            query += QiscusDb.RoomTable.COLUMN_DISTINCT_ID + " = " + DatabaseUtils.sqlEscapeString(uniqueIds.get(i));
            if (i < uniqueIds.size() - 1) {
                query += " OR ";
            }
        }

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        while (cursor.moveToNext()) {
            QiscusChatRoom qiscusChatRoom = QiscusDb.RoomTable.parseCursor(cursor);
            qiscusChatRoom.setMember(getRoomMembers(qiscusChatRoom.getId()));
            QiscusComment latestComment = getLatestComment(qiscusChatRoom.getId());
            if (latestComment != null) {
                qiscusChatRoom.setLastComment(latestComment);
            }
            qiscusChatRooms.add(qiscusChatRoom);
        }
        cursor.close();
        return qiscusChatRooms;
    }

    @Override
    public void deleteChatRoom(int roomId) {
        String where = QiscusDb.RoomTable.COLUMN_ID + " = " + roomId;
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(QiscusDb.RoomTable.TABLE_NAME, where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void addRoomMember(int roomId, QiscusRoomMember qiscusRoomMember, String distinctId) {
        distinctId = distinctId == null ? "default" : distinctId;
        if (!isContainsRoomMember(roomId, qiscusRoomMember.getEmail())) {
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.insert(QiscusDb.RoomMemberTable.TABLE_NAME, null,
                        QiscusDb.RoomMemberTable.toContentValues(roomId, qiscusRoomMember.getEmail(), distinctId));
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
        addOrUpdate(qiscusRoomMember);
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
    public List<QiscusRoomMember> getRoomMembers(int roomId) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomMemberTable.TABLE_NAME + " "
                + "WHERE " + QiscusDb.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomId;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusRoomMember> members = new ArrayList<>();
        while (cursor.moveToNext()) {
            members.add(getMember(QiscusDb.RoomMemberTable.getMember(cursor)));
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
    public void deleteRoomMembers(int roomId) {
        String where = QiscusDb.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomId;

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
    public void add(QiscusRoomMember qiscusRoomMember) {
        if (!isContains(qiscusRoomMember)) {
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.insert(QiscusDb.MemberTable.TABLE_NAME, null, QiscusDb.MemberTable.toContentValues(qiscusRoomMember));
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }

    @Override
    public boolean isContains(QiscusRoomMember qiscusRoomMember) {
        String query = "SELECT * FROM "
                + QiscusDb.MemberTable.TABLE_NAME + " WHERE "
                + QiscusDb.MemberTable.COLUMN_USER_EMAIL + " = " + DatabaseUtils.sqlEscapeString(qiscusRoomMember.getEmail());
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    @Override
    public void update(QiscusRoomMember qiscusRoomMember) {
        String where = QiscusDb.MemberTable.COLUMN_USER_EMAIL + " = " + DatabaseUtils.sqlEscapeString(qiscusRoomMember.getEmail());
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.update(QiscusDb.MemberTable.TABLE_NAME, QiscusDb.MemberTable.toContentValues(qiscusRoomMember), where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void addOrUpdate(QiscusRoomMember qiscusRoomMember) {
        if (!isContains(qiscusRoomMember)) {
            add(qiscusRoomMember);
        } else {
            update(qiscusRoomMember);
        }
    }

    @Override
    public QiscusRoomMember getMember(String email) {
        String query = "SELECT * FROM "
                + QiscusDb.MemberTable.TABLE_NAME + " WHERE "
                + QiscusDb.MemberTable.COLUMN_USER_EMAIL + " = " + DatabaseUtils.sqlEscapeString(email);

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToNext()) {
            QiscusRoomMember qiscusRoomMember = QiscusDb.MemberTable.getMember(cursor);
            cursor.close();
            return qiscusRoomMember;
        } else {
            cursor.close();
            return null;
        }
    }

    @Override
    public void add(QiscusComment qiscusComment) {
        if (!isContains(qiscusComment)) {
            sqLiteDatabase.beginTransaction();
            try {
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
            sqLiteDatabase.update(QiscusDb.FilesTable.TABLE_NAME,
                    QiscusDb.FilesTable.toContentValues(topicId, commentId, localPath), where, null);
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
            QiscusRoomMember qiscusRoomMember = getMember(qiscusComment.getSenderEmail());
            if (qiscusRoomMember != null) {
                qiscusComment.setSender(qiscusRoomMember.getUsername());
                qiscusComment.setSenderAvatar(qiscusRoomMember.getAvatar());
            }
            cursor.close();
            return qiscusComment;
        } else {
            cursor.close();
            return null;
        }
    }

    @Override
    public List<QiscusComment> getComments(int topicId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusComment> qiscusComments = new ArrayList<>();
        while (cursor.moveToNext()) {
            QiscusComment qiscusComment = QiscusDb.CommentTable.parseCursor(cursor);
            QiscusRoomMember qiscusRoomMember = getMember(qiscusComment.getSenderEmail());
            if (qiscusRoomMember != null) {
                qiscusComment.setSender(qiscusRoomMember.getUsername());
                qiscusComment.setSenderAvatar(qiscusRoomMember.getAvatar());
            }
            qiscusComments.add(qiscusComment);
        }
        cursor.close();
        return qiscusComments;
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
            QiscusComment qiscusComment = QiscusDb.CommentTable.parseCursor(cursor);
            QiscusRoomMember qiscusRoomMember = getMember(qiscusComment.getSenderEmail());
            if (qiscusRoomMember != null) {
                qiscusComment.setSender(qiscusRoomMember.getUsername());
                qiscusComment.setSenderAvatar(qiscusRoomMember.getAvatar());
            }
            qiscusComments.add(qiscusComment);
        }
        cursor.close();
        return qiscusComments;
    }

    @Override
    public Observable<List<QiscusComment>> getObservableComments(final int topicId) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getComments(topicId));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public Observable<List<QiscusComment>> getObservableComments(final int topicId, final int count) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getComments(topicId, count));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public List<QiscusComment> getOlderCommentsThan(QiscusComment qiscusComment, int topicId, int count) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " AND "
                + QiscusDb.CommentTable.COLUMN_TIME + " <= " + qiscusComment.getTime().getTime() + " "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC "
                + "LIMIT " + count;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusComment> qiscusComments = new ArrayList<>();
        while (cursor.moveToNext()) {
            QiscusComment comment = QiscusDb.CommentTable.parseCursor(cursor);
            QiscusRoomMember qiscusRoomMember = getMember(comment.getSenderEmail());
            if (qiscusRoomMember != null) {
                comment.setSender(qiscusRoomMember.getUsername());
                comment.setSenderAvatar(qiscusRoomMember.getAvatar());
            }
            qiscusComments.add(comment);
        }
        cursor.close();
        return qiscusComments;
    }

    @Override
    public Observable<List<QiscusComment>> getObservableOlderCommentsThan(QiscusComment qiscusComment, int topicId, int count) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getOlderCommentsThan(qiscusComment, topicId, count));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public List<QiscusComment> getCommentsAfter(QiscusComment qiscusComment, int topicId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " AND ("
                + QiscusDb.CommentTable.COLUMN_ID + " >= " + qiscusComment.getId() + " OR "
                + QiscusDb.CommentTable.COLUMN_ID + " = -1) "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC ";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusComment> qiscusComments = new ArrayList<>();
        while (cursor.moveToNext()) {
            QiscusComment comment = QiscusDb.CommentTable.parseCursor(cursor);
            QiscusRoomMember qiscusRoomMember = getMember(comment.getSenderEmail());
            if (qiscusRoomMember != null) {
                comment.setSender(qiscusRoomMember.getUsername());
                comment.setSenderAvatar(qiscusRoomMember.getAvatar());
            }
            qiscusComments.add(comment);
        }
        cursor.close();
        return qiscusComments;
    }

    @Override
    public Observable<List<QiscusComment>> getObservableCommentsAfter(QiscusComment qiscusComment, int topicId) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getCommentsAfter(qiscusComment, topicId));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public QiscusComment getLatestComment() {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ID + " != -1 "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_ID + " DESC "
                + "LIMIT " + 1;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        QiscusComment qiscusComment = null;
        while (cursor.moveToNext()) {
            qiscusComment = QiscusDb.CommentTable.parseCursor(cursor);
            QiscusRoomMember qiscusRoomMember = getMember(qiscusComment.getSenderEmail());
            if (qiscusRoomMember != null) {
                qiscusComment.setSender(qiscusRoomMember.getUsername());
                qiscusComment.setSenderAvatar(qiscusRoomMember.getAvatar());
            }
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
            QiscusRoomMember qiscusRoomMember = getMember(qiscusComment.getSenderEmail());
            if (qiscusRoomMember != null) {
                qiscusComment.setSender(qiscusRoomMember.getUsername());
                qiscusComment.setSenderAvatar(qiscusRoomMember.getAvatar());
            }
        }
        cursor.close();
        return qiscusComment;
    }

    @Override
    public QiscusComment getLatestDeliveredComment(int topicId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ID + " != -1 "
                + " AND " + QiscusDb.CommentTable.COLUMN_TOPIC_ID + " = " + topicId
                + " AND " + QiscusDb.CommentTable.COLUMN_STATE + " = " + QiscusComment.STATE_DELIVERED
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_ID + " DESC "
                + "LIMIT " + 1;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        QiscusComment qiscusComment = null;
        while (cursor.moveToNext()) {
            qiscusComment = QiscusDb.CommentTable.parseCursor(cursor);
            QiscusRoomMember qiscusRoomMember = getMember(qiscusComment.getSenderEmail());
            if (qiscusRoomMember != null) {
                qiscusComment.setSender(qiscusRoomMember.getUsername());
                qiscusComment.setSenderAvatar(qiscusRoomMember.getAvatar());
            }
        }
        cursor.close();
        return qiscusComment;
    }

    @Override
    public QiscusComment getLatestReadComment(int topicId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ID + " != -1 "
                + " AND " + QiscusDb.CommentTable.COLUMN_TOPIC_ID + " = " + topicId
                + " AND " + QiscusDb.CommentTable.COLUMN_STATE + " = " + QiscusComment.STATE_READ
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_ID + " DESC "
                + "LIMIT " + 1;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        QiscusComment qiscusComment = null;
        while (cursor.moveToNext()) {
            qiscusComment = QiscusDb.CommentTable.parseCursor(cursor);
            QiscusRoomMember qiscusRoomMember = getMember(qiscusComment.getSenderEmail());
            if (qiscusRoomMember != null) {
                qiscusComment.setSender(qiscusRoomMember.getUsername());
                qiscusComment.setSenderAvatar(qiscusRoomMember.getAvatar());
            }
        }
        cursor.close();
        return qiscusComment;
    }

    @Override
    public List<QiscusComment> getPendingComments() {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_STATE + " = " + QiscusComment.STATE_PENDING + " "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " ASC";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusComment> qiscusComments = new ArrayList<>();
        while (cursor.moveToNext()) {
            QiscusComment qiscusComment = QiscusDb.CommentTable.parseCursor(cursor);
            QiscusRoomMember qiscusRoomMember = getMember(qiscusComment.getSenderEmail());
            if (qiscusRoomMember != null) {
                qiscusComment.setSender(qiscusRoomMember.getUsername());
                qiscusComment.setSenderAvatar(qiscusRoomMember.getAvatar());
            }
            qiscusComments.add(qiscusComment);
        }
        cursor.close();
        return qiscusComments;
    }

    @Override
    public Observable<List<QiscusComment>> getObservablePendingComments() {
        return Observable.create(subscriber -> {
            subscriber.onNext(getPendingComments());
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public void clear() {
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(QiscusDb.RoomTable.TABLE_NAME, null, null);
            sqLiteDatabase.delete(QiscusDb.MemberTable.TABLE_NAME, null, null);
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

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

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Emitter;
import rx.Observable;

public class QiscusDataBaseHelper implements QiscusDataStore {

    protected final SQLiteDatabase sqLiteReadDatabase;
    protected final SQLiteDatabase sqLiteWriteDatabase;

    public QiscusDataBaseHelper() {
        QiscusDbOpenHelper qiscusDbOpenHelper = new QiscusDbOpenHelper(QiscusCore.getApps());
        sqLiteReadDatabase = qiscusDbOpenHelper.getReadableDatabase();
        sqLiteWriteDatabase = qiscusDbOpenHelper.getWritableDatabase();
    }

    @Override
    public void add(QiscusChatRoom qiscusChatRoom) {
        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.insertWithOnConflict(QiscusDb.RoomTable.TABLE_NAME, null,
                    QiscusDb.RoomTable.toContentValues(qiscusChatRoom), SQLiteDatabase.CONFLICT_ABORT);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }

        if (qiscusChatRoom.getMember() != null) {
            for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
                addRoomMember(qiscusChatRoom.getId(), member, qiscusChatRoom.getDistinctId());
            }
        }

        QiscusComment comment = qiscusChatRoom.getLastComment();
        if (comment != null && comment.getId() > 0) {
            addOrUpdate(comment);
        }
    }

    @Override
    public boolean isContains(QiscusChatRoom qiscusChatRoom) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomTable.TABLE_NAME + " WHERE "
                + QiscusDb.RoomTable.COLUMN_ID + " = " + qiscusChatRoom.getId();

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    @Override
    public void update(QiscusChatRoom qiscusChatRoom) {
        String where = QiscusDb.RoomTable.COLUMN_ID + " = " + qiscusChatRoom.getId();

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.update(QiscusDb.RoomTable.TABLE_NAME, QiscusDb.RoomTable.toContentValues(qiscusChatRoom), where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }

        if (qiscusChatRoom.getMember() != null && !qiscusChatRoom.getMember().isEmpty()) {
            deleteRoomMembers(qiscusChatRoom.getId());
            for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
                addRoomMember(qiscusChatRoom.getId(), member, qiscusChatRoom.getDistinctId());
            }
        }

        QiscusComment comment = qiscusChatRoom.getLastComment();
        if (comment != null && comment.getId() > 0) {
            addOrUpdate(comment);
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
    public QiscusChatRoom getChatRoom(long id) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomTable.TABLE_NAME + " WHERE "
                + QiscusDb.RoomTable.COLUMN_ID + " = " + id;


        Cursor cursor = null;
        try {
            cursor = sqLiteReadDatabase.rawQuery(query, null);
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

        } catch (Exception e) {
            QiscusErrorLogger.print(e);
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
    }

    @Override
    public QiscusChatRoom getChatRoom(String email) {
        QiscusAccount account = QiscusCore.getQiscusAccount();
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
                + QiscusDb.RoomMemberTable.COLUMN_DISTINCT_ID + " = " + DatabaseUtils.sqlEscapeString(distinctId)
                + " AND " + QiscusDb.RoomMemberTable.COLUMN_USER_EMAIL
                + " = " + DatabaseUtils.sqlEscapeString(email);

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);

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
        String query = "SELECT * FROM "
                + QiscusDb.RoomTable.TABLE_NAME + " WHERE "
                + QiscusDb.RoomTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(uniqueId);

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);

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
    public List<QiscusChatRoom> getChatRooms(int limit) {
        return getChatRooms(limit, -1);
    }

    @Override
    public List<QiscusChatRoom> getChatRooms(int limit, int offset) {
        String roomTableName = QiscusDb.RoomTable.TABLE_NAME;
        String commentTableName = QiscusDb.CommentTable.TABLE_NAME;
        String query = "SELECT " + roomTableName + ".*" + " FROM "
                + QiscusDb.RoomTable.TABLE_NAME
                + " LEFT JOIN " + commentTableName
                + " ON " + roomTableName + "." + QiscusDb.RoomTable.COLUMN_ID
                + " = " + commentTableName + "." + QiscusDb.CommentTable.COLUMN_ROOM_ID
                + " AND " + commentTableName + "." + QiscusDb.CommentTable.COLUMN_DELETED + " != 1"
                + " AND " + commentTableName + "." + QiscusDb.CommentTable.COLUMN_HARD_DELETED + " != 1"
                + " GROUP BY " + roomTableName + "." + QiscusDb.RoomTable.COLUMN_ID
                + " ORDER BY " + commentTableName + "." + QiscusDb.CommentTable.COLUMN_TIME
                + " DESC "
                + " LIMIT " + limit
                + " OFFSET " + offset;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public Observable<List<QiscusChatRoom>> getObservableChatRooms(int limit) {
        return getObservableChatRooms(limit, -1);
    }

    @Override
    public Observable<List<QiscusChatRoom>> getObservableChatRooms(int limit, int offset) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getChatRooms(limit, offset));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public List<QiscusChatRoom> getChatRooms(List<Long> roomIds, List<String> uniqueIds) {
        List<QiscusChatRoom> qiscusChatRooms = new ArrayList<>();
        if (roomIds.isEmpty() && uniqueIds.isEmpty()) {
            return qiscusChatRooms;
        }

        StringBuilder query = new StringBuilder("SELECT * FROM ").append(QiscusDb.RoomTable.TABLE_NAME).append(" WHERE ");
        for (int i = 0; i < roomIds.size(); i++) {
            query.append(QiscusDb.RoomTable.COLUMN_ID).append(" = ").append(roomIds.get(i));
            if (i < roomIds.size() - 1) {
                query.append(" OR ");
            }
        }

        if (!roomIds.isEmpty() && !uniqueIds.isEmpty()) {
            query.append(" OR ");
        }

        for (int i = 0; i < uniqueIds.size(); i++) {
            query.append(QiscusDb.RoomTable.COLUMN_UNIQUE_ID).append(" = ").append(DatabaseUtils.sqlEscapeString(uniqueIds.get(i)));
            if (i < uniqueIds.size() - 1) {
                query.append(" OR ");
            }
        }

        Cursor cursor = sqLiteReadDatabase.rawQuery(query.toString(), null);
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
        sortRooms(qiscusChatRooms);
        return qiscusChatRooms;
    }

    @Override
    public void deleteChatRoom(long roomId) {
        String where = QiscusDb.RoomTable.COLUMN_ID + " = " + roomId;

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.delete(QiscusDb.RoomTable.TABLE_NAME, where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public void addRoomMember(long roomId, QiscusRoomMember qiscusRoomMember, String distinctId) {
        distinctId = distinctId == null ? "default" : distinctId;
        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.insertWithOnConflict(QiscusDb.RoomMemberTable.TABLE_NAME, null,
                    QiscusDb.RoomMemberTable.toContentValues(roomId, distinctId, qiscusRoomMember), SQLiteDatabase.CONFLICT_ABORT);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
        addOrUpdate(qiscusRoomMember);
    }

    @Override
    public boolean isContainsRoomMember(long roomId, String email) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomMemberTable.TABLE_NAME + " WHERE "
                + QiscusDb.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomId
                + " AND " + QiscusDb.RoomMemberTable.COLUMN_USER_EMAIL
                + " = " + DatabaseUtils.sqlEscapeString(email);

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    @Override
    public void updateRoomMember(long roomId, QiscusRoomMember qiscusRoomMember, String distinctId) {
        distinctId = distinctId == null ? "default" : distinctId;
        String where = QiscusDb.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomId + " AND "
                + QiscusDb.RoomMemberTable.COLUMN_USER_EMAIL + " = " + DatabaseUtils.sqlEscapeString(qiscusRoomMember.getEmail());

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.update(QiscusDb.RoomMemberTable.TABLE_NAME,
                    QiscusDb.RoomMemberTable.toContentValues(roomId, distinctId, qiscusRoomMember), where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }

        addOrUpdate(qiscusRoomMember);
    }

    @Override
    public void addOrUpdateRoomMember(long roomId, QiscusRoomMember qiscusRoomMember, String distinctId) {
        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.insertWithOnConflict(QiscusDb.RoomMemberTable.TABLE_NAME, null,
                    QiscusDb.RoomMemberTable.toContentValues(roomId, distinctId, qiscusRoomMember), SQLiteDatabase.CONFLICT_REPLACE);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }

        addOrUpdate(qiscusRoomMember);
    }

    @Override
    public List<QiscusRoomMember> getRoomMembers(long roomId) {
        String query = "SELECT * FROM "
                + QiscusDb.RoomMemberTable.TABLE_NAME + " WHERE "
                + QiscusDb.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomId;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
        List<QiscusRoomMember> members = new ArrayList<>();
        while (cursor.moveToNext()) {
            QiscusRoomMember member = getMember(QiscusDb.RoomMemberTable.getUserEmail(cursor));
            if (member != null) {
                member.setLastDeliveredCommentId(QiscusDb.RoomMemberTable.getLastDeliveredCommentId(cursor));
                member.setLastReadCommentId(QiscusDb.RoomMemberTable.getLastReadCommentId(cursor));
                members.add(member);
            }
        }
        cursor.close();
        return members;
    }

    @Override
    public void deleteRoomMember(long roomId, String email) {
        String where = QiscusDb.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomId
                + " AND " + QiscusDb.RoomMemberTable.COLUMN_USER_EMAIL + " = " + DatabaseUtils.sqlEscapeString(email);

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.delete(QiscusDb.RoomMemberTable.TABLE_NAME, where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public void deleteRoomMembers(long roomId) {
        String where = QiscusDb.RoomMemberTable.COLUMN_ROOM_ID + " = " + roomId;

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.delete(QiscusDb.RoomMemberTable.TABLE_NAME, where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public void add(QiscusRoomMember qiscusRoomMember) {
        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.insertWithOnConflict(QiscusDb.MemberTable.TABLE_NAME, null,
                    QiscusDb.MemberTable.toContentValues(qiscusRoomMember), SQLiteDatabase.CONFLICT_ABORT);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public boolean isContains(QiscusRoomMember qiscusRoomMember) {
        String query = "SELECT * FROM "
                + QiscusDb.MemberTable.TABLE_NAME + " WHERE "
                + QiscusDb.MemberTable.COLUMN_USER_EMAIL + " = " + DatabaseUtils.sqlEscapeString(qiscusRoomMember.getEmail());

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    @Override
    public void update(QiscusRoomMember qiscusRoomMember) {
        String where = QiscusDb.MemberTable.COLUMN_USER_EMAIL + " = " + DatabaseUtils.sqlEscapeString(qiscusRoomMember.getEmail());

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.update(QiscusDb.MemberTable.TABLE_NAME,
                    QiscusDb.MemberTable.toContentValues(qiscusRoomMember), where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public void addOrUpdate(QiscusRoomMember qiscusRoomMember) {
        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.insertWithOnConflict(QiscusDb.MemberTable.TABLE_NAME, null,
                    QiscusDb.MemberTable.toContentValues(qiscusRoomMember), SQLiteDatabase.CONFLICT_REPLACE);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public QiscusRoomMember getMember(String email) {
        String query = "SELECT * FROM "
                + QiscusDb.MemberTable.TABLE_NAME + " WHERE "
                + QiscusDb.MemberTable.COLUMN_USER_EMAIL + " = " + DatabaseUtils.sqlEscapeString(email);

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);

        try {
            if (cursor != null && cursor.moveToNext()) {
                QiscusRoomMember qiscusRoomMember = QiscusDb.MemberTable.getMember(cursor);
                cursor.close();
                return qiscusRoomMember;
            } else {
                cursor.close();
                return null;
            }
        } catch (Exception e) {
            cursor.close();
            QiscusErrorLogger.print(e);
            return null;
        }
    }

    @Override
    public void add(QiscusComment qiscusComment) {
        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.insertWithOnConflict(QiscusDb.CommentTable.TABLE_NAME, null,
                    QiscusDb.CommentTable.toContentValues(qiscusComment), SQLiteDatabase.CONFLICT_ABORT);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public void saveLocalPath(long roomId, long commentId, String localPath) {
        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.insertWithOnConflict(QiscusDb.FilesTable.TABLE_NAME, null,
                    QiscusDb.FilesTable.toContentValues(roomId, commentId, localPath), SQLiteDatabase.CONFLICT_ABORT);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public boolean isContains(QiscusComment qiscusComment) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(qiscusComment.getUniqueId());

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    @Override
    public boolean isContainsFileOfComment(long commentId) {
        String query = "SELECT * FROM "
                + QiscusDb.FilesTable.TABLE_NAME + " WHERE "
                + QiscusDb.FilesTable.COLUMN_COMMENT_ID + " = " + commentId;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    @Override
    public void update(QiscusComment qiscusComment) {
        String where = QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(qiscusComment.getUniqueId());

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.update(QiscusDb.CommentTable.TABLE_NAME, QiscusDb.CommentTable.toContentValues(qiscusComment), where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public void updateLocalPath(long roomId, long commentId, String localPath) {
        String where = QiscusDb.FilesTable.COLUMN_COMMENT_ID + " = " + commentId;

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.update(QiscusDb.FilesTable.TABLE_NAME,
                    QiscusDb.FilesTable.toContentValues(roomId, commentId, localPath), where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public void addOrUpdate(QiscusComment qiscusComment) {
        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.insertWithOnConflict(QiscusDb.CommentTable.TABLE_NAME, null,
                    QiscusDb.CommentTable.toContentValues(qiscusComment), SQLiteDatabase.CONFLICT_REPLACE);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public void addOrUpdateLocalPath(long roomId, long commentId, String localPath) {
        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.insertWithOnConflict(QiscusDb.FilesTable.TABLE_NAME, null,
                    QiscusDb.FilesTable.toContentValues(roomId, commentId, localPath), SQLiteDatabase.CONFLICT_REPLACE);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public void delete(QiscusComment qiscusComment) {
        String where = QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(qiscusComment.getUniqueId());

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.delete(QiscusDb.CommentTable.TABLE_NAME, where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
        deleteLocalPath(qiscusComment.getId());
    }

    @Override
    public boolean deleteCommentsByRoomId(long roomId) {
        List<QiscusComment> comments = getComments(roomId);

        if (comments.isEmpty()) {
            return false;
        }

        for (QiscusComment comment : comments) {
            deleteLocalPath(comment.getId());
        }

        String where = QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId;
        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.delete(QiscusDb.CommentTable.TABLE_NAME, where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }

        return true;
    }

    @Override
    public boolean deleteCommentsByRoomId(long roomId, long timestampOffset) {
        List<QiscusComment> comments = getComments(roomId, timestampOffset);

        if (comments.isEmpty()) {
            return false;
        }

        for (QiscusComment comment : comments) {
            deleteLocalPath(comment.getId());
        }

        String where = QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId + " AND "
                + QiscusDb.CommentTable.COLUMN_TIME + " <= " + timestampOffset;

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.delete(QiscusDb.CommentTable.TABLE_NAME, where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }

        return true;
    }

    @Override
    public void updateLastDeliveredComment(long roomId, long commentId) {
        String sql = "UPDATE " + QiscusDb.CommentTable.TABLE_NAME
                + " SET " + QiscusDb.CommentTable.COLUMN_STATE + " = " + QiscusComment.STATE_DELIVERED
                + " WHERE " + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId
                + " AND " + QiscusDb.CommentTable.COLUMN_ID + " <= " + commentId
                + " AND " + QiscusDb.CommentTable.COLUMN_ID + " != -1"
                + " AND " + QiscusDb.CommentTable.COLUMN_STATE + " < " + QiscusComment.STATE_DELIVERED;

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.execSQL(sql);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public void updateLastReadComment(long roomId, long commentId) {
        String sql = "UPDATE " + QiscusDb.CommentTable.TABLE_NAME
                + " SET " + QiscusDb.CommentTable.COLUMN_STATE + " = " + QiscusComment.STATE_READ
                + " WHERE " + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId
                + " AND " + QiscusDb.CommentTable.COLUMN_ID + " <= " + commentId
                + " AND " + QiscusDb.CommentTable.COLUMN_ID + " != -1"
                + " AND " + QiscusDb.CommentTable.COLUMN_STATE + " < " + QiscusComment.STATE_READ;

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            sqLiteWriteDatabase.execSQL(sql);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public File getLocalPath(long commentId) {
        String query = "SELECT * FROM "
                + QiscusDb.FilesTable.TABLE_NAME + " WHERE "
                + QiscusDb.FilesTable.COLUMN_COMMENT_ID + " = " + commentId;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public void deleteLocalPath(long commentId) {
        File file = getLocalPath(commentId);
        if (file != null) {
            file.delete();
        }

        sqLiteWriteDatabase.beginTransactionNonExclusive();
        try {
            String where = QiscusDb.FilesTable.COLUMN_COMMENT_ID + " = " + commentId;
            sqLiteWriteDatabase.delete(QiscusDb.FilesTable.TABLE_NAME, where, null);
            sqLiteWriteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteWriteDatabase.endTransaction();
        }
    }

    @Override
    public QiscusComment getComment(String uniqueId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(uniqueId);

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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

    private QiscusComment getComment(long id) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ID + " = " + id;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public QiscusComment getCommentByBeforeId(long beforeId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_COMMENT_BEFORE_ID + " = " + beforeId;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public List<QiscusComment> getComments(long roomId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId + " AND "
                + QiscusDb.CommentTable.COLUMN_HARD_DELETED + " = " + 0
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC";

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public List<QiscusComment> getComments(long roomId, int limit) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId + " AND "
                + QiscusDb.CommentTable.COLUMN_HARD_DELETED + " = " + 0
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC"
                + " LIMIT " + limit;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public List<QiscusComment> getComments(long roomId, long timestampOffset) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId + " AND "
                + QiscusDb.CommentTable.COLUMN_TIME + " <= " + timestampOffset + " AND "
                + QiscusDb.CommentTable.COLUMN_HARD_DELETED + " = " + 0
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC";

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public Observable<List<QiscusComment>> getObservableComments(final long roomId) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getComments(roomId));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public Observable<List<QiscusComment>> getObservableComments(final long roomId, final int limit) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getComments(roomId, limit));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public List<QiscusComment> getOlderCommentsThan(QiscusComment qiscusComment, long roomId, int limit) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId + " AND "
                + QiscusDb.CommentTable.COLUMN_TIME + " <= " + qiscusComment.getTime().getTime() + " AND "
                + QiscusDb.CommentTable.COLUMN_HARD_DELETED + " = " + 0
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC"
                + " LIMIT " + limit;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public Observable<List<QiscusComment>> getObservableOlderCommentsThan(QiscusComment qiscusComment, long roomId, int limit) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getOlderCommentsThan(qiscusComment, roomId, limit));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public List<QiscusComment> getCommentsAfter(QiscusComment qiscusComment, long roomId) {
        QiscusComment savedComment = getComment(qiscusComment.getId());
        if (savedComment == null) {
            return new ArrayList<>();
        }

        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId + " AND ("
                + QiscusDb.CommentTable.COLUMN_TIME + " >= " + savedComment.getTime().getTime() + " OR "
                + QiscusDb.CommentTable.COLUMN_ID + " = -1) " + " AND "
                + QiscusDb.CommentTable.COLUMN_HARD_DELETED + " = " + 0
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC";

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public Observable<List<QiscusComment>> getObservableCommentsAfter(QiscusComment qiscusComment, long roomId) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getCommentsAfter(qiscusComment, roomId));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public QiscusComment getLatestComment() {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ID + " != -1 " + " AND "
                + QiscusDb.CommentTable.COLUMN_HARD_DELETED + " = " + 0
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC "
                + " LIMIT " + 1;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public QiscusComment getLatestComment(long roomId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME
                + " WHERE " + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId + " AND "
                + QiscusDb.CommentTable.COLUMN_HARD_DELETED + " = " + 0
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC"
                + " LIMIT " + 1;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public QiscusComment getLatestDeliveredComment(long roomId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ID + " != -1 "
                + " AND " + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId
                + " AND " + QiscusDb.CommentTable.COLUMN_STATE + " = " + QiscusComment.STATE_DELIVERED
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC"
                + " LIMIT " + 1;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public QiscusComment getLatestReadComment(long roomId) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ID + " != -1 "
                + " AND " + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId
                + " AND " + QiscusDb.CommentTable.COLUMN_STATE + " = " + QiscusComment.STATE_READ
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC"
                + " LIMIT " + 1;

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
                + QiscusDb.CommentTable.COLUMN_STATE + " = " + QiscusComment.STATE_PENDING
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " ASC";

        Cursor cursor = sqLiteReadDatabase.rawQuery(query, null);
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
    public List<QiscusComment> searchComments(String query, long roomId, int limit, int offset) {
        String sql = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_ROOM_ID + " = " + roomId + " AND "
                + QiscusDb.CommentTable.COLUMN_MESSAGE + " LIKE '%" + query + "%' " + " AND "
                + QiscusDb.CommentTable.COLUMN_HARD_DELETED + " = " + 0
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC "
                + " LIMIT " + limit + " OFFSET " + offset;

        Cursor cursor = sqLiteReadDatabase.rawQuery(sql, null);
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
    public List<QiscusComment> searchComments(String query, int limit, int offset) {
        String sql = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_MESSAGE + " LIKE '%" + query + "%' " + " AND "
                + QiscusDb.CommentTable.COLUMN_HARD_DELETED + " = " + 0
                + " ORDER BY " + QiscusDb.CommentTable.COLUMN_TIME + " DESC "
                + " LIMIT " + limit + " OFFSET " + offset;

        Cursor cursor = sqLiteReadDatabase.rawQuery(sql, null);
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
    public void clear() {
        sqLiteReadDatabase.beginTransaction();
        try {
            sqLiteReadDatabase.delete(QiscusDb.RoomTable.TABLE_NAME, null, null);
            sqLiteReadDatabase.delete(QiscusDb.MemberTable.TABLE_NAME, null, null);
            sqLiteReadDatabase.delete(QiscusDb.RoomMemberTable.TABLE_NAME, null, null);
            sqLiteReadDatabase.delete(QiscusDb.FilesTable.TABLE_NAME, null, null);
            sqLiteReadDatabase.delete(QiscusDb.CommentTable.TABLE_NAME, null, null);
            sqLiteReadDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            QiscusErrorLogger.print(e);
        } finally {
            sqLiteReadDatabase.endTransaction();
        }
    }

    private void sortRooms(List<QiscusChatRoom> qiscusChatRooms) {
        Collections.sort(qiscusChatRooms, (room1, room2) -> {
            if (room1.getLastComment() != null && room2.getLastComment() != null) {
                return room2.getLastComment().getTime().compareTo(room1.getLastComment().getTime());
            } else if (room1.getLastComment() == null && room2.getLastComment() != null) {
                return 1;
            } else if (room1.getLastComment() != null && room2.getLastComment() == null) {
                return -1;
            }
            return 0;
        });
    }
}

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
import android.database.sqlite.SQLiteDatabase;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusComment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public enum QiscusDataBaseHelper {
    INSTANCE;

    private final SQLiteDatabase sqLiteDatabase;

    QiscusDataBaseHelper() {
        QiscusDbOpenHelper qiscusDbOpenHelper = new QiscusDbOpenHelper(Qiscus.getApps());
        sqLiteDatabase = qiscusDbOpenHelper.getReadableDatabase();
    }

    public static QiscusDataBaseHelper getInstance() {
        return INSTANCE;
    }

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

    public void saveLocalPath(int topicId, int commentId, String localPath) {
        if (!isContainFileOfComment(commentId)) {
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

    public boolean isContainFileOfComment(int commentId) {
        String query = "SELECT * FROM "
                + QiscusDb.FilesTable.TABLE_NAME + " WHERE "
                + QiscusDb.FilesTable.COLUMN_COMMENT_ID + " = " + commentId + "";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

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

    public void addOrUpdate(QiscusComment qiscusComment) {
        if (!isContains(qiscusComment)) {
            add(qiscusComment);
        } else {
            update(qiscusComment);
        }
    }

    public void addOrUpdateLocalPath(int topicId, int commentId, String localPath) {
        if (!isContainFileOfComment(commentId)) {
            saveLocalPath(topicId, commentId, localPath);
        } else {
            updateLocalPath(topicId, commentId, localPath);
        }
    }

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

    public List<QiscusComment> getComments(int topicId, int count) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_ID + " DESC "
                + "LIMIT " + count;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusComment> qiscusComments = new ArrayList<>();
        while (cursor.moveToNext()) {
            qiscusComments.add(QiscusDb.CommentTable.parseCursor(cursor));
        }
        cursor.close();
        return qiscusComments;
    }

    public Observable<List<QiscusComment>> getObservableComments(final int topicId, final int count) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getComments(topicId, count));
            subscriber.onCompleted();
        });
    }

    public List<QiscusComment> getOlderCommentsThan(QiscusComment qiscusComment, int topicId, int count) {
        String query = "SELECT * FROM "
                + QiscusDb.CommentTable.TABLE_NAME + " WHERE "
                + QiscusDb.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " AND "
                + QiscusDb.CommentTable.COLUMN_ID + " < " + qiscusComment.getId() + " "
                + "ORDER BY " + QiscusDb.CommentTable.COLUMN_ID + " DESC "
                + "LIMIT " + count;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusComment> qiscusComments = new ArrayList<>();
        while (cursor.moveToNext()) {
            qiscusComments.add(QiscusDb.CommentTable.parseCursor(cursor));
        }
        cursor.close();
        return qiscusComments;
    }

    public Observable<List<QiscusComment>> getObservableOlderCommentsThan(final QiscusComment qiscusComment, final int topicId, final int count) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getOlderCommentsThan(qiscusComment, topicId, count));
            subscriber.onCompleted();
        });
    }

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
        }
        cursor.close();
        return qiscusComment;
    }

    public void clear() {
        sqLiteDatabase.beginTransaction();
        try {
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

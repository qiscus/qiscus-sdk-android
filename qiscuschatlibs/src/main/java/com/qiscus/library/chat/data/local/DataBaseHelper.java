package com.qiscus.library.chat.data.local;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.qiscus.library.chat.Qiscus;
import com.qiscus.library.chat.data.model.Comment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public enum DataBaseHelper {
    INSTANCE;

    private final SQLiteDatabase sqLiteDatabase;

    DataBaseHelper() {
        DbOpenHelper dbOpenHelper = new DbOpenHelper(Qiscus.getApps());
        sqLiteDatabase = dbOpenHelper.getReadableDatabase();
    }

    public static DataBaseHelper getInstance() {
        return INSTANCE;
    }

    public void add(Comment comment) {
        if (!isContains(comment)) {
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.insert(Db.CommentTable.TABLE_NAME, null, Db.CommentTable.toContentValues(comment));
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
                sqLiteDatabase.insert(Db.FilesTable.TABLE_NAME, null,
                                      Db.FilesTable.toContentValues(topicId, commentId, localPath));
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }

    public boolean isContains(Comment comment) {
        String query;
        if (comment.getId() == -1) {
            query = "SELECT * FROM "
                    + Db.CommentTable.TABLE_NAME + " WHERE "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + comment.getUniqueId() + "'";
        } else {
            query = "SELECT * FROM "
                    + Db.CommentTable.TABLE_NAME + " WHERE "
                    + Db.CommentTable.COLUMN_ID + " = " + comment.getId() + " OR "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + comment.getUniqueId() + "'";
        }
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    public boolean isContainFileOfComment(int commentId) {
        String query = "SELECT * FROM "
                + Db.FilesTable.TABLE_NAME + " WHERE "
                + Db.FilesTable.COLUMN_COMMENT_ID + " = " + commentId + "";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    public void update(Comment comment) {
        String where;
        if (comment.getId() == -1) {
            where = Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + comment.getUniqueId() + "'";
        } else {
            where = Db.CommentTable.COLUMN_ID + " = " + comment.getId() + " OR "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + comment.getUniqueId() + "'";
        }

        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.update(Db.CommentTable.TABLE_NAME, Db.CommentTable.toContentValues(comment), where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public void updateLocalPath(int topicId, int commentId, String localPath) {
        String where = Db.FilesTable.COLUMN_COMMENT_ID + " = " + commentId + "";
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.update(Db.FilesTable.TABLE_NAME, Db.FilesTable.toContentValues(topicId, commentId, localPath), where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public void addOrUpdate(Comment comment) {
        if (!isContains(comment)) {
            add(comment);
        } else {
            update(comment);
        }
    }

    public void addOrUpdateLocalPath(int topicId, int commentId, String localPath) {
        if (!isContainFileOfComment(commentId)) {
            saveLocalPath(topicId, commentId, localPath);
        } else {
            updateLocalPath(topicId, commentId, localPath);
        }
    }

    public void delete(Comment comment) {
        String where;
        if (comment.getId() == -1) {
            where = Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + comment.getUniqueId() + "'";
        } else {
            where = Db.CommentTable.COLUMN_ID + " = " + comment.getId() + " OR "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + comment.getUniqueId() + "'";
        }

        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(Db.CommentTable.TABLE_NAME, where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public File getLocalPath(int commentId) {
        String query = "SELECT * FROM "
                + Db.FilesTable.TABLE_NAME + " WHERE "
                + Db.FilesTable.COLUMN_COMMENT_ID + " = " + commentId + "";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToNext()) {
            File file = new File(Db.FilesTable.parseCursor(cursor));
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

    public Comment getComment(int id, String uniqueId) {
        String query;
        if (id == -1) {
            query = "SELECT * FROM "
                    + Db.CommentTable.TABLE_NAME + " WHERE "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + id + "'";
        } else {
            query = "SELECT * FROM "
                    + Db.CommentTable.TABLE_NAME + " WHERE "
                    + Db.CommentTable.COLUMN_ID + " = " + id + " OR "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + uniqueId + "'";
        }
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToNext()) {
            Comment comment = Db.CommentTable.parseCursor(cursor);
            cursor.close();
            return comment;
        } else {
            cursor.close();
            return null;
        }
    }

    public List<Comment> getComments(int topicId, int count) {
        String query = "SELECT * FROM "
                + Db.CommentTable.TABLE_NAME + " WHERE "
                + Db.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " "
                + "ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC "
                + "LIMIT " + count;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<Comment> comments = new ArrayList<>();
        while (cursor.moveToNext()) {
            comments.add(Db.CommentTable.parseCursor(cursor));
        }
        cursor.close();
        return comments;
    }

    public Observable<List<Comment>> getObservableComments(final int topicId, final int count) {
        return Observable.create((Observable.OnSubscribe<List<Comment>>) subscriber -> {
            subscriber.onNext(getComments(topicId, count));
            subscriber.onCompleted();
        });
    }

    public List<Comment> getOlderCommentsThan(Comment comment, int topicId, int count) {
        String query = "SELECT * FROM "
                + Db.CommentTable.TABLE_NAME + " WHERE "
                + Db.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " AND "
                + Db.CommentTable.COLUMN_TIME + " < " + comment.getTime().getTime() + " "
                + "ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC "
                + "LIMIT " + count;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<Comment> comments = new ArrayList<>();
        while (cursor.moveToNext()) {
            comments.add(Db.CommentTable.parseCursor(cursor));
        }
        cursor.close();
        return comments;
    }

    public Observable<List<Comment>> getObservableOlderCommentsThan(final Comment comment, final int topicId, final int count) {
        return Observable.create((Observable.OnSubscribe<List<Comment>>) subscriber -> {
            subscriber.onNext(getOlderCommentsThan(comment, topicId, count));
            subscriber.onCompleted();
        });
    }

    public void clear() {
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(Db.FilesTable.TABLE_NAME, null, null);
            sqLiteDatabase.delete(Db.CommentTable.TABLE_NAME, null, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }
}

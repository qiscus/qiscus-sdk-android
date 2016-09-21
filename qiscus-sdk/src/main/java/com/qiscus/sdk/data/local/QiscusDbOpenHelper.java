package com.qiscus.sdk.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class QiscusDbOpenHelper extends SQLiteOpenHelper {

    QiscusDbOpenHelper(Context context) {
        super(context, QiscusDb.DATABASE_NAME, null, QiscusDb.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(QiscusDb.CommentTable.CREATE);
            db.execSQL(QiscusDb.FilesTable.CREATE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        clearOldData(db);
        onCreate(db);
    }

    private void clearOldData(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.CommentTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.FilesTable.TABLE_NAME);
    }
}

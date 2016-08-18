package com.qiscus.library.chat.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {

    public DbOpenHelper(Context context) {
        super(context, Db.DATABASE_NAME, null, Db.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(Db.CommentTable.CREATE);
            db.execSQL(Db.FilesTable.CREATE);
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
        db.execSQL("DROP TABLE IF EXISTS " + Db.CommentTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Db.FilesTable.TABLE_NAME);
    }
}

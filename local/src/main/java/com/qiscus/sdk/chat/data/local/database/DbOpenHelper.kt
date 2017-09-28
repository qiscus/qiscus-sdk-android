package com.qiscus.sdk.chat.data.local.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created on : September 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class DbOpenHelper(context: Context) : SQLiteOpenHelper(context, Db.DATABASE_NAME, null, Db.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.transaction {
            db.execSQL(Db.RoomTable.CREATE)
            db.execSQL(Db.UserTable.CREATE)
            db.execSQL(Db.RoomMemberTable.CREATE)
            db.execSQL(Db.CommentTable.CREATE)
            db.execSQL(Db.FileTable.CREATE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        clearOldData(db)
        onCreate(db)
    }

    private fun clearOldData(db: SQLiteDatabase) {
        db.transaction {
            db.execSQL("DROP TABLE IF EXISTS " + Db.RoomTable.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + Db.UserTable.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + Db.RoomMemberTable.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + Db.CommentTable.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + Db.FileTable.TABLE_NAME)
        }
    }
}
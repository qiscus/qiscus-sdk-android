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
            db.execSQL(Db.MessageTable.CREATE)
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
            db.execSQL("DROP TABLE IF EXISTS " + Db.MessageTable.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + Db.FileTable.TABLE_NAME)
        }
    }
}
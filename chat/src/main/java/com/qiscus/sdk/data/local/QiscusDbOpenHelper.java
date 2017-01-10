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
            db.execSQL(QiscusDb.RoomTable.CREATE);
            db.execSQL(QiscusDb.RoomMemberTable.CREATE);
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
        db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.RoomTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.RoomMemberTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.CommentTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.FilesTable.TABLE_NAME);
    }
}

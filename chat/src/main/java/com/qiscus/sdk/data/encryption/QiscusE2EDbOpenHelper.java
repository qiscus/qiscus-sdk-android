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

package com.qiscus.sdk.data.encryption;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created on : March 06, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
final class QiscusE2EDbOpenHelper extends SQLiteOpenHelper {

    QiscusE2EDbOpenHelper(Context context) {
        super(context, QiscusE2EDb.DATABASE_NAME, null, QiscusE2EDb.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(QiscusE2EDb.BundleTable.CREATE);
            db.execSQL(QiscusE2EDb.ConversationTable.CREATE);
            db.execSQL(QiscusE2EDb.GroupConversationTable.CREATE);
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
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + QiscusE2EDb.BundleTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + QiscusE2EDb.ConversationTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + QiscusE2EDb.GroupConversationTable.TABLE_NAME);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}

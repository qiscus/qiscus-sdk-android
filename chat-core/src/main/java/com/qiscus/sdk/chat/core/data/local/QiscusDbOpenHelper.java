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

import static com.qiscus.sdk.chat.core.data.local.QiscusDb.DATABASE_MINIMUM_VERSION;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.qiscus.sdk.chat.core.util.QiscusLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class QiscusDbOpenHelper extends SQLiteOpenHelper {

    private Context context;
    private static QiscusDbOpenHelper instance;

    // ✅ Package-private singleton
    static synchronized QiscusDbOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new QiscusDbOpenHelper(context.getApplicationContext());
        }
        return instance;
    }

    // ✅ Private constructor
    private QiscusDbOpenHelper(Context context) {
        super(context, QiscusDb.DATABASE_NAME, null, QiscusDb.DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(QiscusDb.RoomTable.CREATE);
            db.execSQL(QiscusDb.MemberTable.CREATE);
            db.execSQL(QiscusDb.RoomMemberTable.CREATE);
            db.execSQL(QiscusDb.CommentTable.CREATE);
            db.execSQL(QiscusDb.FilesTable.CREATE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        if (!db.isReadOnly()) {
            db.enableWriteAheadLogging();
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        QiscusLogger.print("Opening database.. ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        QiscusLogger.print("Upgrade database from : " + oldVersion + " to : " + newVersion);

        if (oldVersion < DATABASE_MINIMUM_VERSION) {
            clearOldData(db);
            onCreate(db);
            return;
        }

        try {
            for (int i = oldVersion; i < newVersion; i++) {
                String migrationName = String.format("qiscus.db_from_%d_to_%d.sql", i, (i + 1));
                QiscusLogger.print("Looking for migration file : " + migrationName);
                readAndExecSQL(db, context, migrationName);
            }
        } catch (Exception e) {
            QiscusLogger.print("Exception running upgrade scripts : " + e.getMessage());
        }
    }

    private void readAndExecSQL(SQLiteDatabase db, Context context, String migrationName) {
        if (TextUtils.isEmpty(migrationName)) {
            QiscusLogger.print("SQL Script migration name is empty...");
            return;
        }

        QiscusLogger.print("SQL Script found...");
        AssetManager assets = context.getAssets();
        BufferedReader reader = null;

        try {
            InputStream inputStream = assets.open(migrationName);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            execSQLScript(db, reader);
        } catch (IOException e) {
            QiscusLogger.print("Failed read SQL Script : " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    QiscusLogger.print("Failed close reader : " + e.getMessage());
                }
            }
        }
    }

    private void execSQLScript(SQLiteDatabase db, BufferedReader reader) throws IOException {
        db.beginTransaction();
        try {
            String line;
            StringBuilder statement = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                statement.append(line);
                statement.append(System.getProperty("line.separator"));
                if (line.endsWith(";")) {
                    // ✅ Filter journal mode changes
                    String sql = statement.toString().trim();
                    if (!sql.toUpperCase().contains("PRAGMA JOURNAL_MODE")) {
                        db.execSQL(sql);
                    } else {
                        QiscusLogger.print("Skipping journal mode change: " + sql);
                    }
                    statement = new StringBuilder();
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void clearOldData(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.RoomTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.MemberTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.RoomMemberTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.CommentTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + QiscusDb.FilesTable.TABLE_NAME);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
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

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.RestrictTo;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.encryption.core.BundlePublicCollection;
import com.qiscus.sdk.data.encryption.core.GroupConversation;
import com.qiscus.sdk.data.encryption.core.SesameConversation;

import rx.Emitter;
import rx.Observable;

/**
 * Created on : March 06, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum QiscusE2EDataStore {
    INSTANCE;
    private final SQLiteDatabase sqLiteDatabase;

    QiscusE2EDataStore() {
        sqLiteDatabase = new QiscusE2EDbOpenHelper(Qiscus.getApps()).getReadableDatabase();
    }

    public static QiscusE2EDataStore getInstance() {
        return INSTANCE;
    }

    public Observable<BundlePublicCollection> getBundlePublicCollection(String userId) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getBundle(userId));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    public Observable<BundlePublicCollection> saveBundlePublicCollection(String userId, BundlePublicCollection bundlePublicCollection) {
        return Observable.create(subscriber -> {
            sqLiteDatabase.beginTransaction();
            try {
                if (isContainBundle(userId)) {
                    String where = QiscusE2EDb.BundleTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userId);

                    sqLiteDatabase.update(QiscusE2EDb.BundleTable.TABLE_NAME,
                            QiscusE2EDb.BundleTable.toContentValues(userId, bundlePublicCollection), where, null);
                } else {
                    sqLiteDatabase.insert(QiscusE2EDb.BundleTable.TABLE_NAME, null,
                            QiscusE2EDb.BundleTable.toContentValues(userId, bundlePublicCollection));
                }
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
            subscriber.onNext(getBundle(userId));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    private BundlePublicCollection getBundle(String userId) {
        String query = "SELECT * FROM "
                + QiscusE2EDb.BundleTable.TABLE_NAME + " WHERE "
                + QiscusE2EDb.BundleTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userId);

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToNext()) {
            BundlePublicCollection bundlePublicCollection = QiscusE2EDb.BundleTable.parseCursor(cursor);
            cursor.close();
            return bundlePublicCollection;
        } else {
            cursor.close();
            return null;
        }
    }

    private boolean isContainBundle(String userId) {
        String query = "SELECT * FROM "
                + QiscusE2EDb.BundleTable.TABLE_NAME + " WHERE "
                + QiscusE2EDb.BundleTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userId);

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    public Observable<SesameConversation> getSesameConversation(String userId) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getConversation(userId));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    public Observable<SesameConversation> saveSesameConversation(String userId, SesameConversation conversation) {
        return Observable.create(subscriber -> {
            sqLiteDatabase.beginTransaction();
            try {
                if (isContainConversation(userId)) {
                    String where = QiscusE2EDb.ConversationTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userId);

                    sqLiteDatabase.update(QiscusE2EDb.ConversationTable.TABLE_NAME,
                            QiscusE2EDb.ConversationTable.toContentValues(userId, conversation), where, null);
                } else {
                    sqLiteDatabase.insert(QiscusE2EDb.ConversationTable.TABLE_NAME, null,
                            QiscusE2EDb.ConversationTable.toContentValues(userId, conversation));
                }
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
            subscriber.onNext(getConversation(userId));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    private SesameConversation getConversation(String userId) {
        String query = "SELECT * FROM "
                + QiscusE2EDb.ConversationTable.TABLE_NAME + " WHERE "
                + QiscusE2EDb.ConversationTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userId);

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToNext()) {
            SesameConversation conversation = QiscusE2EDb.ConversationTable.parseCursor(cursor);
            cursor.close();
            return conversation;
        } else {
            cursor.close();
            return null;
        }
    }

    private boolean isContainConversation(String userId) {
        String query = "SELECT * FROM "
                + QiscusE2EDb.ConversationTable.TABLE_NAME + " WHERE "
                + QiscusE2EDb.ConversationTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userId);

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    public Observable<GroupConversation> getGroupConversation(long roomId) {
        return Observable.create(subscriber -> {
            subscriber.onNext(getLocalGroupConversation(roomId));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    public Observable<GroupConversation> saveGroupConversation(long roomId, GroupConversation conversation) {
        return Observable.create(subscriber -> {
            sqLiteDatabase.beginTransaction();
            try {
                if (isContainGroupConversation(roomId)) {
                    String where = QiscusE2EDb.GroupConversationTable.COLUMN_ROOM_ID + " = " + roomId;

                    sqLiteDatabase.update(QiscusE2EDb.GroupConversationTable.TABLE_NAME,
                            QiscusE2EDb.GroupConversationTable.toContentValues(roomId, conversation), where, null);
                } else {
                    sqLiteDatabase.insert(QiscusE2EDb.GroupConversationTable.TABLE_NAME, null,
                            QiscusE2EDb.GroupConversationTable.toContentValues(roomId, conversation));
                }
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
            subscriber.onNext(getLocalGroupConversation(roomId));
            subscriber.onCompleted();
        }, Emitter.BackpressureMode.BUFFER);
    }

    private GroupConversation getLocalGroupConversation(long roomId) {
        String query = "SELECT * FROM "
                + QiscusE2EDb.GroupConversationTable.TABLE_NAME + " WHERE "
                + QiscusE2EDb.GroupConversationTable.COLUMN_ROOM_ID + " = " + roomId;

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToNext()) {
            GroupConversation conversation = QiscusE2EDb.GroupConversationTable.parseCursor(cursor);
            cursor.close();
            return conversation;
        } else {
            cursor.close();
            return null;
        }
    }

    private boolean isContainGroupConversation(long roomId) {
        String query = "SELECT * FROM "
                + QiscusE2EDb.GroupConversationTable.TABLE_NAME + " WHERE "
                + QiscusE2EDb.GroupConversationTable.COLUMN_ROOM_ID + " = " + roomId;

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    public void clear() {
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(QiscusE2EDb.BundleTable.TABLE_NAME, null, null);
            sqLiteDatabase.delete(QiscusE2EDb.ConversationTable.TABLE_NAME, null, null);
            sqLiteDatabase.delete(QiscusE2EDb.GroupConversationTable.TABLE_NAME, null, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }
}

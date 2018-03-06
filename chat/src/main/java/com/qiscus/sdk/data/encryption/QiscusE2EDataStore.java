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
                sqLiteDatabase.insert(QiscusE2EDb.BundleTable.TABLE_NAME, null,
                        QiscusE2EDb.BundleTable.toContentValues(userId, bundlePublicCollection));
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
}

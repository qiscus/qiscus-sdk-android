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

import android.content.ContentValues;
import android.database.Cursor;

import com.qiscus.sdk.data.encryption.core.BundlePublicCollection;
import com.qiscus.sdk.data.encryption.core.IllegalDataSizeException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

/**
 * Created on : March 06, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
final class QiscusE2EDb {
    static final String DATABASE_NAME = "qiscus_encryption.db";
    static final int DATABASE_VERSION = 1;

    abstract static class BundleTable {
        static final String TABLE_NAME = "bundle_public_collection";
        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_BUNDLE_PUBLIC = "bundle_public";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_USER_ID + " TEXT PRIMARY KEY," +
                        COLUMN_BUNDLE_PUBLIC + " TEXT NOT NULL" +
                        " ); ";

        static ContentValues toContentValues(String userId, BundlePublicCollection bundlePublicCollection) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, userId);
            try {
                values.put(COLUMN_BUNDLE_PUBLIC, new String(bundlePublicCollection.encode()));
            } catch (IOException e) {
                e.printStackTrace();
                values.put(COLUMN_BUNDLE_PUBLIC, "");
            }
            return values;
        }

        static BundlePublicCollection parseCursor(Cursor cursor) {
            try {
                return BundlePublicCollection.decode(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUNDLE_PUBLIC)).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalDataSizeException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

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

package com.qiscus.sdk.chat.data.local.mapper

import android.content.ContentValues
import android.database.Cursor
import com.qiscus.sdk.chat.data.local.database.Db
import com.qiscus.sdk.chat.data.local.database.stringValue

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
internal fun toContentValues(messageUniqueId: String, localPath: String): ContentValues {
    return ContentValues().apply {
        put(Db.FileTable.COLUMN_MESSAGE_UNIQUE_ID, messageUniqueId)
        put(Db.FileTable.COLUMN_LOCAL_PATH, localPath)
    }
}

internal fun Cursor.getLocalPath(): String {
    return stringValue(Db.FileTable.COLUMN_LOCAL_PATH)
}
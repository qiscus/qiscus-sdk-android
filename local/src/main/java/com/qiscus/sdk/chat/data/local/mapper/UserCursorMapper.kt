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
import com.qiscus.sdk.chat.data.model.UserEntity

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun UserEntity.toContentValues(): ContentValues {
    return ContentValues().apply {
        put(Db.UserTable.COLUMN_USER_ID, id)
        put(Db.UserTable.COLUMN_USER_NAME, name)
        put(Db.UserTable.COLUMN_USER_AVATAR, avatar)
    }
}

fun Cursor.toUserEntity(): UserEntity {
    return UserEntity(
            stringValue(Db.UserTable.COLUMN_USER_ID),
            stringValue(Db.UserTable.COLUMN_USER_NAME),
            stringValue(Db.UserTable.COLUMN_USER_AVATAR)
    )
}
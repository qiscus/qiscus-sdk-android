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

package com.qiscus.sdk.chat.data.local

import android.database.DatabaseUtils
import com.qiscus.sdk.chat.data.local.database.Db
import com.qiscus.sdk.chat.data.local.database.DbOpenHelper
import com.qiscus.sdk.chat.data.local.database.transaction
import com.qiscus.sdk.chat.data.local.mapper.toContentValues
import com.qiscus.sdk.chat.data.local.mapper.toUserEntity
import com.qiscus.sdk.chat.data.model.UserEntity
import com.qiscus.sdk.chat.data.source.user.UserLocal

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class UserLocalImpl(dbOpenHelper: DbOpenHelper) : UserLocal {

    private val database = dbOpenHelper.readableDatabase

    override fun addUser(userEntity: UserEntity) {
        if (!isExistUser(userEntity.id)) {
            database.transaction {
                database.insert(Db.UserTable.TABLE_NAME, null, userEntity.toContentValues())
            }
        }
    }

    override fun updateUser(userEntity: UserEntity) {
        val oldUser = getUser(userEntity.id)
        if (oldUser != null && oldUser != userEntity) {
            val where = Db.UserTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userEntity.id)
            database.transaction {
                database.update(Db.UserTable.TABLE_NAME, userEntity.toContentValues(), where, null)
            }
        }
    }

    override fun addOrUpdateUser(userEntity: UserEntity) {
        val oldUser = getUser(userEntity.id)
        if (oldUser != null && oldUser != userEntity) {
            val where = Db.UserTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userEntity.id)
            database.transaction {
                database.update(Db.UserTable.TABLE_NAME, userEntity.toContentValues(), where, null)
            }
        } else if (oldUser == null) {
            database.transaction {
                database.insert(Db.UserTable.TABLE_NAME, null, userEntity.toContentValues())
            }
        }
    }

    override fun deleteUser(userEntity: UserEntity) {
        if (isExistUser(userEntity.id)) {
            val where = Db.UserTable.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(userEntity.id)
            database.transaction {
                database.delete(Db.UserTable.TABLE_NAME, where, null)
            }
        }
    }

    override fun getUser(userId: String): UserEntity? {
        val query = "SELECT * FROM ${Db.UserTable.TABLE_NAME} WHERE ${Db.UserTable.COLUMN_USER_ID} " +
                "= ${DatabaseUtils.sqlEscapeString(userId)}"
        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val userEntity = cursor.toUserEntity()
            cursor.close()
            return userEntity
        }

        cursor.close()
        return null
    }

    override fun clearData() {
        database.transaction {
            database.delete(Db.UserTable.TABLE_NAME, null, null)
        }
    }

    private fun isExistUser(userId: String): Boolean {
        val query = "SELECT * FROM ${Db.UserTable.TABLE_NAME} WHERE ${Db.UserTable.COLUMN_USER_ID} " +
                "= ${DatabaseUtils.sqlEscapeString(userId)}"
        val cursor = database.rawQuery(query, null)
        val contains = cursor.count > 0
        cursor.close()
        return contains
    }
}
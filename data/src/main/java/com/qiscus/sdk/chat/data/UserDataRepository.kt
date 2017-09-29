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

package com.qiscus.sdk.chat.data

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.mapper.toEntity
import com.qiscus.sdk.chat.data.source.user.UserLocal
import com.qiscus.sdk.chat.domain.model.User
import com.qiscus.sdk.chat.domain.repository.UserRepository
import io.reactivex.Completable
import io.reactivex.Maybe

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class UserDataRepository(private val userLocal: UserLocal) : UserRepository {

    override fun addUser(user: User): Completable {
        return Completable.defer {
            userLocal.addUser(user.toEntity())
            Completable.complete()
        }
    }

    override fun updateUser(user: User): Completable {
        return Completable.defer {
            userLocal.updateUser(user.toEntity())
            Completable.complete()
        }
    }

    override fun addOrUpdateUser(user: User): Completable {
        return Completable.defer {
            userLocal.addOrUpdateUser(user.toEntity())
            Completable.complete()
        }
    }

    override fun deleteUser(user: User): Completable {
        return Completable.defer {
            userLocal.deleteUser(user.toEntity())
            Completable.complete()
        }
    }

    override fun getUser(userId: String): Maybe<User> {
        val user = userLocal.getUser(userId) ?: return Maybe.empty()
        return Maybe.defer { Maybe.just(user.toDomainModel()) }
    }

    override fun clearData(): Completable {
        return Completable.defer {
            userLocal.clearData()
            Completable.complete()
        }
    }
}
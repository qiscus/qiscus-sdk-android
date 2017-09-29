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

package com.qiscus.sdk.chat.domain.repository

import com.qiscus.sdk.chat.domain.model.Account
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created on : August 30, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface AccountRepository {
    fun requestNonce(): Single<String>

    fun authenticate(token: String): Single<Account>

    fun authenticateWithKey(userId: String, userKey: String, name: String = userId, avatarUrl: String = ""): Single<Account>

    fun isAuthenticated(): Single<Boolean>

    fun getAccount(): Single<Account>

    fun updateAccount(name: String = "", avatarUrl: String = ""): Single<Account>

    fun clearData(): Completable
}
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

package com.qiscus.sdk.chat.data.source.account

import com.qiscus.sdk.chat.data.model.AccountEntity
import io.reactivex.Single

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface AccountRemote {
    fun requestNonce(): Single<String>

    fun authenticate(token: String): Single<AccountEntity>

    fun authenticateWithKey(userId: String, userKey: String, name: String = userId, avatarUrl: String = ""): Single<AccountEntity>

    fun updateAccount(name: String = "", avatarUrl: String = ""): Single<AccountEntity>
}
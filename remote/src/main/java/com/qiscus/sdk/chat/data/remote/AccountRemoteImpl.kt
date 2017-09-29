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

package com.qiscus.sdk.chat.data.remote

import com.qiscus.sdk.chat.data.model.AccountEntity
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.account.AccountRemote
import io.reactivex.Single

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class AccountRemoteImpl(private val accountLocal: AccountLocal, private val qiscusRestApi: QiscusRestApi) : AccountRemote {

    override fun requestNonce(): Single<String> {
        return qiscusRestApi.requestNonce().map { it.results.nonce }
    }

    override fun authenticateWithKey(userId: String, userKey: String, name: String, avatarUrl: String): Single<AccountEntity> {
        return qiscusRestApi.authenticateWithKey(userId, userKey, name, avatarUrl)
                .map { it.results.user.toEntity() }
    }

    override fun authenticate(token: String): Single<AccountEntity> {
        return qiscusRestApi.authenticate(token)
                .map { it.results.user.toEntity() }
    }

    override fun updateAccount(name: String, avatarUrl: String): Single<AccountEntity> {
        return qiscusRestApi.updateProfile(accountLocal.getAccount().token, name, avatarUrl)
                .map { it.results.user.toEntity() }
    }
}
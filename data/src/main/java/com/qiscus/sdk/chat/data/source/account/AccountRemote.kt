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
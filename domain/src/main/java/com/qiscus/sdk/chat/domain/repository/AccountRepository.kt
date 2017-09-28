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
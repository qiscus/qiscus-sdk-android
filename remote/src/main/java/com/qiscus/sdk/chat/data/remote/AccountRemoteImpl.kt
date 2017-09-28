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
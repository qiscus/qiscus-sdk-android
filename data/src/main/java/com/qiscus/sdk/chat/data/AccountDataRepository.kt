package com.qiscus.sdk.chat.data

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.account.AccountRemote
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class AccountDataRepository(private val accountLocal: AccountLocal,
                            private val accountRemote: AccountRemote) : AccountRepository {

    override fun requestNonce(): Single<String> {
        return accountRemote.requestNonce()
    }

    override fun authenticate(token: String): Single<Account> {
        return accountRemote.authenticate(token)
                .doOnSuccess { accountLocal.saveAccount(it) }
                .map { it.toDomainModel() }
    }

    override fun authenticateWithKey(userId: String, userKey: String, name: String, avatarUrl: String): Single<Account> {
        return accountRemote.authenticateWithKey(userId, userKey, name, avatarUrl)
                .doOnSuccess { accountLocal.saveAccount(it) }
                .map { it.toDomainModel() }
    }

    override fun isAuthenticated(): Single<Boolean> {
        return Single.defer { Single.just(accountLocal.isAuthenticate()) }
    }

    override fun getAccount(): Single<Account> {
        return Single.defer { Single.just(accountLocal.getAccount().toDomainModel()) }
    }

    override fun updateAccount(name: String, avatarUrl: String): Single<Account> {
        return accountRemote.updateAccount(name, avatarUrl)
                .doOnSuccess { accountLocal.saveAccount(it) }
                .map { it.toDomainModel() }
    }

    override fun clearData(): Completable {
        return Completable.defer {
            accountLocal.clearData()
            Completable.complete()
        }
    }
}
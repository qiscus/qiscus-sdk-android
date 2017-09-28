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
package com.qiscus.sdk.chat.domain.repository

import com.qiscus.sdk.chat.domain.model.User
import io.reactivex.Completable
import io.reactivex.Maybe

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface UserRepository {
    fun addUser(user: User): Completable

    fun updateUser(user: User): Completable

    fun addOrUpdateUser(user: User): Completable

    fun deleteUser(user: User): Completable

    fun getUser(userId: String): Maybe<User>

    fun clearData(): Completable
}
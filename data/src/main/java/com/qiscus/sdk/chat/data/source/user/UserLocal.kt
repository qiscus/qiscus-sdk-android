package com.qiscus.sdk.chat.data.source.user

import com.qiscus.sdk.chat.data.model.UserEntity

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface UserLocal {
    fun addUser(userEntity: UserEntity)

    fun updateUser(userEntity: UserEntity)

    fun addOrUpdateUser(userEntity: UserEntity)

    fun deleteUser(userEntity: UserEntity)

    fun getUser(userId: String): UserEntity?

    fun clearData()
}
package com.qiscus.sdk.chat.data.mapper

import com.qiscus.sdk.chat.data.model.UserEntity
import com.qiscus.sdk.chat.domain.model.User

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun UserEntity.toDomainModel(): User {
    return User(id, name, avatar)
}

fun User.toEntity(): UserEntity {
    return UserEntity(id, name, avatar)
}
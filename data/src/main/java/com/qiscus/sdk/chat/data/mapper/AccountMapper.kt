package com.qiscus.sdk.chat.data.mapper

import com.qiscus.sdk.chat.data.model.AccountEntity
import com.qiscus.sdk.chat.domain.model.Account

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun AccountEntity.toDomainModel(): Account {
    return Account(user.toDomainModel(), token)
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(user.toEntity(), token)
}
package com.qiscus.sdk.chat.data.source.account

import com.qiscus.sdk.chat.data.model.AccountEntity

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface AccountLocal {
    fun saveAccount(accountEntity: AccountEntity)

    fun getAccount(): AccountEntity

    fun isAuthenticate(): Boolean

    fun clearData()
}
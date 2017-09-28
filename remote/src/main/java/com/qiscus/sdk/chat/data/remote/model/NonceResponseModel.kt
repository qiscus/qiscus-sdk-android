package com.qiscus.sdk.chat.data.remote.model

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class NonceResponseModel(var results: Results, var status: Int) {
    data class Results(var expiredAt: Long, var nonce: String)
}
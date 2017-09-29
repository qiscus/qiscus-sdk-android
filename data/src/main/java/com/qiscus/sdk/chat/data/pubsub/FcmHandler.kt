package com.qiscus.sdk.chat.data.pubsub

/**
 * Created on : September 29, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface FcmHandler {
    fun handle(data: Map<String, String>): Boolean
}
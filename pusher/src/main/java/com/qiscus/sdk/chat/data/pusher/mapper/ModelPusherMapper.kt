package com.qiscus.sdk.chat.data.pusher.mapper

/**
 * Created on : September 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface ModelPusherMapper<in P, out E> {
    fun mapFromPusher(payload: P): E
}
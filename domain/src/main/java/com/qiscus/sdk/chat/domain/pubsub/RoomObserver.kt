package com.qiscus.sdk.chat.domain.pubsub

import com.qiscus.sdk.chat.domain.model.UserTyping
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Created on : September 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface RoomObserver {
    fun listenUserTyping(roomId: String): Observable<UserTyping>

    fun setTyping(roomId: String, typing: Boolean) : Completable
}
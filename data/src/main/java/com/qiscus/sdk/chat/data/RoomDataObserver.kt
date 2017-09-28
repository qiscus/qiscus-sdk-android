package com.qiscus.sdk.chat.data

import com.qiscus.sdk.chat.data.pubsub.room.RoomSubscriber
import com.qiscus.sdk.chat.domain.model.UserTyping
import com.qiscus.sdk.chat.domain.pubsub.QiscusPubSubClient
import com.qiscus.sdk.chat.domain.pubsub.RoomObserver
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Created on : September 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class RoomDataObserver(private val pubSubClient: QiscusPubSubClient,
                       private val roomSubscriber: RoomSubscriber) : RoomObserver {

    override fun listenUserTyping(roomId: String): Observable<UserTyping> {
        return roomSubscriber.listenUserTyping(roomId)
                .doOnSubscribe { pubSubClient.listenUserTyping(roomId) }
                .doOnDispose { pubSubClient.unlistenUserTyping(roomId) }
    }

    override fun setTyping(roomId: String, typing: Boolean): Completable {
        return Completable.defer { Completable.complete() }
                .doOnSubscribe { pubSubClient.publishTypingStatus(roomId, typing) }
    }
}
package com.qiscus.sdk.chat.data.pusher

import com.qiscus.sdk.chat.data.model.MessageIdEntity
import com.qiscus.sdk.chat.data.model.MessageStateEntity
import com.qiscus.sdk.chat.data.pubsub.message.MessageSubscriber
import com.qiscus.sdk.chat.data.source.message.MessageLocal
import com.qiscus.sdk.chat.data.source.message.MessageRemote
import com.qiscus.sdk.chat.data.util.SyncHandler
import io.reactivex.schedulers.Schedulers

/**
 * Created on : September 29, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class SyncHandlerImpl(private val messageLocal: MessageLocal,
                      private val messageRemote: MessageRemote,
                      messageSubscriber: MessageSubscriber) : SyncHandler {

    init {
        messageSubscriber.listenMessageAdded()
                .filter { it.state.intValue >= MessageStateEntity.ON_SERVER.intValue }
                .doOnNext {
                    val messages = messageLocal.getOnServerMessages(it.room.id, it.messageId, 10)
                    for (i in 0 until messages.size - 1) {
                        if (messages[i].messageId.beforeId != messages[i + 1].messageId.id) {
                            sync(it.room.id, messages[i + 1].messageId)
                            break
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }

    override fun sync() {
        val lastId = messageLocal.getLastOnServerMessageId()
        if (lastId != null) {
            sync(lastId)
        }
    }

    override fun sync(lastMessageId: MessageIdEntity) {
        messageRemote.sync(lastMessageId)
                .doOnSuccess {
                    if (it.size > 20) {
                        it.forEach { messageLocal.addOrUpdateMessage(it) }
                    } else {
                        it.forEach { messageLocal.saveAndNotify(it) }
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }

    override fun sync(roomId: String, lastMessageId: MessageIdEntity) {
        messageRemote.sync(roomId, lastMessageId)
                .doOnSuccess {
                    if (it.size > 20) {
                        it.forEach { messageLocal.addOrUpdateMessage(it) }
                    } else {
                        it.forEach { messageLocal.saveAndNotify(it) }
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }
}
package com.qiscus.sdk.chat.data.pusher

import com.qiscus.sdk.chat.data.model.MessageEntity
import com.qiscus.sdk.chat.data.pubsub.FcmHandler
import com.qiscus.sdk.chat.data.pusher.mapper.MessagePayloadMapper
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.message.MessageLocal
import com.qiscus.sdk.chat.data.source.message.MessageRemote
import com.qiscus.sdk.chat.domain.pubsub.QiscusPubSubClient
import io.reactivex.schedulers.Schedulers

/**
 * Created on : September 29, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class FcmHandlerImpl(private val accountLocal: AccountLocal,
                     private val messageLocal: MessageLocal,
                     private val messageRemote: MessageRemote,
                     private val qiscusPubSubClient: QiscusPubSubClient,
                     private val messagePayloadMapper: MessagePayloadMapper) : FcmHandler {

    override fun handle(data: Map<String, String>): Boolean {
        if (data.containsKey("qiscus_sdk")) {
            if (accountLocal.isAuthenticated()) {
                if (!qiscusPubSubClient.isConnected()) {
                    qiscusPubSubClient.restartConnection()
                }

                if (data.containsKey("payload")) {
                    val message = messagePayloadMapper.mapFromPusher(data["payload"]!!)
                    messageLocal.saveAndNotify(message)
                    if (message.sender.id != accountLocal.getAccount().user.id) {
                        notifyDelivered(message)
                    }
                }
            }
            return true
        }

        return false
    }

    private fun notifyDelivered(message: MessageEntity) {
        messageRemote.updateLastDeliveredMessage(message.room.id, message.messageId)
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }
}
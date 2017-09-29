package com.qiscus.sdk.chat.data.pusher

import com.qiscus.sdk.chat.data.model.CommentEntity
import com.qiscus.sdk.chat.data.pubsub.FcmHandler
import com.qiscus.sdk.chat.data.pusher.mapper.CommentPayloadMapper
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.comment.CommentLocal
import com.qiscus.sdk.chat.data.source.comment.CommentRemote
import com.qiscus.sdk.chat.domain.pubsub.QiscusPubSubClient
import io.reactivex.schedulers.Schedulers

/**
 * Created on : September 29, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class FcmHandlerImpl(private val accountLocal: AccountLocal,
                     private val commentLocal: CommentLocal,
                     private val commentRemote: CommentRemote,
                     private val qiscusPubSubClient: QiscusPubSubClient,
                     private val commentPayloadMapper: CommentPayloadMapper) : FcmHandler {

    override fun handle(data: Map<String, String>): Boolean {
        if (data.containsKey("qiscus_sdk")) {
            if (accountLocal.isAuthenticate()) {
                if (!qiscusPubSubClient.isConnected()) {
                    qiscusPubSubClient.restartConnection()
                }

                if (data.containsKey("payload")) {
                    val comment = commentPayloadMapper.mapFromPusher(data["payload"]!!)
                    commentLocal.saveAndNotify(comment)
                    if (comment.sender.id != accountLocal.getAccount().user.id) {
                        notifyDelivered(comment)
                    }
                }
            }
            return true
        }

        return false
    }

    private fun notifyDelivered(comment: CommentEntity) {
        commentRemote.updateLastDeliveredComment(comment.room.id, comment.commentId)
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }
}
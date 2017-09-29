package com.qiscus.sdk.chat.data.pusher

import android.util.Log
import com.qiscus.sdk.chat.data.model.CommentIdEntity
import com.qiscus.sdk.chat.data.model.CommentStateEntity
import com.qiscus.sdk.chat.data.pubsub.comment.CommentSubscriber
import com.qiscus.sdk.chat.data.source.comment.CommentLocal
import com.qiscus.sdk.chat.data.source.comment.CommentRemote
import com.qiscus.sdk.chat.data.util.SyncHandler
import io.reactivex.schedulers.Schedulers

/**
 * Created on : September 29, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class SyncHandlerImpl(private val commentLocal: CommentLocal,
                      private val commentRemote: CommentRemote,
                      commentSubscriber: CommentSubscriber) : SyncHandler {

    init {
        commentSubscriber.listenCommentAdded()
                .doOnNext { Log.d("ZETRA", "New comment: ${it.message}") }
                .filter { it.state.intValue >= CommentStateEntity.ON_SERVER.intValue }
                .doOnNext {
                    val comments = commentLocal.getOnServerComments(it.room.id, it.commentId, 10)
                    for (i in 0 until comments.size - 1) {
                        if (comments[i].commentId.commentBeforeId != comments[i + 1].commentId.id) {
                            sync(it.room.id, comments[i + 1].commentId)
                            break
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }

    override fun sync() {
        Log.d("ZETRA", "sync")
        val lastId = commentLocal.getLastOnServerCommentId()
        if (lastId != null) {
            sync(lastId)
        }
    }

    override fun sync(lastCommentId: CommentIdEntity) {
        Log.d("ZETRA", "sync(${lastCommentId.id})")
        commentRemote.sync(lastCommentId)
                .doOnSuccess {
                    if (it.size > 20) {
                        it.forEach { commentLocal.addOrUpdateComment(it) }
                    } else {
                        it.forEach { commentLocal.saveAndNotify(it) }
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }

    override fun sync(roomId: String, lastCommentId: CommentIdEntity) {
        Log.d("ZETRA", "sync($roomId, ${lastCommentId.id}")
        commentRemote.sync(roomId, lastCommentId)
                .doOnSuccess {
                    if (it.size > 20) {
                        it.forEach { commentLocal.addOrUpdateComment(it) }
                    } else {
                        it.forEach { commentLocal.saveAndNotify(it) }
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }
}
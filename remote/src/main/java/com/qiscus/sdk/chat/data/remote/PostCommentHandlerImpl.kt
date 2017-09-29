/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.chat.data.remote

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.model.CommentEntity
import com.qiscus.sdk.chat.data.model.CommentIdEntity
import com.qiscus.sdk.chat.data.model.CommentStateEntity
import com.qiscus.sdk.chat.data.model.FileAttachmentCommentEntity
import com.qiscus.sdk.chat.data.pubsub.file.FilePublisher
import com.qiscus.sdk.chat.data.source.comment.CommentLocal
import com.qiscus.sdk.chat.data.source.comment.CommentRemote
import com.qiscus.sdk.chat.data.source.file.FileRemote
import com.qiscus.sdk.chat.data.source.file.ProgressListener
import com.qiscus.sdk.chat.data.util.PostCommentHandler
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

/**
 * Created on : September 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class PostCommentHandlerImpl(private val commentLocal: CommentLocal,
                             private val commentRemote: CommentRemote,
                             private val fileRemote: FileRemote,
                             private val filePublisher: FilePublisher) : PostCommentHandler {
    private val pendingTask = HashMap<CommentIdEntity, Disposable>()

    override fun tryResendPendingComment() {
        Observable.defer { Observable.fromCallable { commentLocal.getPendingComments() } }
                .flatMap { Observable.fromIterable(it) }
                .filter { !pendingTask.containsKey(it.commentId) }
                .doOnNext { resendComment(it) }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }

    override fun cancelPendingComment(comment: CommentEntity) {
        if (pendingTask.containsKey(comment.commentId)) {
            val disposable = pendingTask[comment.commentId]
            if (disposable!!.isDisposed) {
                disposable.dispose()
            }
            pendingTask.remove(comment.commentId)
        }
    }

    private fun resendComment(comment: CommentEntity) {
        comment.state = CommentStateEntity.SENDING
        commentLocal.addOrUpdateComment(comment)

        if (comment is FileAttachmentCommentEntity) {
            resendFileComment(comment)
            return
        }

        val disposable = commentRemote.postComment(comment)
                .doOnSuccess { onSuccessSendingComment(it) }
                .doOnError { onErrorSendingComment(comment, it) }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        pendingTask.put(comment.commentId, disposable)
    }

    private fun resendFileComment(comment: FileAttachmentCommentEntity) {
        if (!comment.getAttachmentUrl().isBlank()) {//We have upload it, just need to send comment
            val disposable = commentRemote.postComment(comment)
                    .doOnSuccess { onSuccessSendingComment(it) }
                    .doOnError { onErrorSendingComment(comment, it) }
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, {})

            pendingTask.put(comment.commentId, disposable)
            return
        }

        if (comment.file == null || !comment.file!!.exists()) {//File have been deleted, so can not upload it anymore
            comment.state = CommentStateEntity.FAILED
            commentLocal.addOrUpdateComment(comment)
            pendingTask.remove(comment.commentId)
            return
        }

        //Reuploading the file
        val progress = FileAttachmentProgress(comment.toDomainModel(), FileAttachmentProgress.State.UPLOADING, 0)
        val disposable = fileRemote.upload(comment.file!!, object : ProgressListener {
            override fun onProgress(total: Int) {
                progress.progress = total
                filePublisher.onFileProgressUpdated(progress)
            }
        })
                .doOnSuccess { comment.updateAttachmentUrl(it) }
                .doOnError { onErrorSendingComment(comment, it) }
                .flatMap {
                    commentRemote.postComment(comment)
                            .doOnSuccess { onSuccessSendingComment(it) }
                            .doOnError { onErrorSendingComment(comment, it) }
                }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        pendingTask.put(comment.commentId, disposable)
    }

    private fun onSuccessSendingComment(comment: CommentEntity) {
        pendingTask.remove(comment.commentId)
        commentLocal.addOrUpdateComment(comment)
    }

    private fun onErrorSendingComment(comment: CommentEntity, throwable: Throwable) {
        pendingTask.remove(comment.commentId)

        var state = CommentStateEntity.PENDING
        if (throwable is HttpException) { //Error response from server
            //Means something wrong with server, e.g user is not member of these room anymore
            if (throwable.code() >= 400) {
                state = CommentStateEntity.FAILED
            }
        }

        comment.state = state
        commentLocal.addOrUpdateComment(comment)
    }
}
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

package com.qiscus.sdk.chat.data

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.mapper.toEntity
import com.qiscus.sdk.chat.data.model.CommentEntity
import com.qiscus.sdk.chat.data.model.CommentIdEntity
import com.qiscus.sdk.chat.data.model.CommentStateEntity
import com.qiscus.sdk.chat.data.pubsub.file.FilePublisher
import com.qiscus.sdk.chat.data.source.comment.CommentLocal
import com.qiscus.sdk.chat.data.source.comment.CommentRemote
import com.qiscus.sdk.chat.data.source.file.FileLocal
import com.qiscus.sdk.chat.data.source.file.FileRemote
import com.qiscus.sdk.chat.data.source.file.ProgressListener
import com.qiscus.sdk.chat.data.util.FileManager
import com.qiscus.sdk.chat.data.util.PostCommentHandler
import com.qiscus.sdk.chat.domain.model.*
import com.qiscus.sdk.chat.domain.repository.CommentRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentDataRepository(private val commentLocal: CommentLocal,
                            private val commentRemote: CommentRemote,
                            private val fileLocal: FileLocal,
                            private val fileRemote: FileRemote,
                            private val fileManager: FileManager,
                            private val filePublisher: FilePublisher,
                            private val postCommentHandler: PostCommentHandler) : CommentRepository {

    override fun postComment(comment: Comment): Completable {
        if (comment is FileAttachmentComment) {
            return postAttachmentComment(comment)
        }

        val commentEntity = comment.toEntity()
        return commentRemote.postComment(commentEntity)
                .doOnSubscribe { commentLocal.saveAndNotify(commentEntity) }
                .doOnSuccess { commentLocal.addOrUpdateComment(it) }
                .doOnError { handleErrorPostComment(commentEntity) }
                .toCompletable()
    }

    private fun postAttachmentComment(comment: FileAttachmentComment): Completable {
        val attachmentComment = comment.toEntity()
        attachmentComment.file = fileManager.saveFile(comment.file!!)

        val progress = FileAttachmentProgress(attachmentComment.toDomainModel(),
                FileAttachmentProgress.State.UPLOADING, 0)

        return fileRemote.upload(comment.file!!, object : ProgressListener {
            override fun onProgress(total: Int) {
                progress.progress = total
                filePublisher.onFileProgressUpdated(progress)
            }
        })
                .doOnSubscribe {
                    fileLocal.saveLocalPath(attachmentComment.commentId, comment.file!!)
                    commentLocal.saveAndNotify(attachmentComment)
                }
                .doOnSuccess { attachmentComment.updateAttachmentUrl(it) }
                .doOnError { handleErrorPostComment(attachmentComment) }
                .flatMap {
                    commentRemote.postComment(attachmentComment)
                            .doOnSuccess { commentLocal.addOrUpdateComment(it) }
                            .doOnError { handleErrorPostComment(attachmentComment) }
                }
                .toCompletable()
    }

    private fun handleErrorPostComment(commentEntity: CommentEntity) {
        commentEntity.state = CommentStateEntity.PENDING
        commentLocal.addOrUpdateComment(commentEntity)
    }

    override fun downloadAttachmentComment(comment: FileAttachmentComment): Single<FileAttachmentComment> {
        val attachmentComment = comment.toEntity()
        val progress = FileAttachmentProgress(attachmentComment.toDomainModel(),
                FileAttachmentProgress.State.DOWNLOADING, 0)

        return fileRemote.download(attachmentComment.getAttachmentUrl(), object : ProgressListener {
            override fun onProgress(total: Int) {
                progress.progress = total
                filePublisher.onFileProgressUpdated(progress)
            }
        })
                .doOnSuccess {
                    attachmentComment.file = it
                    fileLocal.saveLocalPath(attachmentComment.commentId, attachmentComment.file!!)
                    commentLocal.addOrUpdateComment(attachmentComment)
                }
                .map { attachmentComment.toDomainModel() }
    }

    override fun getComments(roomId: String): Single<List<Comment>> {
        commentRemote.getComments(roomId)
                .doOnSuccess { it.forEach { commentLocal.addOrUpdateComment(it) } }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        return Single.defer { Single.fromCallable { commentLocal.getComments(roomId, 20) } }
                .flatMap {
                    if (it.isEmpty() || !isValidChainingComments(it)) {
                        return@flatMap commentRemote.getComments(roomId)
                                .doOnSuccess {
                                    it.forEach {
                                        determineCommentState(it)
                                        commentLocal.addOrUpdateComment(it)
                                    }
                                }
                    }
                    return@flatMap Single.just(it)
                }.map { it.map { it.toDomainModel() } }
    }

    override fun getComments(roomId: String, lastCommentId: CommentId, limit: Int): Single<List<Comment>> {
        val lastComment = lastCommentId.toEntity()

        commentRemote.getComments(roomId, lastComment, limit)
                .doOnSuccess { it.forEach { commentLocal.addOrUpdateComment(it) } }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        return Single.defer { Single.fromCallable { commentLocal.getComments(roomId, lastComment, limit) } }
                .flatMap {
                    if (it.isEmpty() || !isValidOlderComments(it, lastComment)) {
                        return@flatMap commentRemote.getComments(roomId, lastComment, limit)
                                .doOnSuccess {
                                    it.forEach {
                                        determineCommentState(it)
                                        commentLocal.addOrUpdateComment(it)
                                    }
                                }
                    }
                    return@flatMap Single.just(it)
                }.map { it.map { it.toDomainModel() } }
    }

    override fun updateCommentState(roomId: String, commentId: CommentId, commentState: CommentState): Completable {
        val lastComment = commentId.toEntity()
        if (commentState == CommentState.DELIVERED) {
            return commentRemote.updateLastDeliveredComment(roomId, lastComment)
        } else if (commentState == CommentState.READ) {
            return commentRemote.updateLastReadComment(roomId, lastComment)
        }
        return Completable.complete()
    }

    override fun deleteComment(comment: Comment): Completable {
        val commentEntity = comment.toEntity()
        return Completable.defer {
            postCommentHandler.cancelPendingComment(commentEntity)
            commentLocal.deleteComment(commentEntity)
            Completable.complete()
        }
    }

    override fun clearData(): Completable {
        return Completable.defer {
            fileLocal.clearData()
            commentLocal.clearData()
            Completable.complete()
        }
    }

    private fun determineCommentState(commentEntity: CommentEntity) {
        if (commentEntity.state.intValue >= CommentStateEntity.ON_SERVER.intValue
                && commentEntity.state.intValue < CommentStateEntity.READ.intValue) {
            val lastRead = commentLocal.getLastReadCommentId(commentEntity.room.id)
            if (lastRead != null && commentEntity.commentId.id <= lastRead.id) {
                commentEntity.state = CommentStateEntity.READ
            } else {
                val lastDelivered = commentLocal.getLastDeliveredCommentId(commentEntity.room.id)
                if (lastDelivered != null && commentEntity.commentId.id <= lastDelivered.id) {
                    commentEntity.state = CommentStateEntity.DELIVERED
                }
            }
        }
    }

    private fun cleanFailedComments(comments: List<CommentEntity>): List<CommentEntity> {
        return comments.filter { it.commentId.id.isNotBlank() }
    }

    private fun isValidChainingComments(comments: List<CommentEntity>): Boolean {
        val cleanedComments = cleanFailedComments(comments)
        val size = cleanedComments.size
        return (0 until size - 1)
                .none { cleanedComments[it].commentId.commentBeforeId != cleanedComments[it + 1].commentId.id }
    }

    private fun isValidOlderComments(comments: List<CommentEntity>, lastComment: CommentIdEntity): Boolean {
        if (comments.isEmpty()) return false

        val cleanedComments = cleanFailedComments(comments)
        var containsLastValidComment = cleanedComments.isEmpty() || lastComment.id.isNotBlank()
        val size = cleanedComments.size

        if (size == 1) {
            return (cleanedComments[0].commentId.commentBeforeId.isBlank() ||
                    cleanedComments[0].commentId.commentBeforeId == "0")
                    && lastComment.commentBeforeId == cleanedComments[0].commentId.id
        }

        for (i in 0 until size - 1) {
            if (!containsLastValidComment && cleanedComments[i].commentId.id == lastComment.commentBeforeId) {
                containsLastValidComment = true
            }

            if (cleanedComments[i].commentId.commentBeforeId != cleanedComments[i + 1].commentId.id) {
                return false
            }
        }
        return containsLastValidComment
    }
}
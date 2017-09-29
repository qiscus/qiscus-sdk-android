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

import com.qiscus.sdk.chat.data.model.CommentEntity
import com.qiscus.sdk.chat.data.model.CommentIdEntity
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.comment.CommentRemote
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentRemoteImpl(private val accountLocal: AccountLocal,
                        private val qiscusRestApi: QiscusRestApi) : CommentRemote {

    override fun postComment(commentEntity: CommentEntity): Single<CommentEntity> {
        return qiscusRestApi.postComment(accountLocal.getAccount().token, commentEntity.room.id,
                commentEntity.message, commentEntity.commentId.uniqueId, commentEntity.type.rawType,
                commentEntity.type.payload.toString())
                .map { it.results.comment.toEntity() }
    }

    override fun getComments(roomId: String): Single<List<CommentEntity>> {
        return qiscusRestApi.getComments(accountLocal.getAccount().token, roomId, false)
                .map { it.results.comments.map { it.toEntity() } }
    }

    override fun getComments(roomId: String, lastCommentId: CommentIdEntity, limit: Int): Single<List<CommentEntity>> {
        return qiscusRestApi.getComments(accountLocal.getAccount().token, roomId, lastCommentId.id, limit, false)
                .map { it.results.comments.map { it.toEntity() } }
    }

    override fun updateLastDeliveredComment(roomId: String, commentId: CommentIdEntity): Completable {
        return qiscusRestApi.updateCommentStatus(accountLocal.getAccount().token, roomId, commentId.id, "")
    }

    override fun updateLastReadComment(roomId: String, commentId: CommentIdEntity): Completable {
        return qiscusRestApi.updateCommentStatus(accountLocal.getAccount().token, roomId, "", commentId.id)
    }
}
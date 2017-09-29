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

package com.qiscus.sdk.chat.data.source.comment

import com.qiscus.sdk.chat.data.model.CommentEntity
import com.qiscus.sdk.chat.data.model.CommentIdEntity
import io.reactivex.Completable
import io.reactivex.Single

interface CommentRemote {
    fun postComment(commentEntity: CommentEntity): Single<CommentEntity>

    fun getComments(roomId: String): Single<List<CommentEntity>>

    fun getComments(roomId: String, lastCommentId: CommentIdEntity, limit: Int): Single<List<CommentEntity>>

    fun updateLastDeliveredComment(roomId: String, commentId: CommentIdEntity): Completable

    fun updateLastReadComment(roomId: String, commentId: CommentIdEntity): Completable
}
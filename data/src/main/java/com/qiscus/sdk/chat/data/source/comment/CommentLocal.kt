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

interface CommentLocal {
    fun addComment(commentEntity: CommentEntity)

    fun saveAndNotify(commentEntity: CommentEntity)

    fun updateComment(commentEntity: CommentEntity)

    fun addOrUpdateComment(commentEntity: CommentEntity)

    fun deleteComment(commentEntity: CommentEntity)

    fun getComment(commentIdEntity: CommentIdEntity): CommentEntity?

    fun getComments(roomId: String, limit: Int = -1): List<CommentEntity>

    fun getComments(roomId: String, lastCommentIdEntity: CommentIdEntity, limit: Int = -1): List<CommentEntity>

    fun getPendingComments(): List<CommentEntity>

    fun updateLastDeliveredComment(roomId: String, userId: String, commentId: CommentIdEntity)

    fun updateLastReadComment(roomId: String, userId: String, commentId: CommentIdEntity)

    fun getLastOnServerCommentId(): CommentIdEntity?

    fun getLastDeliveredCommentId(roomId: String): CommentIdEntity?

    fun getLastReadCommentId(roomId: String): CommentIdEntity?

    fun getOnServerComments(roomId: String, lastCommentIdEntity: CommentIdEntity, limit: Int = 20): List<CommentEntity>

    fun clearData()
}
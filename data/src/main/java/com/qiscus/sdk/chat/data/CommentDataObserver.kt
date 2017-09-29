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
import com.qiscus.sdk.chat.data.pubsub.comment.CommentSubscriber
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.pubsub.CommentObserver
import com.qiscus.sdk.chat.domain.pubsub.QiscusPubSubClient
import io.reactivex.Observable

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentDataObserver(private val pubSubClient: QiscusPubSubClient,
                          private val commentSubscriber: CommentSubscriber) : CommentObserver {

    override fun listenNewComment(): Observable<Comment> {
        return commentSubscriber.listenCommentAdded().map { it.toDomainModel() }
    }

    override fun listenCommentState(roomId: String): Observable<Comment> {
        return commentSubscriber.listenCommentUpdated()
                .doOnSubscribe { pubSubClient.listenCommentState(roomId) }
                .doOnDispose { pubSubClient.unlistenCommentState(roomId) }
                .filter { it.room.id == roomId }
                .map { it.toDomainModel() }
    }

    override fun listenCommentDeleted(roomId: String): Observable<Comment> {
        return commentSubscriber.listenCommentDeleted()
                .filter { it.room.id == roomId }
                .map { it.toDomainModel() }
    }
}
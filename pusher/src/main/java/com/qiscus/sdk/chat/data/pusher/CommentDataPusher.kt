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

package com.qiscus.sdk.chat.data.pusher

import com.qiscus.sdk.chat.data.model.CommentEntity
import com.qiscus.sdk.chat.data.pubsub.comment.CommentPublisher
import com.qiscus.sdk.chat.data.pubsub.comment.CommentSubscriber
import com.qiscus.sdk.chat.data.pusher.event.CommentEvent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentDataPusher(private val publisher: PublishSubject<Any>) : CommentPublisher, CommentSubscriber {

    override fun onCommentAdded(commentEntity: CommentEntity) {
        publisher.onNext(CommentEvent(commentEntity, CommentEvent.Type.ADDED))
    }

    override fun onCommentUpdated(commentEntity: CommentEntity) {
        publisher.onNext(CommentEvent(commentEntity, CommentEvent.Type.UPDATED))
    }

    override fun onCommentDeleted(commentEntity: CommentEntity) {
        publisher.onNext(CommentEvent(commentEntity, CommentEvent.Type.DELETED))
    }

    override fun listenCommentAdded(): Observable<CommentEntity> {
        return publisher.filter { it is CommentEvent }
                .map { it as CommentEvent }
                .filter { it.type == CommentEvent.Type.ADDED }
                .map { it.commentEntity }
    }

    override fun listenCommentUpdated(): Observable<CommentEntity> {
        return publisher.filter { it is CommentEvent }
                .map { it as CommentEvent }
                .filter { it.type == CommentEvent.Type.UPDATED }
                .map { it.commentEntity }
    }

    override fun listenCommentDeleted(): Observable<CommentEntity> {
        return publisher.filter { it is CommentEvent }
                .map { it as CommentEvent }
                .filter { it.type == CommentEvent.Type.DELETED }
                .map { it.commentEntity }
    }
}
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

package com.qiscus.sdk.chat.presentation.listencomment

import com.qiscus.sdk.chat.domain.interactor.BaseObserver
import com.qiscus.sdk.chat.domain.interactor.comment.ListenNewComment
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.presentation.mapper.CommentMapper

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListenCommentPresenter(val view: ListenCommentContract.View,
                             private val useCase: ListenNewComment,
                             private val mapper: CommentMapper) : ListenCommentContract.Presenter {

    override fun start() {
        listenNewComment()
    }

    override fun listenNewComment() {
        useCase.execute(Subscriber())
    }

    private fun handleNewComment(comment: Comment) {
        view.onNewComment(mapper.mapToView(comment))
    }

    override fun stop() {
        useCase.dispose()
    }

    inner class Subscriber : BaseObserver<Comment>() {
        override fun onNext(t: Comment) {
            handleNewComment(t)
        }
    }
}

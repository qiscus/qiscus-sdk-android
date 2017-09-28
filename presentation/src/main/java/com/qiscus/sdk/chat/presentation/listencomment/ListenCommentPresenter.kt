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

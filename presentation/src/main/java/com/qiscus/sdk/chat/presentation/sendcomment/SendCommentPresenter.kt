package com.qiscus.sdk.chat.presentation.sendcomment

import com.qiscus.sdk.chat.domain.interactor.comment.PostComment
import com.qiscus.sdk.chat.domain.model.*
import java.util.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class SendCommentPresenter(val view: SendCommentContract.View,
                           private val useCase: PostComment) : SendCommentContract.Presenter {

    override fun start() {}

    override fun stop() {
        useCase.dispose()
    }

    override fun sendComment(message: String) {
        useCase.execute(PostComment.Params(Comment(CommentId(), message, User("zetra"), Date(),
                Room(1, name = "Room"), CommentState.SENDING, CommentType("text"))))
        view.clearTextField()
    }
}
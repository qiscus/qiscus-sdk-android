package com.qiscus.sdk.chat.presentation.listencomment

import com.qiscus.sdk.chat.presentation.BasePresenter
import com.qiscus.sdk.chat.presentation.BaseView
import com.qiscus.sdk.chat.presentation.model.CommentView

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface ListenCommentContract {

    interface View : BaseView {
        fun onNewComment(commentView: CommentView)
    }

    interface Presenter : BasePresenter {
        fun listenNewComment()
    }
}
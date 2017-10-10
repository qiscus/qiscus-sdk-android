package com.qiscus.sdk.chat.presentation.listcomment

import com.qiscus.sdk.chat.domain.model.CommentId
import com.qiscus.sdk.chat.domain.model.CommentState
import com.qiscus.sdk.chat.presentation.BasePresenter
import com.qiscus.sdk.chat.presentation.BaseView
import com.qiscus.sdk.chat.presentation.model.CommentViewModel

/**
 * Created on : October 10, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface ListCommentContract {

    interface View : BaseView {
        fun addComment(commentViewModel: CommentViewModel)

        fun updateComment(commentViewModel: CommentViewModel)

        fun removeComment(commentViewModel: CommentViewModel)
    }

    interface Presenter : BasePresenter {
        fun setRoomId(roomId: String)

        fun loadComments(roomId: String, lastCommentId: CommentId? = null, limit: Int = 20)

        fun listenCommentAdded(roomId: String)

        fun listenCommentUpdated(roomId: String)

        fun listenCommentDeleted(roomId: String)

        fun updateCommentState(roomId: String, lastCommentId: CommentId, commentState: CommentState)
    }
}
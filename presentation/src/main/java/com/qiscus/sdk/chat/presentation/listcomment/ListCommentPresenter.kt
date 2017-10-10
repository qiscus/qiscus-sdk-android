package com.qiscus.sdk.chat.presentation.listcomment

import android.support.annotation.ColorInt
import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.interactor.comment.*
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.model.CommentId
import com.qiscus.sdk.chat.domain.model.CommentState
import com.qiscus.sdk.chat.presentation.MentionClickHandler
import com.qiscus.sdk.chat.presentation.mapper.toViewModel
import com.qiscus.sdk.chat.presentation.model.CommentViewModel

/**
 * Created on : October 10, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListCommentPresenter(val view: ListCommentContract.View,
                           private val getComments: GetComments,
                           private val listenNewComment: ListenNewComment,
                           private val listenCommentState: ListenCommentState,
                           private val listenCommentDeleted: ListenCommentDeleted,
                           private val updateCommentState: UpdateCommentState,
                           private @ColorInt val mentionAllColor: Int,
                           private @ColorInt val mentionOtherColor: Int,
                           private @ColorInt val mentionMeColor: Int,
                           private val mentionClickHandler: MentionClickHandler? = null) : ListCommentContract.Presenter {

    private lateinit var roomId: String

    override fun start() {
        listenCommentAdded(roomId)
        listenCommentUpdated(roomId)
        listenCommentDeleted(roomId)
        loadComments(roomId, limit = 20)
    }

    override fun setRoomId(roomId: String) {
        this.roomId = roomId
    }

    override fun loadComments(roomId: String, lastCommentId: CommentId?, limit: Int) {
        getComments.execute(GetComments.Params(roomId, lastCommentId, limit), Action {
            it.comments.forEach { view.addComment(toViewModel(it)) }
        })
    }

    override fun listenCommentAdded(roomId: String) {
        listenNewComment.execute(null, Action {
            if (it.room.id == roomId) {
                view.addComment(toViewModel(it))
                updateCommentState(roomId, it.commentId, CommentState.READ)
            }
        })
    }

    override fun listenCommentUpdated(roomId: String) {
        listenCommentState.execute(ListenCommentState.Params(roomId), Action {
            view.updateComment(toViewModel(it))
        })
    }

    override fun listenCommentDeleted(roomId: String) {
        listenCommentDeleted.execute(ListenCommentDeleted.Params(roomId), Action {
            view.removeComment(toViewModel(it))
        })
    }

    override fun updateCommentState(roomId: String, lastCommentId: CommentId, commentState: CommentState) {
        updateCommentState.execute(UpdateCommentState.Params(roomId, lastCommentId, commentState))
    }

    override fun stop() {
        getComments.dispose()
        listenNewComment.dispose()
        listenCommentState.dispose()
        listenCommentDeleted.dispose()
        updateCommentState.dispose()
    }


    private fun toViewModel(comment: Comment): CommentViewModel {
        return comment.toViewModel(mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickHandler)
    }
}
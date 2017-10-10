package com.qiscus.sdk.chat.presentation.listconversation

import android.support.annotation.ColorInt
import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.interactor.comment.GetComments
import com.qiscus.sdk.chat.domain.interactor.comment.ListenNewComment
import com.qiscus.sdk.chat.domain.interactor.room.*
import com.qiscus.sdk.chat.presentation.MentionClickHandler
import com.qiscus.sdk.chat.presentation.mapper.toViewModel
import com.qiscus.sdk.chat.presentation.model.ConversationViewModel

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListConversationPresenter(val view: ListConversationContract.View,
                                private val getRoom: GetRoom,
                                private val getRooms: GetRooms,
                                private val getComments: GetComments,
                                private val listenNewComment: ListenNewComment,
                                private val listenRoomAdded: ListenRoomAdded,
                                private val listenRoomUpdated: ListenRoomUpdated,
                                private val listenRoomDeleted: ListenRoomDeleted,
                                private @ColorInt val mentionAllColor: Int,
                                private @ColorInt val mentionOtherColor: Int,
                                private @ColorInt val mentionMeColor: Int,
                                private val mentionClickHandler: MentionClickHandler? = null) : ListConversationContract.Presenter {

    override fun start() {
        listenNewComment()
        listenConversationAdded()
        listenConversationUpdated()
        listenConversationDeleted()
        loadConversations()
    }

    private fun loadLastComment(conversationViewModel: ConversationViewModel, onSuccess: Action<ConversationViewModel>) {
        getComments.execute(GetComments.Params(conversationViewModel.room.id), Action {
            if (it.comments.isNotEmpty()) {
                conversationViewModel.lastComment = it.comments.first()
                        .toViewModel(mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickHandler)
            }
            onSuccess.call(conversationViewModel)
        })
    }

    private fun listenNewComment() {
        listenNewComment.execute(null, Action {
            view.addOrUpdateConversation(ConversationViewModel(it.room,
                    it.toViewModel(mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickHandler)))
        })
    }

    override fun loadConversations(page: Int, limit: Int) {
        getRooms.execute(GetRooms.Params(page, limit), Action {
            val conversations = it.map { ConversationViewModel(it, null) }
            conversations.forEach {
                loadLastComment(it, Action {
                    if (it.lastComment != null) {
                        view.addOrUpdateConversation(it)
                    }
                })
            }
        })
    }

    override fun listenConversationAdded() {
        listenRoomAdded.execute(null, Action {
            loadLastComment(ConversationViewModel(it), Action {
                if (it.lastComment != null) {
                    view.addOrUpdateConversation(it)
                }
            })
        })
    }

    override fun listenConversationUpdated() {
        listenRoomUpdated.execute(null, Action {
            loadLastComment(ConversationViewModel(it), Action {
                if (it.lastComment != null) {
                    view.addOrUpdateConversation(it)
                }
            })
        })
    }

    override fun listenConversationDeleted() {
        listenRoomUpdated.execute(null, Action {
            loadLastComment(ConversationViewModel(it), Action {
                view.removeConversation(it)
            })
        })
    }

    override fun stop() {
        getRoom.dispose()
        getRooms.dispose()
        getComments.dispose()
        listenNewComment.dispose()
        listenRoomAdded.dispose()
        listenRoomUpdated.dispose()
        listenRoomDeleted.dispose()
    }
}
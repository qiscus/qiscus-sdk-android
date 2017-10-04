package com.qiscus.sdk.chat.presentation.listconversation

import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.interactor.comment.GetComments
import com.qiscus.sdk.chat.domain.interactor.comment.ListenNewComment
import com.qiscus.sdk.chat.domain.interactor.room.*
import com.qiscus.sdk.chat.presentation.model.ConversationView

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
                                private val listenRoomDeleted: ListenRoomDeleted) : ListConversationContract.Presenter {

    override fun start() {
        listenNewComment()
        listenConversationAdded()
        listenConversationUpdated()
        listenConversationDeleted()
        loadConversations()
    }

    private fun loadLastComment(conversationView: ConversationView, onSuccess: Action<ConversationView>) {
        getComments.execute(GetComments.Params(conversationView.room.id), Action {
            if (it.isNotEmpty()) {
                conversationView.lastComment = it.last()
            }
            onSuccess.call(conversationView)
        })
    }

    private fun listenNewComment() {
        listenNewComment.execute(null, Action {
            view.addOrUpdateConversation(ConversationView(it.room, it))
        })
    }

    override fun loadConversations(page: Int, limit: Int) {
        getRooms.execute(GetRooms.Params(page, limit), Action {
            val conversations = it.map { ConversationView(it, null) }
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
            loadLastComment(ConversationView(it), Action {
                if (it.lastComment != null) {
                    view.addOrUpdateConversation(it)
                }
            })
        })
    }

    override fun listenConversationUpdated() {
        listenRoomUpdated.execute(null, Action {
            loadLastComment(ConversationView(it), Action {
                if (it.lastComment != null) {
                    view.addOrUpdateConversation(it)
                }
            })
        })
    }

    override fun listenConversationDeleted() {
        listenRoomUpdated.execute(null, Action {
            loadLastComment(ConversationView(it), Action {
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
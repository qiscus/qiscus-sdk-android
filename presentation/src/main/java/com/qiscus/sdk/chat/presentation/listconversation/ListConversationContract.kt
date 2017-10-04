package com.qiscus.sdk.chat.presentation.listconversation

import com.qiscus.sdk.chat.presentation.BasePresenter
import com.qiscus.sdk.chat.presentation.BaseView
import com.qiscus.sdk.chat.presentation.model.ConversationView

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface ListConversationContract {
    interface View : BaseView {
        fun addOrUpdateConversation(conversationView: ConversationView)

        fun removeConversation(conversationView: ConversationView)
    }

    interface Presenter : BasePresenter {
        fun loadConversations(page: Int = 1, limit: Int = 20)

        fun listenConversationAdded()

        fun listenConversationUpdated()

        fun listenConversationDeleted()
    }
}
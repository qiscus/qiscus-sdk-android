package com.qiscus.sdk.chat.presentation.listmessage

import com.qiscus.sdk.chat.domain.model.MessageId
import com.qiscus.sdk.chat.domain.model.MessageState
import com.qiscus.sdk.chat.presentation.BasePresenter
import com.qiscus.sdk.chat.presentation.BaseView
import com.qiscus.sdk.chat.presentation.model.MessageFileViewModel
import com.qiscus.sdk.chat.presentation.model.MessageImageViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel

/**
 * Created on : October 10, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface ListMessageContract {

    interface View : BaseView {
        fun addMessage(messageViewModel: MessageViewModel)

        fun updateMessage(messageViewModel: MessageViewModel)

        fun removeMessage(messageViewModel: MessageViewModel)

        fun openImageViewer(messageViewModel: MessageImageViewModel)

        fun openFileHandler(messageViewModel: MessageFileViewModel)

        fun showError(errorMessage: String)
    }

    interface Presenter : BasePresenter {
        fun setRoomId(roomId: String)

        fun loadMessages(roomId: String, lastMessageId: MessageId? = null, limit: Int = 20)

        fun listenMessageAdded(roomId: String)

        fun listenMessageUpdated(roomId: String)

        fun listenMessageDeleted(roomId: String)

        fun updateMessageState(roomId: String, lastMessageId: MessageId, messageState: MessageState)

        fun onMessageClick(messageViewModel: MessageViewModel)

        fun onMessageLongClick(messageViewModel: MessageViewModel)
    }
}